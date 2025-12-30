package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentenceOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.sentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomFutureLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

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
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = null, // make this the index sentence
              sentenceEndDate = LocalDate.now().minusDays(1),
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
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = 1, // not an index sentence
              sentenceEndDate = randomFutureLocalDate(),
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
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = null, // make this the index sentence
              sentenceEndDate = randomFutureLocalDate(),
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
          randomLocalDateTime(),
          listOf(
            sentence(
              consecutiveToSequence = null, // make this the index sentence
              sentenceEndDate = randomFutureLocalDate(),
            ),
          ),
        ),
      ),
    )

    // then
    assertThat(actualSentences[0].indexSentence.releasingPrison).isNull()
    actualSentences.forEach { sentenceSequence ->
      assertThat(sentenceSequence.indexSentence.releasingPrison).isNull()
      sentenceSequence.sentencesInSequence?.values?.flatten()?.forEach { sentence ->
        assertThat(sentence.releasingPrison).isNull()
      }
    }
  }

  @Test
  fun `converted sentences have prison period details aligned`() {
    // given
    val licenceExpiryDate = randomLocalDate()
    val firstPeriodPrisonDescription = randomString()
    val firstPeriodLastDateOutOfPrison = randomLocalDateTime()
    val firstPrisonPeriodSentences = listOf(
      sentence(
        consecutiveToSequence = null, // make this the index sentence
        sentenceSequence = 1,
        sentenceEndDate = LocalDate.now().plusDays(7), // ensures doing minusDays(1) below also leads to future date
      ),
      sentence(
        consecutiveToSequence = 1,
        sentenceSequence = 2,
        sentenceEndDate = randomFutureLocalDate(),
      ),
    )
    val secondPeriodPrisonDescription = randomString()
    val secondPeriodLastDateOutOfPrison = randomLocalDateTime()
    val secondPrisonPeriodSentences = listOf(
      sentence(
        consecutiveToSequence = null, // make this the index sentence
        sentenceSequence = 1,
        sentenceEndDate = firstPrisonPeriodSentences[0].sentenceEndDate!!.minusDays(1),
      ),
      sentence(
        consecutiveToSequence = 1,
        sentenceSequence = 2,
        sentenceEndDate = randomFutureLocalDate(),
      ),
    )

    val expectedSentenceSequence = listOf(
      sentenceSequence(
        indexSentence = firstPrisonPeriodSentences[0].copy(
          releaseDate = firstPeriodLastDateOutOfPrison,
          releasingPrison = firstPeriodPrisonDescription,
          licenceExpiryDate = licenceExpiryDate,
        ),
        sentencesInSequence = mutableMapOf(
          1 to listOf(
            firstPrisonPeriodSentences[1].copy(
              releaseDate = firstPeriodLastDateOutOfPrison,
              releasingPrison = firstPeriodPrisonDescription,
              licenceExpiryDate = licenceExpiryDate,
            ),
          ),
        ),
      ),
      sentenceSequence(
        indexSentence = secondPrisonPeriodSentences[0].copy(
          releaseDate = secondPeriodLastDateOutOfPrison,
          releasingPrison = secondPeriodPrisonDescription,
          licenceExpiryDate = licenceExpiryDate,
        ),
        sentencesInSequence = mutableMapOf(
          1 to listOf(
            secondPrisonPeriodSentences[1].copy(
              releaseDate = secondPeriodLastDateOutOfPrison,
              releasingPrison = secondPeriodPrisonDescription,
              licenceExpiryDate = licenceExpiryDate,
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
          firstPeriodLastDateOutOfPrison,
          firstPrisonPeriodSentences,
        ),
        PrisonPeriodInfo(
          secondPeriodPrisonDescription,
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

    /* This set of sentences is to produce the following SentenceSequences of increasing complexity
     * - { indexSentence: 0, sentencesInSequence: null } Single sentence
     * - { indexSentence: 1, sentencesInSequence: {1=[2]} } Sentence with single consecutive
     * - { indexSentence: 3, sentencesInSequence: {3=[4], 4=[5]} 5=[14]} Sentence with multiple consecutive, including one out of sequence
     * - { indexSentence: 6, sentencesInSequence: {6=[7, 8]} } Sentence with single concurrent consecutive set
     * - { indexSentence: 9, sentencesInSequence: {9=[10, 11], 10=[12, 13]} } Sentence with multiple concurrent consecutive sets, 12 and 13 to be sorted by same end date but different courts
     */
    val sentencesForSequencesFirst = listOf(
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 0,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
        sentenceEndDate = testDate.plusDays(100),
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 1,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
        sentenceEndDate = testDate.plusDays(99),
      ),
      Sentence(
        bookingId = defaultBookingId,
        sentenceSequence = 2,
        consecutiveToSequence = 1,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 3,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
        sentenceEndDate = testDate.plusDays(98),
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 4,
        consecutiveToSequence = 3,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 5,
        consecutiveToSequence = 4,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 6,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
        sentenceEndDate = testDate.plusDays(97),
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 7,
        consecutiveToSequence = 6,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 8,
        consecutiveToSequence = 6,
        courtDescription = defaultBookingCourt,
      ),

      Sentence(
        defaultBookingId,
        sentenceSequence = 9,
        consecutiveToSequence = null,
        courtDescription = defaultBookingCourt,
        sentenceEndDate = testDate.plusDays(96),
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 10,
        consecutiveToSequence = 9,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 11,
        consecutiveToSequence = 9,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 13,
        consecutiveToSequence = 10,
        sentenceEndDate = testDate,
        courtDescription = alternativeBookingCourt,
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 12,
        consecutiveToSequence = 10,
        sentenceEndDate = testDate,
        courtDescription = defaultBookingCourt,
      ),
      Sentence(
        defaultBookingId,
        sentenceSequence = 14,
        consecutiveToSequence = 5,
        courtDescription = defaultBookingCourt,
      ),
    )

    // sentenceSequence 0: stand alone
    val expectedSentenceSequenceA = SentenceSequence(
      indexSentence = sentencesForSequencesFirst[0],
      sentencesInSequence = null,
    )

    // sentenceSequence 1, 2: sentence with a single consecutive
    val expectedSentenceSequenceB = SentenceSequence(
      indexSentence = sentencesForSequencesFirst[1],
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesFirst[1].sentenceSequence!! to listOf(sentencesForSequencesFirst[2]),
      ),
    )

    // sentenceSequence 3, 4, 5: sentence with a single consecutive followed by a single consecutive
    val expectedSentenceSequenceC = SentenceSequence(
      indexSentence = sentencesForSequencesFirst[3],
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesFirst[3].sentenceSequence!! to listOf(sentencesForSequencesFirst[4]),
        sentencesForSequencesFirst[4].sentenceSequence!! to listOf(sentencesForSequencesFirst[5]),
        sentencesForSequencesFirst[5].sentenceSequence!! to listOf(sentencesForSequencesFirst[14]),
      ),
    )

    // sentenceSequence 6, 7, 8: sentence with a consecutively concurrents
    val expectedSentenceSequenceD = SentenceSequence(
      indexSentence = sentencesForSequencesFirst[6],
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesFirst[6].sentenceSequence!! to listOf(
          sentencesForSequencesFirst[7],
          sentencesForSequencesFirst[8],
        ),
      ),
    )

    // sentenceSequence 9, 10, 11: sentence with multiple concurrents consecutive to each other
    val expectedSentenceSequenceE = SentenceSequence(
      indexSentence = sentencesForSequencesFirst[9],
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesFirst[9].sentenceSequence!! to listOf(
          sentencesForSequencesFirst[10],
          sentencesForSequencesFirst[11],
        ),
        sentencesForSequencesFirst[10].sentenceSequence!! to listOf(
          sentencesForSequencesFirst[13],
          sentencesForSequencesFirst[12],
        ),
      ),
    )

    /*
     * A supplementary set of sentences to produce further complex
     * test cases against sentenceForSequencesFirst
     * 21 and 0 will need sorting by end date to end in the correct order
     * - { indexSentence 21: sentencesInSequence: {21=[22, 23], 22=[24, 25]} These will be delivered out of order, 24 and 25 need sorting by end date
     * - { indexSentence: 0, sentencesInSequence: null } Same sentence sequence as previous booking, but should appear differently
     */
    val sentencesForSequencesSecond = listOf(
      Sentence(
        bookingId = alternativeBookingId,
        sentenceSequence = 0,
        consecutiveToSequence = null,
        courtDescription = alternativeBookingCourt,
        sentenceEndDate = testDate.plusDays(1),
      ),

      Sentence(
        bookingId = alternativeBookingId,
        sentenceSequence = 25,
        consecutiveToSequence = 22,
        sentenceEndDate = testDate.plusDays(1),
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
        sentenceEndDate = testDate.plusDays(10),
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
        sentenceEndDate = testDate.plusDays(10),
      ),
    )

    // sentenceSequence: 21, 22, 23, 24, 25: delivered out of order but handled
    // Expect to be sorted after index 0 due to end date
    val expectedSentenceSequenceF = SentenceSequence(
      indexSentence = sentencesForSequencesSecond[3],
      sentencesInSequence = mutableMapOf(
        sentencesForSequencesSecond[3].sentenceSequence!! to listOf(
          sentencesForSequencesSecond[2],
          sentencesForSequencesSecond[4],
        ),
        sentencesForSequencesSecond[2].sentenceSequence!! to listOf(
          sentencesForSequencesSecond[5],
          sentencesForSequencesSecond[1],
        ),
      ),
    )

    // sentenceSequence 0: stand alone, same sentence sequence as previous but unique booking
    // Expect to be sorted after before 21 due to end date
    val expectedSentenceSequenceG = SentenceSequence(
      indexSentence = sentencesForSequencesSecond[0],
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
          null,
          sentencesForSequencesFirst,
        ),
        PrisonPeriodInfo(
          null,
          null,
          sentencesForSequencesSecond,
        ),
      ),
    )

    // then
    assertThat(actualSentences).containsExactlyElementsOf(expectedSentenceSequences)
  }
}
