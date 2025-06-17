package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi

import ch.qos.logback.classic.Level
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.prisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Movement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PhysicalAttributes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonPeriod
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter.OffenderMovementConverter
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PrisonerApiServiceTest {

  @InjectMocks
  private lateinit var prisonerApiService: PrisonerApiService

  @Mock
  private lateinit var prisonApiClient: PrisonApiClient

  @Mock
  private lateinit var offenderMovementConverter: OffenderMovementConverter

  private val logAppender = findLogAppender(PrisonerApiService::class.java)

  @Test
  fun `call retrieve offender`() {
    val nomsId = "AB234A"

    val response = Offender(
      agencyId = "BRX",
      facialImageId = 1,
    )

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val expectedAgencyDescription = "HMP Brixton"
    given(prisonApiClient.retrieveAgency(response.agencyId!!)).willReturn(
      Mono.fromCallable {
        Agency(description = expectedAgencyDescription)
      },
    )

    val headers = HttpHeaders()
    headers.put("Content-Type", listOf("image/jpeg"))

    given(prisonApiClient.retrieveImageData(response.facialImageId.toString()))
      .willReturn(
        Mono.fromCallable {
          ResponseEntity(ByteArrayResource("data".toByteArray()), headers, HttpStatus.OK)
        },
      )

    val result = prisonerApiService.searchPrisonApi(nomsId)

    assertThat(result).isEqualTo(response)
    assertThat(result.agencyDescription).isEqualTo(expectedAgencyDescription)
    assertThat(result.image).isEqualTo("data:image/jpeg;base64,ZGF0YQ==")
  }

  @Test
  fun `call retrieve offender with no agency`() {
    val nomsId = "AB234A"

    val response = Offender(
      facialImageId = 1,
    )

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val headers = HttpHeaders()
    headers.put("Content-Type", listOf("image/jpeg"))

    given(prisonApiClient.retrieveImageData(response.facialImageId.toString()))
      .willReturn(
        Mono.fromCallable {
          ResponseEntity(ByteArrayResource("data".toByteArray()), headers, HttpStatus.OK)
        },
      )

    val result = prisonerApiService.searchPrisonApi(nomsId)

    assertThat(result).isEqualTo(response)
    assertThat(result.image).isEqualTo("data:image/jpeg;base64,ZGF0YQ==")
  }

  @Test
  fun `call retrieve offender with invalid agency`() {
    val nomsId = "AB234A"

    val response = Offender(
      agencyId = "BRX",
      facialImageId = 1,
    )

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    given(prisonApiClient.retrieveAgency(any())).willThrow(
      NotFoundException("Agency not found"),
    )

    val headers = HttpHeaders()
    headers.put("Content-Type", listOf("image/jpeg"))

    given(prisonApiClient.retrieveImageData(response.facialImageId.toString()))
      .willReturn(
        Mono.fromCallable {
          ResponseEntity(ByteArrayResource("data".toByteArray()), headers, HttpStatus.OK)
        },
      )

    val result = prisonerApiService.searchPrisonApi(nomsId)

    assertThat(result).isEqualTo(response)
    assertThat(result.image).isEqualTo("data:image/jpeg;base64,ZGF0YQ==")
  }

  @Test
  fun `call retrieve offender with no image`() {
    val nomsId = "AB234A"

    val response = mock(Offender::class.java)

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val result = prisonerApiService.searchPrisonApi(nomsId)

    assertThat(result).isEqualTo(response)

    verify(prisonApiClient).retrieveOffender(nomsId)

    verify(result).agencyId

    verify(result, times(2)).facialImageId

    verifyNoMoreInteractions(result)

    verifyNoMoreInteractions(prisonApiClient)
  }

  @Test
  fun `call retrieve offender with none-zero facialImageId but no matching image`() {
    val nomsId = "AB234A"

    val response = mock(Offender::class.java)

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    given(response.facialImageId).willReturn(1)

    `when`(prisonApiClient.retrieveImageData(any())).thenThrow(
      RuntimeException("Something went wrong"),
    )

    val result = prisonerApiService.searchPrisonApi(nomsId)

    assertThat(result).isEqualTo(response)

    assertThat(result.facialImageId).isEqualTo(1)

    verify(result).agencyId

    verify(prisonApiClient).retrieveOffender(nomsId)

    verifyNoMoreInteractions(result)

    verifyNoMoreInteractions(prisonApiClient)
  }

  @Test
  fun `gets offender movements`() {
    // given
    val nomsId = randomString()
    val prisonApiOffenderMovements = listOf(prisonApiOffenderMovement())
    given(prisonApiClient.retrieveOffenderMovements(nomsId)).willReturn(
      Mono.fromCallable { prisonApiOffenderMovements },
    )

    val expectedOffenderMovements =
      listOf(uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.offenderMovement())
    given(offenderMovementConverter.convert(prisonApiOffenderMovements)).willReturn(
      expectedOffenderMovements,
    )

    // when
    val actualOffenderMovements = prisonerApiService.getOffenderMovements(nomsId)

    // then
    assertThat(actualOffenderMovements).isEqualTo(expectedOffenderMovements)
    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("Searching for offender movements for offender with NOMIS ID $nomsId")
      }
    }
  }

  @Test
  fun `returns empty offender movements list if no response given`() {
    // given
    val nomsId = randomString()
    given(prisonApiClient.retrieveOffenderMovements(nomsId)).willReturn(
      Mono.empty(),
    )

    // when
    val actualOffenderMovements = prisonerApiService.getOffenderMovements(nomsId)

    // then
    assertThat(actualOffenderMovements).isEmpty()
    with(logAppender.list) {
      assertThat(size).isEqualTo(2)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("Searching for offender movements for offender with NOMIS ID $nomsId")
      }
      with(get(1)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("No movements found for offender with NOMIS ID $nomsId")
      }
    }
  }

  //region Retrieve Offences

  @Test
  fun `Retrieve offences - call retrieve sentences and confirm ordering`() {
    val nomsId = "AB234A"

    val prisonTimelineResponse = mock(PrisonTimelineResponse::class.java)
    given(prisonTimelineResponse.prisonPeriod).willReturn(
      listOf(PrisonPeriod(bookingId = 123)),
    )
    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable { prisonTimelineResponse },
    )

    given(prisonApiClient.retrieveSentencesAndOffences(123)).willReturn(
      Mono.fromCallable {
        listOf(
          Sentence(
            bookingId = 123,
            courtDescription = "DEF",
            sentenceDate = LocalDate.now().minusMonths(3).plusDays(2),
            sentenceEndDate = LocalDate.now(),
          ),
          Sentence(
            bookingId = 123,
            courtDescription = "GHI",
            sentenceDate = LocalDate.now().minusMonths(3).minusDays(1),
            // sentence date is in the past and so this result should not be returned.
            sentenceEndDate = LocalDate.now()
              .minusDays(1),
          ),
          Sentence(
            bookingId = 123,
            courtDescription = "ABC",
            sentenceDate = LocalDate.now().minusMonths(3).plusDays(2),
            sentenceEndDate = LocalDate.now().plusDays(1),
          ),
          Sentence(
            bookingId = 123,
            courtDescription = "ABC",
            sentenceDate = LocalDate.now().minusMonths(3).plusDays(1),
            sentenceEndDate = LocalDate.now().plusDays(1),
          ),
          Sentence(
            bookingId = 123,
            courtDescription = "ABC",
            sentenceDate = LocalDate.now().minusMonths(3).plusDays(5),
            sentenceEndDate = LocalDate.now().plusDays(1),
          ),
          Sentence(
            bookingId = 123,
            courtDescription = "ABC",
            sentenceDate = LocalDate.now().minusMonths(3).plusDays(5),
            sentenceEndDate = null,
          ),
        )
      },
    )

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable {
        mock(Offender::class.java)
      },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    assertThat(result.size).isEqualTo(5)

    assertThat(result[0].indexSentence.courtDescription).isEqualTo("ABC")
    assertThat(result[0].indexSentence.sentenceEndDate).isNull()

    // expect results to be ordered by sentence end date and then court description.
    assertThat(result[1].indexSentence.bookingId).isEqualTo(123)
    assertThat(result[1].indexSentence.courtDescription).isEqualTo("ABC")
    assertThat(result[1].indexSentence.sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(5))

    assertThat(result[2].indexSentence.courtDescription).isEqualTo("ABC")
    assertThat(result[2].indexSentence.sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(2))

    assertThat(result[3].indexSentence.courtDescription).isEqualTo("DEF")
    assertThat(result[3].indexSentence.sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(2))

    assertThat(result[4].indexSentence.courtDescription).isEqualTo("ABC")
    assertThat(result[4].indexSentence.sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(1))
  }

  @Test
  fun `Retrieve offences - offences should be sorted`() {
    val nomsId = "AB234A"

    val prisonTimelineResponse = mock(PrisonTimelineResponse::class.java)
    given(prisonTimelineResponse.prisonPeriod).willReturn(listOf(PrisonPeriod(bookingId = 123)))
    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable { prisonTimelineResponse },
    )

    given(prisonApiClient.retrieveSentencesAndOffences(123)).willReturn(
      Mono.fromCallable {
        listOf(
          Sentence(
            bookingId = 123,
            courtDescription = "DEF",
            sentenceDate = LocalDate.now().minusMonths(3).plusDays(2),
            sentenceEndDate = LocalDate.now(),
            offences = listOf(
              SentenceOffence(offenceDescription = "DEF"),
              SentenceOffence(offenceDescription = "ABC"),
              SentenceOffence(offenceDescription = "NMO"),
              SentenceOffence(offenceDescription = "GHI"),
            ),
          ),
        )
      },
    )

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable { mock(Offender::class.java) },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    val offences = result.get(0).indexSentence.offences
    assertThat(offences[0].offenceDescription).isEqualTo("ABC")
    assertThat(offences[1].offenceDescription).isEqualTo("DEF")
    assertThat(offences[2].offenceDescription).isEqualTo("GHI")
    assertThat(offences[3].offenceDescription).isEqualTo("NMO")
  }

  @Test
  fun `Retrieve offences - prison movements which refer to prisonIds referencing inactive prisons are catered for`() {
    val nomsId = "AB234A"

    val prisonTimelineResponse = mock(PrisonTimelineResponse::class.java)
    given(prisonTimelineResponse.prisonPeriod).willReturn(
      listOf(
        PrisonPeriod(
          bookingId = 123,
          movementDates = listOf(
            Movement(releaseFromPrisonId = "MDI"),
          ),
        ),
      ),
    )
    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable { prisonTimelineResponse },
    )

    given(prisonApiClient.retrieveSentencesAndOffences(123)).willReturn(
      Mono.fromCallable {
        listOf(
          Sentence(
            bookingId = 123,
            courtDescription = "DEF",
            sentenceDate = LocalDate.now().minusMonths(3).plusDays(2),
            sentenceEndDate = LocalDate.now(),

            offences = listOf(
              SentenceOffence(offenceDescription = "DEF"),
              SentenceOffence(offenceDescription = "ABC"),
              SentenceOffence(offenceDescription = "NMO"),
              SentenceOffence(offenceDescription = "GHI"),
            ),
          ),
        )
      },
    )

    given(prisonApiClient.retrieveAgency("MDI"))
      .willThrow(NotFoundException("Prison api returned agency not found for agency id MDI"))

    val offenderResponse = mock(Offender::class.java)

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable {
        offenderResponse
      },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    val offences = result[0].indexSentence.offences
    assertThat(offences.size).isGreaterThan(0)
    assertThat(offences[0].offenceDescription).isEqualTo("ABC")
    assertThat(result[0].indexSentence.releasingPrison).isNull()
  }

  @Test
  fun `Retrieve offences - call retrieve sentences with additional details`() {
    val nomsId = "AB234A"
    val referenceDate = LocalDateTime.now()

    val prisonTimelineResponse = mock(PrisonTimelineResponse::class.java)
    given(prisonTimelineResponse.prisonPeriod).willReturn(
      listOf(
        PrisonPeriod(
          bookingId = 123,
          movementDates = listOf(
            Movement(
              dateOutOfPrison = referenceDate.minusDays(5),
              releaseFromPrisonId = "B1234",
            ),
            Movement(
              dateOutOfPrison = referenceDate.minusDays(4),
              releaseFromPrisonId = "A1234",
            ),
          ),
        ),
      ),
    )
    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable { prisonTimelineResponse },
    )

    given(prisonApiClient.retrieveSentencesAndOffences(123)).willReturn(
      Mono.fromCallable {
        listOf(
          Sentence(
            bookingId = 123,
            courtDescription = "DEF",
            sentenceDate = LocalDate.now().minusMonths(3).plusDays(2),
            sentenceEndDate = LocalDate.now(),
          ),
        )
      },
    )

    given(prisonApiClient.retrieveAgency("A1234")).willReturn(
      Mono.fromCallable {
        Agency(
          longDescription = "Prison A1234",
        )
      },
    )

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable {
        Offender(
          locationDescription = "",
          bookingNo = "",
          facialImageId = 0,
          firstName = "Joe",
          middleName = "A",
          lastName = "Bloggs",
          dateOfBirth = LocalDate.now().minusYears(45),
          agencyId = "BRX",
          status = "",
          physicalAttributes = PhysicalAttributes(gender = "M", ethnicity = "White"),
          identifiers = listOf(),
          sentenceDetail = SentenceDetail(licenceExpiryDate = LocalDate.now()),
        )
      },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    assertThat(result.size).isEqualTo(1)

    assertThat(result[0].indexSentence.courtDescription).isEqualTo("DEF")
    assertThat(result[0].indexSentence.sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(2))
    assertThat(result[0].indexSentence.releasingPrison).isEqualTo("Prison A1234")
    assertThat(result[0].indexSentence.licenceExpiryDate).isEqualTo(LocalDate.now())
  }

  @Test
  fun `Retrieve offences - Given a series of sentences, sentence sequences are correctly calculated with consecutive and concurrent sentence`() {
    val nomsId = "A1234"

    val prisonTimelineResponse = mock(PrisonTimelineResponse::class.java)
    given(prisonTimelineResponse.prisonPeriod).willReturn(
      listOf(PrisonPeriod(bookingId = defaultBookingId), PrisonPeriod(bookingId = alternativeBookingId)),
    )
    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable { prisonTimelineResponse },
    )

    given(prisonApiClient.retrieveSentencesAndOffences(defaultBookingId)).willReturn(
      Mono.fromCallable { sentencesForSequencesFirst },
    )
    given(prisonApiClient.retrieveSentencesAndOffences(alternativeBookingId)).willReturn(
      Mono.fromCallable { sentencesForSequencesSecond },
    )

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable { mock(Offender::class.java) },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    assertThat(result).hasSize(7)

    assertThat(result[0]).isEqualTo(expectedSentenceSequenceA)
    assertThat(result[1]).isEqualTo(expectedSentenceSequenceB)
    assertThat(result[2]).isEqualTo(expectedSentenceSequenceC)
    assertThat(result[3]).isEqualTo(expectedSentenceSequenceD)
    assertThat(result[4]).isEqualTo(expectedSentenceSequenceE)

    assertThat(result[5]).isEqualTo(expectedSentenceSequenceF)
    assertThat(result[6]).isEqualTo(expectedSentenceSequenceG)
  }

  //endregion

  // region Test Data -
  val defaultBookingId = 123
  val alternativeBookingId = 987

  /* This set of sentences is to produce the following SentenceSequences of increasing complexity
   * - { indexSentence: 0, sentencesInSequence: null } Single sentence
   * - { indexSentence: 1, sentencesInSequence: {1=[2]} } Sentence with single consecutive
   * - { indexSentence: 3, sentencesInSequence: {3=[4], 4=[5]} } Sentence with multiple consecutive
   * - { indexSentence: 6, sentencesInSequence: {6=[7, 8]} } Sentence with single concurrent consecutive set
   * - { indexSentence: 9, sentencesInSequence: {9=[10, 11], 10=[12, 13]} } Sentence with multiple concurrent consecutive sets
   */
  val sentencesForSequencesFirst = listOf(
    Sentence(
      bookingId = defaultBookingId,
      sentenceSequence = 0,
      consecutiveToSequence = null,
      courtDescription = "First Booking Court",
    ),
    Sentence(
      bookingId = defaultBookingId,
      sentenceSequence = 1,
      consecutiveToSequence = null,
    ),
    Sentence(
      bookingId = defaultBookingId,
      sentenceSequence = 2,
      consecutiveToSequence = 1,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 3,
      consecutiveToSequence = null,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 4,
      consecutiveToSequence = 3,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 5,
      consecutiveToSequence = 4,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 6,
      consecutiveToSequence = null,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 7,
      consecutiveToSequence = 6,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 8,
      consecutiveToSequence = 6,
    ),

    Sentence(
      defaultBookingId,
      sentenceSequence = 9,
      consecutiveToSequence = null,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 10,
      consecutiveToSequence = 9,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 11,
      consecutiveToSequence = 9,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 12,
      consecutiveToSequence = 10,
    ),
    Sentence(
      defaultBookingId,
      sentenceSequence = 13,
      consecutiveToSequence = 10,
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
    sentencesInSequence = mapOf(
      sentencesForSequencesFirst[1].sentenceSequence!! to listOf(sentencesForSequencesFirst[2]),
    ),
  )

  // sentenceSequence 3, 4, 5: sentence with a single consecutive followed by a single consecutive
  val expectedSentenceSequenceC = SentenceSequence(
    indexSentence = sentencesForSequencesFirst[3],
    sentencesInSequence = mapOf(
      sentencesForSequencesFirst[3].sentenceSequence!! to listOf(sentencesForSequencesFirst[4]),
      sentencesForSequencesFirst[4].sentenceSequence!! to listOf(sentencesForSequencesFirst[5]),
    ),
  )

  // sentenceSequence 6, 7, 8: sentence with a consecutively concurrents
  val expectedSentenceSequenceD = SentenceSequence(
    indexSentence = sentencesForSequencesFirst[6],
    sentencesInSequence = mapOf(
      sentencesForSequencesFirst[6].sentenceSequence!! to listOf(sentencesForSequencesFirst[7], sentencesForSequencesFirst[8]),
    ),
  )

  // sentenceSequence 9, 10, 11: sentence with multiple concurrents consecutive to each other
  val expectedSentenceSequenceE = SentenceSequence(
    indexSentence = sentencesForSequencesFirst[9],
    sentencesInSequence = mapOf(
      sentencesForSequencesFirst[9].sentenceSequence!! to listOf(sentencesForSequencesFirst[10], sentencesForSequencesFirst[11]),
      sentencesForSequencesFirst[10].sentenceSequence!! to listOf(sentencesForSequencesFirst[12], sentencesForSequencesFirst[13]),
    ),
  )

  /*
   * A supplementary set of sentences to produce further complex
   * test cases against sentenceForSequencesFirst
   * - { indexSentence: 0, sentencesInSequence: null } Same sentence sequence as previous booking, but should appear differently
   * - { indexSentence 21: sentencesInSequence: {21=[22, 23], 22=[24, 25]} These will be delivered out of order
   */
  val sentencesForSequencesSecond = listOf(
    Sentence(
      bookingId = alternativeBookingId,
      sentenceSequence = 0,
      consecutiveToSequence = null,
      courtDescription = "Second Booking Court",
    ),

    Sentence(
      bookingId = alternativeBookingId,
      sentenceSequence = 25,
      consecutiveToSequence = 22,
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
    ),
  )

  // sentenceSequence 0: stand alone, same sentence sequence as previous but unique booking
  val expectedSentenceSequenceF = SentenceSequence(
    indexSentence = sentencesForSequencesSecond[0],
    sentencesInSequence = null,
  )

  // sentenceSequence: 21, 22, 23, 24, 25: delivered out of order but handled
  val expectedSentenceSequenceG = SentenceSequence(
    indexSentence = sentencesForSequencesSecond[3],
    sentencesInSequence = mapOf(
      sentencesForSequencesSecond[3].sentenceSequence!! to listOf(sentencesForSequencesSecond[2], sentencesForSequencesSecond[4]),
      sentencesForSequencesSecond[2].sentenceSequence!! to listOf(sentencesForSequencesSecond[5], sentencesForSequencesSecond[1]),
    ),
  )

  // endregion
}
