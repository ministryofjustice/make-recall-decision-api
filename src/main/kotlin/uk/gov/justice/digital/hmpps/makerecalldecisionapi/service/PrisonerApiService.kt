package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import java.time.LocalDate
import java.util.Base64

@Service
internal class PrisonerApiService(
  private val prisonApiClient: PrisonApiClient,
) {
  fun searchPrisonApi(nomsId: String): Offender {
    log.info("searching for offender using nomis id : $nomsId")
    val response = getValueAndHandleWrappedException(
      prisonApiClient.retrieveOffender(nomsId),
    )

    response?.agencyId?.let {
      try {
        val agency = prisonApiClient.retrieveAgency(it).block()
        response.agencyDescription = agency?.description
      } catch (ex: Exception) {
        log.info(
          "Could not retrieve offender agency (prison): " +
            "${response.agencyId} for NOMIS offender id: " +
            "$nomsId; exception message: " +
            "${ex.message}",
        )
      }
    }

    response?.facialImageId?.let {
      if (response.facialImageId > 0) {
        try {
          val responseImage = prisonApiClient.retrieveImageData(response.facialImageId.toString()).block()
          val contentType = responseImage?.headers?.get("Content-Type")?.get(0)
          response.image =
            "data:" + contentType + ";base64," + String(Base64.getEncoder().encode(responseImage?.body?.byteArray))
        } catch (ex: Exception) {
          log.info(
            "could not retrieve facial image id: " +
              "${response.facialImageId} for NOMIS offender id: " +
              "$nomsId; exception message: " +
              "${ex.message}",
          )
        }
      }
    }

    return response!!
  }

  fun retrieveOffences(nomsId: String): List<Sentence> {
    return getValueAndHandleWrappedException(
      prisonApiClient.retrievePrisonTimelines(nomsId),
    )!!.prisonPeriod
      .flatMap { t ->
        prisonApiClient.retrieveSentencesAndOffences(t.bookingId).block()!!.map { sentencesAndOffences ->
          val lastDateOutOfPrison =
            t.movementDates.map { it.dateOutOfPrison }.filter { it != null }.maxWithOrNull(Comparator.naturalOrder())
          val movement = t.movementDates.find { it.dateOutOfPrison === lastDateOutOfPrison }
          val prisonDescription = movement?.releaseFromPrisonId?.let {
            try {
              prisonApiClient.retrieveAgency(movement.releaseFromPrisonId).block()?.longDescription
            } catch (notFoundEx: NotFoundException) {
              log.info("Agency with id ${movement.releaseFromPrisonId} not found: ${notFoundEx.message}")
              null
            }
          }

          val offender = prisonApiClient.retrieveOffender(nomsId).block()
          sentencesAndOffences.copy(
            releaseDate = movement?.dateOutOfPrison,
            releasingPrison = prisonDescription,
            licenceExpiryDate = offender?.sentenceDetail?.licenceExpiryDate,
            offences = sentencesAndOffences.offences.sortedBy { it.offenceDescription },
          )
        }
      }
      .filter { it.sentenceEndDate == null || !it.sentenceEndDate.isBefore(LocalDate.now()) }
      .sortedByDescending { it.sentenceEndDate ?: LocalDate.MAX }
      .sortedBy { it.courtDescription }
      .sortedByDescending { it.sentenceDate }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
