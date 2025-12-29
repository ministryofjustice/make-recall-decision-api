package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence
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

  /**
   * Sort sentences by first the sentence end date and the by court if there are any with the same date
   */
  private val sentenceSort: Comparator<Sentence> =
    compareByDescending<Sentence> { it.sentenceEndDate }.thenBy { it.courtDescription }

  /**
   * Sort sentence sequences by sorting their index sentences by sentenceSort
   */
  private val sentenceSequenceSort: Comparator<SentenceSequence> =
    Comparator { a: SentenceSequence, b: SentenceSequence -> sentenceSort.compare(a.indexSentence, b.indexSentence) }

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

    return getValueAndHandleWrappedException(
      prisonApiClient.retrievePrisonTimelines(nomsId),
    )!!.prisonPeriod
      .flatMap { prisonPeriod ->
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

        val sentencesForBooking =
          prisonApiClient.retrieveSentencesAndOffences(prisonPeriod.bookingId).block()!!.map { sentenceAndOffences ->

            sentenceAndOffences.copy(
              releaseDate = movement?.dateOutOfPrison,
              releasingPrison = prisonDescription,
              licenceExpiryDate = offender?.sentenceDetail?.licenceExpiryDate,
              offences = sentenceAndOffences.offences.sortedBy { it.offenceDescription },
            )
          }
            .filter { it.sentenceEndDate == null || !it.sentenceEndDate.isBefore(LocalDate.now()) }

        // Sentences that have no consecutiveToSequence value will be the index of a sentence sequence
        // even if it is the only sentence in that that sequence (i.e. has no consecutive)
        val indexSentences = sentencesForBooking.filter { it.consecutiveToSequence === null }
        // Any sentence that is not an index must be a consecutive/concurrent
        val consecutiveSequences = sentencesForBooking.filter { it.consecutiveToSequence !== null }

        // Build a list of sequence, using the map key to reference the sentenceSequence of the index
        // sentence for more immediate reference as it will be used frequently
        val sentenceSequenceMap: MutableMap<Int, SentenceSequence> = mutableMapOf()
        // Every index sentence will start a sequence so map a sequence for each index sentence
        indexSentences.forEach {
          // Should never be null as it make the sentence invalid, however the API contract is nullable
          // (as are all fields) and so we must defend against this until that may change
          if (it.sentenceSequence != null) {
            sentenceSequenceMap[it.sentenceSequence] = SentenceSequence(it)
          }
        }

        // Group all consecutive by consecutiveToSequence, creating concurrent groups of sentence with the same
        // Sort by key so we know we don't have to double back through the data
        val groupedByConsecutiveToSequence = consecutiveSequences.groupBy { it.consecutiveToSequence!! }.toSortedMap()
        // For every consecutiveToSequence (which we know already contains any concurrent for that consecutiveToSequence
        groupedByConsecutiveToSequence.forEach { (consecutiveTo, sentences) ->
          // If the group is consecutive to an index sentence...
          if (sentenceSequenceMap.containsKey(consecutiveTo)) {
            val sequence = sentenceSequenceMap.getValue(consecutiveTo)
            // ...create a new sentencesInSequence map as this must be the first in the chain...
            sentenceSequenceMap[consecutiveTo] = SentenceSequence(
              sequence.indexSentence,
              mutableMapOf(consecutiveTo to sentences.sortedWith(sentenceSort)),
            )
          }
          // ...else...
          else {
            sentenceSequenceMap.forEach { (key, sentenceSequence) ->
              // ...find the SentenceSequence that contains the consecutiveTo we are looking for...
              if (sentenceSequence.sentencesInSequence?.values?.flatMap { x -> x }
                  ?.any { s -> s.sentenceSequence == consecutiveTo } ?: false
              ) {
                // ...and add it to the map, sorting them as we do
                sentenceSequence.sentencesInSequence[consecutiveTo] = sentences.sortedWith(sentenceSort)
              }
            }
          }
        }

        // We don't need the map that references the index sentence's sequence so return all values as a list
        sentenceSequenceMap.values
      }
      // Sort the SentenceSequences as required
      .sortedWith(sentenceSequenceSort)
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
