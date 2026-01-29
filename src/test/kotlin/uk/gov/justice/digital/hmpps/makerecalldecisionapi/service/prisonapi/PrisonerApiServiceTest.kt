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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.prisonPeriod
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.prisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.sentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.sentenceCalculationDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter.OffenceConverter
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter.OffenderMovementConverter
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter.PrisonPeriodInfo
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PrisonerApiServiceTest {

  @InjectMocks
  private lateinit var prisonerApiService: PrisonerApiService

  @Mock
  private lateinit var prisonApiClient: PrisonApiClient

  @Mock
  private lateinit var offenderMovementConverter: OffenderMovementConverter

  @Mock
  private lateinit var offenceConverter: OffenceConverter

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

  @Test
  fun `Retrieve offences - retrieve prison details and convert`() {
    val nomsId = "A1234"

    val offender = offender()
    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable { offender },
    )

    val firstPrisonPeriod = prisonPeriod()
    val lastPrisonReleasedFromIdForFirstPeriod = firstPrisonPeriod.movementDates[0].releaseFromPrisonId!!
    val secondPrisonPeriod = prisonPeriod()
    val lastPrisonReleasedFromIdForSecondPeriod = secondPrisonPeriod.movementDates[0].releaseFromPrisonId!!

    val prisonTimelineResponse = prisonTimelineResponse(
      prisonPeriod = listOf(firstPrisonPeriod, secondPrisonPeriod),
    )

    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable { prisonTimelineResponse },
    )

    val sentencesAndOffencesForFirstPeriod = listOf(sentence(), sentence(), sentence())
    given(prisonApiClient.retrieveSentencesAndOffences(firstPrisonPeriod.bookingId)).willReturn(
      Mono.fromCallable { sentencesAndOffencesForFirstPeriod },
    )
    val sentencesAndOffencesForSecondPeriod = listOf(sentence(), sentence(), sentence())
    given(prisonApiClient.retrieveSentencesAndOffences(secondPrisonPeriod.bookingId)).willReturn(
      Mono.fromCallable { sentencesAndOffencesForSecondPeriod },
    )

    val sentenceCalculationDatesForFirstPeriod = sentenceCalculationDates()
    given(prisonApiClient.bookingSentenceDetails(firstPrisonPeriod.bookingId)).willReturn(
      Mono.fromCallable { sentenceCalculationDatesForFirstPeriod },
    )
    val sentenceCalculationDatesForSecondPeriod = sentenceCalculationDates()
    given(prisonApiClient.bookingSentenceDetails(secondPrisonPeriod.bookingId)).willReturn(
      Mono.fromCallable { sentenceCalculationDatesForSecondPeriod },
    )

    val prisonForFirstPeriod = agency()
    given(prisonApiClient.retrieveAgency(lastPrisonReleasedFromIdForFirstPeriod)).willReturn(
      Mono.fromCallable { prisonForFirstPeriod },
    )
    val prisonForSecondPeriod = agency()
    given(prisonApiClient.retrieveAgency(lastPrisonReleasedFromIdForSecondPeriod)).willReturn(
      Mono.fromCallable { prisonForSecondPeriod },
    )

    val convertedSentences = listOf(sentenceSequence())
    given(
      offenceConverter.convert(
        offender,
        listOf(
          PrisonPeriodInfo(
            prisonDescription = prisonForFirstPeriod.longDescription,
            sentenceCalculationDatesForFirstPeriod,
            lastDateOutOfPrison = firstPrisonPeriod.movementDates[0].dateOutOfPrison,
            sentencesAndOffences = sentencesAndOffencesForFirstPeriod,
          ),
          PrisonPeriodInfo(
            prisonDescription = prisonForSecondPeriod.longDescription,
            sentenceCalculationDatesForSecondPeriod,
            lastDateOutOfPrison = secondPrisonPeriod.movementDates[0].dateOutOfPrison,
            sentencesAndOffences = sentencesAndOffencesForSecondPeriod,
          ),
        ),
      ),
    ).willReturn(convertedSentences)

    val actualSentenceSequences = prisonerApiService.retrieveOffences(nomsId)

    // Mockito checks there are no unused stubs by default, but it doesn't
    // check the number of calls each stub receives, so we verify this
    // explicitly (verify checks a stub was called exactly once by default).
    verify(prisonApiClient).retrieveOffender(nomsId)
    verify(prisonApiClient).retrievePrisonTimelines(nomsId)
    verify(prisonApiClient).retrieveSentencesAndOffences(firstPrisonPeriod.bookingId)
    verify(prisonApiClient).retrieveSentencesAndOffences(secondPrisonPeriod.bookingId)
    verify(prisonApiClient).retrieveAgency(lastPrisonReleasedFromIdForFirstPeriod)
    verify(prisonApiClient).retrieveAgency(lastPrisonReleasedFromIdForSecondPeriod)
    assertThat(actualSentenceSequences).isEqualTo(convertedSentences)
  }

  @Test
  fun `Retrieve offences - handles not finding the prison`() {
    val nomsId = "A1234"

    val offender = offender()
    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable { offender },
    )

    val prisonPeriod = prisonPeriod()
    val lastPrisonReleasedFromId = prisonPeriod.movementDates[0].releaseFromPrisonId!!

    val prisonTimelineResponse = prisonTimelineResponse(
      prisonPeriod = listOf(prisonPeriod),
    )

    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable { prisonTimelineResponse },
    )

    val sentencesAndOffences = listOf(sentence(), sentence(), sentence())
    given(prisonApiClient.retrieveSentencesAndOffences(prisonPeriod.bookingId)).willReturn(
      Mono.fromCallable { sentencesAndOffences },
    )

    val sentenceCalculationDates = sentenceCalculationDates()
    given(prisonApiClient.bookingSentenceDetails(prisonPeriod.bookingId)).willReturn(
      Mono.fromCallable { sentenceCalculationDates },
    )

    val notFoundErrorMessage = "Prison api returned agency not found for agency id $lastPrisonReleasedFromId"
    given(prisonApiClient.retrieveAgency(lastPrisonReleasedFromId))
      .willThrow(NotFoundException(notFoundErrorMessage))

    val convertedSentences = listOf(sentenceSequence())
    given(
      offenceConverter.convert(
        offender,
        listOf(
          PrisonPeriodInfo(
            prisonDescription = null,
            sentenceCalculationDates,
            lastDateOutOfPrison = prisonPeriod.movementDates[0].dateOutOfPrison,
            sentencesAndOffences = sentencesAndOffences,
          ),
        ),
      ),
    ).willReturn(convertedSentences)

    prisonerApiService.retrieveOffences(nomsId)

    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("Agency with id $lastPrisonReleasedFromId not found: $notFoundErrorMessage")
      }
    }
  }
}
