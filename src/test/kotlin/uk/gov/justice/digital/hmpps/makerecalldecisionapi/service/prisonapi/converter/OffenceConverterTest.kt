package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentenceOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.SentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.sentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.sentenceCalculationDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomFutureLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomPastLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate
import java.time.LocalDateTime

internal class OffenceConverterTest {

  val offenceConverter = OffenceConverter()

  @Test
  fun `convert empty prison periods returns empty sentences`() {
    // when
    val actualSentences = offenceConverter.convert(
      offender(),
      emptyList(),
    )

    // then
    assertThat(actualSentences).isEmpty()
  }

  @Test
  fun `sentences with past end dates are excluded`() {
    // when
    val actualSentences = offenceConverter.convert(
      offender(),
      listOf(
        PrisonPeriodInfo(
          randomString(),
          sentenceCalculationDates(
            randomPastLocalDate(),
            randomPastLocalDate(),
          ),
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = null, // make this the index sentence
            ),
          ),
        ),
      ),
    )

    // then
    assertThat(actualSentences).isEmpty()
  }

  @Test
  fun `no index sentence results in no sentences`() {
    // when
    val actualSentences = offenceConverter.convert(
      offender(),
      listOf(
        PrisonPeriodInfo(
          randomString(),
          sentenceCalculationDates(
            randomFutureLocalDate(),
            randomFutureLocalDate(),
          ),
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = 1, // not an index sentence
            ),
          ),
        ),
      ),
    )

    // then
    assertThat(actualSentences).isEmpty()
  }

  @Test
  fun `converted offences are sorted`() {
    // given
    val expectedOffenceDescriptionsInOrder = listOf("ABC", "DEF", "GHI", "NMO")

    // when
    val randomisedDescriptions = expectedOffenceDescriptionsInOrder.shuffled()
    val actualSentences = offenceConverter.convert(
      offender(),
      listOf(
        PrisonPeriodInfo(
          randomString(),
          sentenceCalculationDates(
            randomFutureLocalDate(),
            randomFutureLocalDate(),
          ),
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = null, // make this the index sentence
              offences = randomisedDescriptions.map { sentenceOffence(offenceDescription = it) },
            ),
          ),
        ),
      ),
    )

    // then
    val actualOffenceDescriptions = actualSentences[0].indexSentence.offences.map { it.offenceDescription }
    assertThat(actualOffenceDescriptions).containsExactlyElementsOf(expectedOffenceDescriptionsInOrder)
  }

  @Test
  fun `converted sentences have null releasingPrison if provided prisonDescription is null`() {
    // when
    val actualSentences = offenceConverter.convert(
      offender(),
      listOf(
        PrisonPeriodInfo(
          null,
          sentenceCalculationDates(
            randomFutureLocalDate(),
            randomFutureLocalDate(),
          ),
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = null, // make this the index sentence
            ),
          ),
        ),
      ),
    )
  }

  @Test
  fun `converted sentences use sentenceExpiryOverrideDate when available`() {
    // when
    val overrideEndDate = randomFutureLocalDate()
    val actualSentences = offenceConverter.convert(
      offender(),
      listOf(
        PrisonPeriodInfo(
          null,
          sentenceCalculationDates(
            overrideEndDate,
            randomFutureLocalDate(),
          ),
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = null, // make this the index sentence
            ),
          ),
        ),
      ),
    )

    // then
    assertThat(actualSentences).hasSize(1)
    assertThat(actualSentences[0].indexSentence.sentenceEndDate).isEqualTo(overrideEndDate)
    assertThat(actualSentences[0].indexSentence.sentenceSequenceExpiryDate).isEqualTo(overrideEndDate)
    actualSentences[0].sentencesInSequence?.values?.flatten()?.forEach { sentence ->
      assertThat(sentence.sentenceEndDate).isNull()
      assertThat(sentence.sentenceSequenceExpiryDate).isNull()
    }
  }

  @Test
  fun `converted sentences use sentenceExpiryCalculatedDate when sentenceExpiryOverrideDate is null`() {
    // when
    val calculatedDate = randomFutureLocalDate()
    val actualSentences = offenceConverter.convert(
      offender(),
      listOf(
        PrisonPeriodInfo(
          null,
          sentenceCalculationDates(
            null,
            calculatedDate,
          ),
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = null, // make this the index sentence
            ),
          ),
        ),
      ),
    )

    // then
    assertThat(actualSentences).hasSize(1)
    assertThat(actualSentences[0].indexSentence.sentenceEndDate).isEqualTo(calculatedDate)
    assertThat(actualSentences[0].indexSentence.sentenceSequenceExpiryDate).isEqualTo(calculatedDate)
    actualSentences[0].sentencesInSequence?.values?.flatten()?.forEach { sentence ->
      assertThat(sentence.sentenceEndDate).isNull()
      assertThat(sentence.sentenceSequenceExpiryDate).isNull()
    }
  }

  @Test
  fun `converted sentences have prison period details aligned`() {
    // given
    val licenceExpiryDate = randomLocalDate()
    val firstPeriodPrisonDescription = randomString()
    val firstPeriodSentenceEndDate =
      LocalDate.now().plusDays(7) // ensures doing minusDays(1) below also leads to future date
    val firstPeriodLastDateOutOfPrison = randomLocalDateTime()
    val firstPrisonPeriodSentences = listOf(
      sentence(
        consecutiveToSequence = null, // make this the index sentence
        sentenceSequence = 1,
      ),
      sentence(
        consecutiveToSequence = 1,
        sentenceSequence = 2,
      ),
    )
    val secondPeriodPrisonDescription = randomString()
    val secondPeriodSentenceEndDate = firstPeriodSentenceEndDate.minusDays(1)
    val secondPeriodLastDateOutOfPrison = randomLocalDateTime()
    val secondPrisonPeriodSentences = listOf(
      sentence(
        consecutiveToSequence = null, // make this the index sentence
        sentenceSequence = 1,
      ),
      sentence(
        consecutiveToSequence = 1,
        sentenceSequence = 2,
      ),
    )

    val convertToExpectedResponseSentence = {
        sentence: Sentence,
        sentenceSequenceExpiryDate: LocalDate?,
        releaseDate: LocalDateTime?,
        releasingPrison: String,
        licenceExpiryDate: LocalDate,
      ->
      uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.sentence(
        bookingId = sentence.bookingId,
        sentenceSequence = sentence.sentenceSequence,
        lineSequence = sentence.lineSequence,
        consecutiveToSequence = sentence.consecutiveToSequence,
        caseSequence = sentence.caseSequence,
        courtDescription = sentence.courtDescription,
        sentenceStatus = sentence.sentenceStatus,
        sentenceCategory = sentence.sentenceCategory,
        sentenceCalculationType = sentence.sentenceCalculationType,
        sentenceTypeDescription = sentence.sentenceTypeDescription,
        sentenceDate = sentence.sentenceDate,
        sentenceStartDate = sentence.sentenceStartDate,
        sentenceEndDate = null, // as all expected sequences have more than one sentence
        sentenceSequenceExpiryDate = sentenceSequenceExpiryDate,
        terms = sentence.terms.map {
          uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.term(
            years = it.years,
            months = it.months,
            weeks = it.weeks,
            days = it.days,
            code = it.code,
          )
        },
        offences = sentence.offences.map {
          uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.sentenceOffence(
            offenderChargeId = it.offenderChargeId,
            offenceStartDate = it.offenceStartDate,
            offenceStatute = it.offenceStatute,
            offenceCode = it.offenceCode,
            offenceDescription = it.offenceDescription,
            indicators = it.indicators,
          )
        },
        releaseDate = releaseDate,
        releasingPrison = releasingPrison,
        licenceExpiryDate = licenceExpiryDate,
      )
    }
    val expectedSentenceSequence = listOf(
      sentenceSequence(
        indexSentence = convertToExpectedResponseSentence(
          firstPrisonPeriodSentences[0],
          firstPeriodSentenceEndDate,
          firstPeriodLastDateOutOfPrison,
          firstPeriodPrisonDescription,
          licenceExpiryDate,
        ),
        sentencesInSequence = mutableMapOf(
          1 to listOf(
            convertToExpectedResponseSentence(
              firstPrisonPeriodSentences[1],
              null,
              firstPeriodLastDateOutOfPrison,
              firstPeriodPrisonDescription,
              licenceExpiryDate,
            ),
          ),
        ),
      ),
      sentenceSequence(
        indexSentence = convertToExpectedResponseSentence(
          secondPrisonPeriodSentences[0],
          secondPeriodSentenceEndDate,
          secondPeriodLastDateOutOfPrison,
          secondPeriodPrisonDescription,
          licenceExpiryDate,
        ),
        sentencesInSequence = mutableMapOf(
          1 to listOf(
            convertToExpectedResponseSentence(
              secondPrisonPeriodSentences[1],
              null,
              secondPeriodLastDateOutOfPrison,
              secondPeriodPrisonDescription,
              licenceExpiryDate,
            ),
          ),
        ),
      ),
    )

    // when
    val actualSentences = offenceConverter.convert(
      offender(
        sentenceDetail = sentenceDetail(licenceExpiryDate = licenceExpiryDate),
      ),
      listOf(
        PrisonPeriodInfo(
          firstPeriodPrisonDescription,
          sentenceCalculationDates(
            firstPeriodSentenceEndDate,
            firstPeriodSentenceEndDate,
          ),
          firstPeriodLastDateOutOfPrison,
          firstPrisonPeriodSentences,
        ),
        PrisonPeriodInfo(
          secondPeriodPrisonDescription,
          sentenceCalculationDates(
            secondPeriodSentenceEndDate,
            secondPeriodSentenceEndDate,
          ),
          secondPeriodLastDateOutOfPrison,
          secondPrisonPeriodSentences,
        ),
      ),
    )

    // then
    assertThat(actualSentences).isEqualTo(expectedSentenceSequence)
  }

  @Test
  fun `converted sentences are correctly ordered with consecutive and concurrent sentence`() {
    // given
    val defaultBookingId = 123
    val alternativeBookingId = 987
    val defaultBookingCourt = "First Booking Court"
    val alternativeBookingCourt = "Second Booking Court"
    val testDate = randomFutureLocalDate()
    val firstPeriodSentenceEndDate = testDate.plusDays(1)
    val secondPeriodSentenceEndDate = testDate

    /* This set of sentences is to produce the following SentenceSequences of increasing complexity
     * - { indexSentence: 0, sentencesInSequence: null } Single sentence
     * - { indexSentence: 1, sentencesInSequence: {1=[2]} } Sentence with single consecutive
     * - { indexSentence: 3, sentencesInSequence: {3=[4], 4=[5]} 5=[14]} Sentence with multiple consecutive, including one out of sequence
     * - { indexSentence: 6, sentencesInSequence: {6=[7, 8]} } Sentence with single concurrent consecutive set
     * - { indexSentence: 9, sentencesInSequence: {9=[10, 11], 10=[12, 13]} } Sentence with multiple concurrent consecutive sets, 10 and 11 stay in
     *                                            order (same court), 12 and 13 to be sorted by court
     */
    val sentencesForSequencesFirst = listOf(
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 0,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 1,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 2,
        consecutiveToSequence = 1,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 3,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 4,
        consecutiveToSequence = 3,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 5,
        consecutiveToSequence = 4,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 6,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 7,
        consecutiveToSequence = 6,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 8,
        consecutiveToSequence = 6,
        courtDescription = defaultBookingCourt,
      ),

      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 9,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 10,
        consecutiveToSequence = 9,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 11,
        consecutiveToSequence = 9,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 13,
        consecutiveToSequence = 10,
        courtDescription = alternativeBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 12,
        consecutiveToSequence = 10,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 14,
        consecutiveToSequence = 5,
        courtDescription = defaultBookingCourt,
      ),
    )

    val convertToExpectedResponseSentence =
      { sentence: Sentence ->
        // we only use a subset, as that's what is used above to initialise the sentences
        uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.response.prison.Sentence(
          bookingId = sentence.bookingId,
          sentenceSequence = sentence.sentenceSequence,
          consecutiveToSequence = sentence.consecutiveToSequence,
          courtDescription = sentence.courtDescription,
        )
      }

    // sentenceSequence 0: stand alone
    val expectedSentenceSequenceA = SentenceSequence(
      indexSentence = convertToExpectedResponseSentence(sentencesForSequencesFirst[0]).copy(
        sentenceEndDate = firstPeriodSentenceEndDate,
        sentenceSequenceExpiryDate = firstPeriodSentenceEndDate,
      ),
      sentencesInSequence = null,
    )

    // sentenceSequence 1, 2: sentence with a single consecutive
    val expectedSentenceSequenceB = SentenceSequence(
      indexSentence = convertToExpectedResponseSentence(sentencesForSequencesFirst[1]).copy(
        sentenceSequenceExpiryDate = firstPeriodSentenceEndDate,
      ),
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesFirst[1].sentenceSequence!! to listOf(
          convertToExpectedResponseSentence(
            sentencesForSequencesFirst[2],
          ),
        ),
      ),
    )

    // sentenceSequence 3, 4, 5: sentence with a single consecutive followed by a single consecutive
    val expectedSentenceSequenceC = SentenceSequence(
      indexSentence = convertToExpectedResponseSentence(sentencesForSequencesFirst[3]).copy(
        sentenceSequenceExpiryDate = firstPeriodSentenceEndDate,
      ),
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesFirst[3].sentenceSequence!! to listOf(
          convertToExpectedResponseSentence(
            sentencesForSequencesFirst[4],
          ),
        ),
        sentencesForSequencesFirst[4].sentenceSequence!! to listOf(
          convertToExpectedResponseSentence(
            sentencesForSequencesFirst[5],
          ),
        ),
        sentencesForSequencesFirst[5].sentenceSequence!! to listOf(
          convertToExpectedResponseSentence(
            sentencesForSequencesFirst[14],
          ),
        ),
      ),
    )

    // sentenceSequence 6, 7, 8: sentence with a consecutively concurrents
    val expectedSentenceSequenceD = SentenceSequence(
      indexSentence = convertToExpectedResponseSentence(sentencesForSequencesFirst[6]).copy(
        sentenceSequenceExpiryDate = firstPeriodSentenceEndDate,
      ),
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesFirst[6].sentenceSequence!! to listOf(
          convertToExpectedResponseSentence(sentencesForSequencesFirst[7]),
          convertToExpectedResponseSentence(sentencesForSequencesFirst[8]),
        ),
      ),
    )

    // sentenceSequence 9, 10, 11: sentence with multiple concurrents consecutive to each other
    val expectedSentenceSequenceE = SentenceSequence(
      indexSentence = convertToExpectedResponseSentence(sentencesForSequencesFirst[9]).copy(
        sentenceSequenceExpiryDate = firstPeriodSentenceEndDate,
      ),
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesFirst[9].sentenceSequence!! to listOf(
          convertToExpectedResponseSentence(sentencesForSequencesFirst[10]),
          convertToExpectedResponseSentence(sentencesForSequencesFirst[11]),
        ),
        sentencesForSequencesFirst[10].sentenceSequence!! to listOf(
          convertToExpectedResponseSentence(sentencesForSequencesFirst[13]),
          convertToExpectedResponseSentence(sentencesForSequencesFirst[12]),
        ),
      ),
    )

    /*
     * A supplementary set of sentences to produce further complex
     * test cases against sentenceForSequencesFirst
     * 21 and 0 will need sorting by court to end in the correct order
     * - { indexSentence 21: sentencesInSequence: {21=[22, 23], 22=[24, 25]} These will be delivered out of order, 24 and 25 need sorting by court
     * - { indexSentence: 0, sentencesInSequence: null } Same sentence sequence as previous booking, but should appear differently
     */
    val sentencesForSequencesSecond = listOf(
      Sentence(
        bookingId = alternativeBookingId,
        sentenceSequence = 0,
        consecutiveToSequence = null,
        courtDescription = alternativeBookingCourt,
      ),

      Sentence(
        bookingId = alternativeBookingId,
        sentenceSequence = 25,
        consecutiveToSequence = 22,
        courtDescription = alternativeBookingCourt,
      ),
      Sentence(
        bookingId = alternativeBookingId,
        sentenceSequence = 22,
        consecutiveToSequence = 21,
      ),
      Sentence(
        bookingId = alternativeBookingId,
        sentenceSequence = 21,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        bookingId = alternativeBookingId,
        sentenceSequence = 23,
        consecutiveToSequence = 21,
      ),
      Sentence(
        bookingId = alternativeBookingId,
        sentenceSequence = 24,
        consecutiveToSequence = 22,
        courtDescription = defaultBookingCourt,
      ),
    )

    // sentenceSequence: 21, 22, 23, 24, 25: delivered out of order but handled
    // Expect to be sorted after index 0 due to end date
    val expectedSentenceSequenceF = SentenceSequence(
      indexSentence = convertToExpectedResponseSentence(sentencesForSequencesSecond[3]).copy(
        sentenceSequenceExpiryDate = secondPeriodSentenceEndDate,
      ),
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesSecond[3].sentenceSequence!! to listOf(
          convertToExpectedResponseSentence(sentencesForSequencesSecond[2]),
          convertToExpectedResponseSentence(sentencesForSequencesSecond[4]),
        ),
        sentencesForSequencesSecond[2].sentenceSequence!! to listOf(
          convertToExpectedResponseSentence(sentencesForSequencesSecond[5]),
          convertToExpectedResponseSentence(sentencesForSequencesSecond[1]),
        ),
      ),
    )

    // sentenceSequence 0: stand alone, same sentence sequence as previous but unique booking
    // Expect to be sorted after before 21 due to end date
    val expectedSentenceSequenceG = SentenceSequence(
      indexSentence = convertToExpectedResponseSentence(sentencesForSequencesSecond[0]).copy(
        sentenceEndDate = secondPeriodSentenceEndDate,
        sentenceSequenceExpiryDate = secondPeriodSentenceEndDate,
      ),
      sentencesInSequence = null,
    )

    val expectedSentenceSequences = listOf(
      expectedSentenceSequenceA,
      expectedSentenceSequenceB,
      expectedSentenceSequenceC,
      expectedSentenceSequenceD,
      expectedSentenceSequenceE,
      expectedSentenceSequenceF,
      expectedSentenceSequenceG,
    )

    // when
    // We use null values for the licenceExpiryDate, prisonDescription and lastDateOfOutPrison
    // values because the converter uses them to override values in the resulting sentences, but
    // that isn't relevant to what we're testing in this use case. However, were we to provide
    // them, we would need to adjust the expected sentences accordingly, which would overcomplicate
    // the test set-up unnecessarily. Further up we're using the default Sentence constructor, so
    // non-specified fields are null already, hence why null works here.
    val actualSentences = offenceConverter.convert(
      offender(
        sentenceDetail = sentenceDetail(
          licenceExpiryDate = null,
        ),
      ),
      listOf(
        PrisonPeriodInfo(
          null,
          sentenceCalculationDates(firstPeriodSentenceEndDate),
          null,
          sentencesForSequencesFirst,
        ),
        PrisonPeriodInfo(
          null,
          sentenceCalculationDates(secondPeriodSentenceEndDate),
          null,
          sentencesForSequencesSecond,
        ),
      ),
    )

    // then
    assertThat(actualSentences).containsExactlyElementsOf(expectedSentenceSequences)
  }
}
