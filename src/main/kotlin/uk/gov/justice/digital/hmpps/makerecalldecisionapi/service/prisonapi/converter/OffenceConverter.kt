package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.SentenceCalculationDates
import java.time.LocalDate
import java.time.LocalDateTime

@Service
internal class OffenceConverter {

  fun convert(
    offender: Offender?,
    prisonPeriodInfoList: List<PrisonPeriodInfo>,
  ): List<SentenceSequence> = prisonPeriodInfoList.flatMap {
    convert(
      offender,
      it,
    )
  }.sortedWith(sentenceSequenceSort)

  private fun convert(
    offender: Offender?,
    prisonPeriodInfo: PrisonPeriodInfo,
  ): List<SentenceSequence> {
    val periodSentenceEndDate = prisonPeriodInfo.sentenceCalculationDates.sentenceExpiryOverrideDate
      ?: prisonPeriodInfo.sentenceCalculationDates.sentenceExpiryCalculatedDate
    if (periodSentenceEndDate == null || periodSentenceEndDate.isBefore(LocalDate.now())) {
      return emptyList()
    }

    val sentencesForBooking = prisonPeriodInfo.sentencesAndOffences
      .map { sentenceAndOffences ->
        sentenceAndOffences.copy(
          // sentence end dates are now calculated (by the Calculate Release Dates team)
          // for "the entire sentence calculation envelope", hence our overriding here.
          sentenceEndDate = periodSentenceEndDate,
          releaseDate = prisonPeriodInfo.lastDateOutOfPrison,
          releasingPrison = prisonPeriodInfo.prisonDescription,
          licenceExpiryDate = offender?.sentenceDetail?.licenceExpiryDate,
          offences = sentenceAndOffences.offences.sortedBy { it.offenceDescription },
        )
      }

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
    return sentenceSequenceMap.values.toList()
  }

  /**
   * Sort sentences by first the sentence end date and the by court if there are any with the same date.
   *
   * Note that sentences within the same period all have the same end date (we override them to make it so
   * during the conversion), but this comparator is also used in the sentence sequence sorter below, where
   * index sentences from different sequences are compared. In the incredibly unlikely case this ever
   * becomes a performance issue, we can be more granular about the comparisons, but this is simpler for now.
   */
  private val sentenceSort: Comparator<Sentence> =
    compareByDescending<Sentence> { it.sentenceEndDate }.thenBy { it.courtDescription }

  /**
   * Sort sentence sequences by sorting their index sentences by sentenceSort
   */
  private val sentenceSequenceSort: Comparator<SentenceSequence> =
    Comparator { a: SentenceSequence, b: SentenceSequence -> sentenceSort.compare(a.indexSentence, b.indexSentence) }
}

internal data class PrisonPeriodInfo(
  val prisonDescription: String?,
  val sentenceCalculationDates: SentenceCalculationDates,
  val lastDateOutOfPrison: LocalDateTime?,
  val sentencesAndOffences: List<Sentence>,
)
