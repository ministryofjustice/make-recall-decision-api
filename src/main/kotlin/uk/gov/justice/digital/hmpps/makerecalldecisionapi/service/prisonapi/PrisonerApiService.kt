package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.OffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.getValueAndHandleWrappedException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter.OffenderMovementConverter
import java.time.LocalDate
import java.util.Base64

@Service
internal class PrisonerApiService(
  private val prisonApiClient: PrisonApiClient,
  private val offenderMovementConverter: OffenderMovementConverter,
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
    val sentences = ArrayList(
      getValueAndHandleWrappedException(
        prisonApiClient.retrievePrisonTimelines(nomsId),
      )!!.prisonPeriod
        .flatMap { t ->
          prisonApiClient.retrieveSentencesAndOffences(t.bookingId).block()!!.map { sentenceAndOffences ->
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
            sentenceAndOffences.copy(
              releaseDate = movement?.dateOutOfPrison,
              releasingPrison = prisonDescription,
              licenceExpiryDate = offender?.sentenceDetail?.licenceExpiryDate,
              offences = sentenceAndOffences.offences.sortedBy { it.offenceDescription },
            )
          }
        }
        .filter { it.sentenceEndDate == null || !it.sentenceEndDate.isBefore(LocalDate.now()) }
        .sortedByDescending { it.sentenceEndDate ?: LocalDate.MAX }
        .sortedBy { it.courtDescription }
        .sortedByDescending { it.sentenceDate },
    )

    sentences.forEach { sentence ->
      // If the sentence is consecutive but nothing is consecutive to it, we are at the end of the sequence
      if (sentence.consecutiveToSequence != null && !sentences.any { s -> s.consecutiveToSequence == sentence.sentenceSequence }) {
        val consecutiveGroup = ArrayList<Int>()
        fun buildSequence(consecutiveTo: Int?) {
          val previousInSequence = sentences.find { s -> s.sentenceSequence == consecutiveTo }
          if (previousInSequence?.consecutiveToSequence != null) {
            buildSequence(previousInSequence.consecutiveToSequence)
            consecutiveGroup.add(previousInSequence.sentenceSequence!!)
          } else {
            consecutiveGroup.add(previousInSequence?.sentenceSequence!!)
          }
        }
        buildSequence(sentence.consecutiveToSequence)
        consecutiveGroup.add(sentence.sentenceSequence!!)
        consecutiveGroup.forEach { sentenceSequence ->
          val sentenceToUpdateIndex = sentences.indexOf(sentences.find { s -> s.sentenceSequence == sentenceSequence })
          sentences[sentenceToUpdateIndex] = sentences[sentenceToUpdateIndex].copy(consecutiveGroup = consecutiveGroup)
        }
      }
    }

    return sentences
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
