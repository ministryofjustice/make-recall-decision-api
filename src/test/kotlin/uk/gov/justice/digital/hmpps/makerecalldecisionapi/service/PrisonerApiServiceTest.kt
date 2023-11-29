package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonPeriod
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Sentence
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PrisonerApiServiceTest : ServiceTestBase() {

  @Test
  fun `call retrieve offender`() {
    val nomsId = "AB234A"

    val response = mock(PrisonOffenderSearchResponse::class.java)

    given(prisonApiClient.retrieveOffender(any())).willReturn(
      Mono.fromCallable {
        response
      },
    )

    val headers = HttpHeaders()
    headers.put("Content-Type", listOf("image/jpeg"))

    given(prisonApiClient.retrieveImageData(any()))
      .willReturn(
        Mono.fromCallable {
          ResponseEntity(ByteArrayResource("data".toByteArray()), headers, HttpStatus.OK)
        },
      )

    val result = PrisonerApiService(prisonApiClient).searchPrisonApi(nomsId)

    assertThat(result).isEqualTo(response)

    verify(result).image = "data:image/jpeg;base64,ZGF0YQ=="
  }

  @Test
  fun `call retrieve sentences`() {
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
            sentenceEndDate = LocalDate.now()
              .minusDays(1), // sentence date is in the past and so this result should not be returned.
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
        )
      },
    )

    val result = PrisonerApiService(prisonApiClient).retrieveOffences(nomsId)

    assertThat(result.size).isEqualTo(4)

    // expect results to be ordered by sentence end date and then court description.
    assertThat(result[0].bookingId).isEqualTo(123)
    assertThat(result[0].courtDescription).isEqualTo("ABC")
    assertThat(result[0].sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(5))

    assertThat(result[1].courtDescription).isEqualTo("ABC")
    assertThat(result[1].sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(2))

    assertThat(result[2].courtDescription).isEqualTo("DEF")
    assertThat(result[2].sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(2))

    assertThat(result[3].courtDescription).isEqualTo("ABC")
    assertThat(result[3].sentenceDate).isEqualTo(LocalDate.now().minusMonths(3).plusDays(1))
  }
}
