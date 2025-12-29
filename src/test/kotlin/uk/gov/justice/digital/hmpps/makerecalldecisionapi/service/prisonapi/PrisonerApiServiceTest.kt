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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Movement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.PhysicalAttributes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.PrisonPeriod
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.PrisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.SentenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.SentenceOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.prisonPeriod
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.prisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentence
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
            sentenceSequence = 1,
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

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable { mock(Offender::class.java) },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    val offences = result.get(0).indexSentence.offences
    assertThat(offences[0].offenceDescription).isEqualTo("ABC")
    assertThat(offences[1].offenceDescription).isEqualTo("DEF")
    assertThat(offences[2].offenceDescription).isEqualTo("GHI")
    assertThat(offences[3].offenceDescription).isEqualTo("NMO")

    // Mockito checks there are no unused stubs by default, but it doesn't
    // check the number of calls each stub receives, so we verify this
    // explicitly (verify checks a stub was called exactly once by default).
    verify(prisonApiClient).retrieveOffender(nomsId)
  }

  @Test
  fun `Retrieve offences - prison movements which refer to prisonIds referencing inactive prisons are catered for`() {
    val nomsId = "AB234A"

    val prisonTimelineResponse = mock(PrisonTimelineResponse::class.java)
    val releaseFromPrisonId = "MDI"
    given(prisonTimelineResponse.prisonPeriod).willReturn(
      listOf(
        PrisonPeriod(
          bookingId = 123,
          movementDates = listOf(
            Movement(releaseFromPrisonId = releaseFromPrisonId),
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
            sentenceSequence = 1,

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

    given(prisonApiClient.retrieveAgency(releaseFromPrisonId))
      .willThrow(NotFoundException("Prison api returned agency not found for agency id MDI"))

    val offenderResponse = mock(Offender::class.java)

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable {
        offenderResponse
      },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    val offences = result[0].indexSentence.offences
    assertThat(offences.size).isGreaterThan(0)
    assertThat(offences[0].offenceDescription).isEqualTo("ABC")
    assertThat(result[0].indexSentence.releasingPrison).isNull()

    // Mockito checks there are no unused stubs by default, but it doesn't
    // check the number of calls each stub receives, so we verify this
    // explicitly (verify checks a stub was called exactly once by default).
    verify(prisonApiClient).retrieveOffender(nomsId)
    verify(prisonApiClient).retrieveAgency(releaseFromPrisonId)
  }

  @Test
  fun `Retrieve offences - call retrieve sentences with additional details`() {
    val nomsId = "AB234A"
    val referenceDate = LocalDateTime.now()

    val prisonTimelineResponse = mock(PrisonTimelineResponse::class.java)
    val lastPrisonReleasedFromId = "A1234"
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
              releaseFromPrisonId = lastPrisonReleasedFromId,
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
            sentenceSequence = 1,
            courtDescription = "DEF",
            sentenceDate = LocalDate.now().minusMonths(3).plusDays(2),
            sentenceEndDate = LocalDate.now(),
          ),
        )
      },
    )

    given(prisonApiClient.retrieveAgency(lastPrisonReleasedFromId)).willReturn(
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

    // Mockito checks there are no unused stubs by default, but it doesn't
    // check the number of calls each stub receives, so we verify this
    // explicitly (verify checks a stub was called exactly once by default).
    verify(prisonApiClient).retrieveOffender(nomsId)
    verify(prisonApiClient).retrieveAgency(lastPrisonReleasedFromId)
  }

  @Test
  fun `Retrieve offences - retrieve prison details once per period`() {
    val nomsId = "A1234"

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable { offender() },
    )

    val lastPrisonReleasedFromId = randomString()
    val secondLastPrisonReleasedFromId = randomString()
    val prisonTimelineResponse = prisonTimelineResponse(
      prisonPeriod =
      listOf(
        prisonPeriod(
          bookingId = defaultBookingId,
          movementDates = listOf(Movement(releaseFromPrisonId = lastPrisonReleasedFromId)),
        ),
        prisonPeriod(
          bookingId = alternativeBookingId,
          movementDates = listOf(Movement(releaseFromPrisonId = secondLastPrisonReleasedFromId)),
        ),
      ),
    )

    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable { prisonTimelineResponse },
    )
    given(prisonApiClient.retrieveSentencesAndOffences(defaultBookingId)).willReturn(
      Mono.fromCallable { listOf(sentence(), sentence(), sentence()) },
    )
    given(prisonApiClient.retrieveSentencesAndOffences(alternativeBookingId)).willReturn(
      Mono.fromCallable { listOf(sentence(), sentence(), sentence()) },
    )
    given(prisonApiClient.retrieveAgency(lastPrisonReleasedFromId)).willReturn(
      Mono.fromCallable { agency() },
    )
    given(prisonApiClient.retrieveAgency(secondLastPrisonReleasedFromId)).willReturn(
      Mono.fromCallable { agency() },
    )

    prisonerApiService.retrieveOffences(nomsId)

    // Mockito checks there are no unused stubs by default, but it doesn't
    // check the number of calls each stub receives, so we verify this
    // explicitly (verify checks a stub was called exactly once by default).
    verify(prisonApiClient).retrieveAgency(lastPrisonReleasedFromId)
    verify(prisonApiClient).retrieveAgency(secondLastPrisonReleasedFromId)
  }

  @Test
  fun `Retrieve offences - Given a series of sentences, sentence sequences are correctly ordered with consecutive and concurrent sentence`() {
    val nomsId = "A1234"

    val prisonTimelineResponse = mock(PrisonTimelineResponse::class.java)
    given(prisonTimelineResponse.prisonPeriod).willReturn(
      listOf(
        PrisonPeriod(bookingId = defaultBookingId),
        PrisonPeriod(bookingId = alternativeBookingId),
      ),
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

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
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

    // Mockito checks there are no unused stubs by default, but it doesn't
    // check the number of calls each stub receives, so we verify this
    // explicitly (verify checks a stub was called exactly once by default).
    verify(prisonApiClient).retrieveOffender(nomsId)
  }

  //endregion

  // region Test Data -
  val defaultBookingId = 123
  val alternativeBookingId = 987
  val defaultBookingCourt = "First Booking Court"
  val alternativeBookingCourt = "Second Booking Court"
  val testDate = LocalDate.now()

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
      courtDescription = "Second Booking Court",
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

  // endregion
}
