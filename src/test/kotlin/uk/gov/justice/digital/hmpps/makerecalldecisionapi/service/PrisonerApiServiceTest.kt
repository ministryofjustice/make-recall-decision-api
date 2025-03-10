package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Movement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PhysicalAttributes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonPeriod
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceOffence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PrisonerApiServiceTest {

  @InjectMocks
  private lateinit var prisonerApiService: PrisonerApiService

  @Mock
  private lateinit var prisonApiClient: PrisonApiClient

  @Test
  fun `call retrieve offender`() {
    val nomsId = "AB234A"

    val response = Offender(
      agencyId = "KLN",
      facialImageId = 1,
    )

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val expectedAgencyDescription = "The Kyln"
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
      agencyId = "KLN",
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
  fun `call retrieve sentences and confirm ordering`() {
    val nomsId = "AB234A"

    val response = mock(PrisonTimelineResponse::class.java)

    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    given(response.prisonPeriod).willReturn(listOf(PrisonPeriod(bookingId = 123)))

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

    val offenderResponse = mock(Offender::class.java)

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable {
        offenderResponse
      },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    assertThat(result.size).isEqualTo(5)

    assertThat(result[0].courtDescription).isEqualTo("ABC")
    assertThat(result[0].sentenceEndDate).isNull()

    // expect results to be ordered by sentence end date and then court description.
    assertThat(result[1].bookingId).isEqualTo(123)
    assertThat(result[1].courtDescription).isEqualTo("ABC")
    assertThat(result[1].sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(5))

    assertThat(result[2].courtDescription).isEqualTo("ABC")
    assertThat(result[2].sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(2))

    assertThat(result[3].courtDescription).isEqualTo("DEF")
    assertThat(result[3].sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(2))

    assertThat(result[4].courtDescription).isEqualTo("ABC")
    assertThat(result[4].sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(1))
  }

  @Test
  fun `offences should be sorted`() {
    val nomsId = "AB234A"

    val response = mock(PrisonTimelineResponse::class.java)

    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    given(response.prisonPeriod).willReturn(listOf(PrisonPeriod(bookingId = 123)))

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

    val offenderResponse = mock(Offender::class.java)

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable {
        offenderResponse
      },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    val offences = result.get(0).offences
    assertThat(offences[0].offenceDescription).isEqualTo("ABC")
    assertThat(offences[1].offenceDescription).isEqualTo("DEF")
    assertThat(offences[2].offenceDescription).isEqualTo("GHI")
    assertThat(offences[3].offenceDescription).isEqualTo("NMO")
  }

  @Test
  fun `prison movements which refer to prisonIds referencing inactive prisons are catered for`() {
    val nomsId = "AB234A"

    val response = mock(PrisonTimelineResponse::class.java)

    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    given(response.prisonPeriod).willReturn(
      listOf(
        PrisonPeriod(
          bookingId = 123,
          movementDates = listOf(
            Movement(releaseFromPrisonId = "MDI"),
          ),
        ),
      ),
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

    val offences = result[0].offences
    assertThat(offences.size).isGreaterThan(0)
    assertThat(offences[0].offenceDescription).isEqualTo("ABC")
    assertThat(result[0].releasingPrison).isNull()
  }

  @Test
  fun `call retrieve sentences with additional details`() {
    val nomsId = "AB234A"
    val referenceDate = LocalDateTime.now()

    val response = mock(PrisonTimelineResponse::class.java)

    given(prisonApiClient.retrievePrisonTimelines(nomsId)).willReturn(
      Mono.fromCallable {
        response
      },
    )

    given(response.prisonPeriod).willReturn(
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
          longDescription = "Hogwarts",
        )
      },
    )

    given(prisonApiClient.retrieveOffender(nomsId)).willReturn(
      Mono.fromCallable {
        Offender(
          locationDescription = "",
          bookingNo = "",
          facialImageId = 0,
          firstName = "Robert",
          middleName = "A",
          lastName = "Buchanan",
          dateOfBirth = LocalDate.now().minusYears(45),
          agencyId = "KLN",
          status = "",
          physicalAttributes = PhysicalAttributes(gender = "M", ethnicity = "Caucasian"),
          identifiers = listOf(),
          sentenceDetail = SentenceDetail(licenceExpiryDate = LocalDate.now()),
        )
      },
    )

    val result = prisonerApiService.retrieveOffences(nomsId)

    assertThat(result.size).isEqualTo(1)

    assertThat(result[0].courtDescription).isEqualTo("DEF")
    assertThat(result[0].sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(2))
    assertThat(result[0].releasingPrison).isEqualTo("Hogwarts")
    assertThat(result[0].licenceExpiryDate).isEqualTo(LocalDate.now())
  }
}
