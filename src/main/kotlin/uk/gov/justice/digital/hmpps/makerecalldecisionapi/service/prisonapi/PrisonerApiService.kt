package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.OffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.getValueAndHandleWrappedException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter.OffenceConverter
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter.OffenderMovementConverter
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter.PrisonPeriodInfo
import java.util.Base64

@Service
internal class PrisonerApiService(
  private val prisonApiClient: PrisonApiClient,
  private val offenderMovementConverter: OffenderMovementConverter,
  private val offenceConverter: OffenceConverter,
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

  /**
   * Retrieve all sequences of sentences and their offences for a nomsId
   * @param nomsId The id in NOMS to retrieve all sentences/offences for
   * @return A series of SentenceSequences. These represent the sequence of consecutive sentences.
   * These will be sorted by the index sentence's end date whilst the subsequence sentences in the sequences
   * will be sorted first into consecutive groups using their consecutive to value and then again by end date
   * and court
   */
  fun retrieveOffences(nomsId: String): List<SentenceSequence> {
    val offender = prisonApiClient.retrieveOffender(nomsId).block()

    val prisonPeriods = getValueAndHandleWrappedException(
      prisonApiClient.retrievePrisonTimelines(nomsId),
    )!!.prisonPeriod

    val prisonPeriodInfos = prisonPeriods
      .map { prisonPeriod ->
        val lastDateOutOfPrison =
          prisonPeriod.movementDates.map { it.dateOutOfPrison }.filter { it != null }
            .maxWithOrNull(Comparator.naturalOrder())
        val movement = prisonPeriod.movementDates.find { it.dateOutOfPrison === lastDateOutOfPrison }
        val prisonDescription = movement?.releaseFromPrisonId?.let {
          try {
            prisonApiClient.retrieveAgency(movement.releaseFromPrisonId).block()?.longDescription
          } catch (notFoundEx: NotFoundException) {
            log.info("Agency with id ${movement.releaseFromPrisonId} not found: ${notFoundEx.message}")
            null
          }
        }

        val sentencesAndOffences = prisonApiClient.retrieveSentencesAndOffences(prisonPeriod.bookingId).block()!!

        PrisonPeriodInfo(
          prisonDescription,
          lastDateOutOfPrison,
          sentencesAndOffences,
        )
      }

    return offenceConverter.convert(
      offender,
      prisonPeriodInfos,
    )
  }

  fun getOffenderMovements(nomsId: String): List<OffenderMovement> {
    log.info("Searching for offender movements for offender with NOMIS ID $nomsId")
    val prisonApiOffenderMovements = prisonApiClient.retrieveOffenderMovements(nomsId).block()

    return if (prisonApiOffenderMovements != null) {
      offenderMovementConverter.convert(prisonApiOffenderMovements)
    } else {
      log.info("No movements found for offender with NOMIS ID $nomsId")
      emptyList()
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
