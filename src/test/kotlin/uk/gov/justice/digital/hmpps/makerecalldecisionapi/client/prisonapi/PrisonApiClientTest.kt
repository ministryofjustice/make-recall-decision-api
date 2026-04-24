package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi

import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import io.micrometer.core.instrument.Counter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.EndpointMocker.Companion.mockGetEndpointWithFailure
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.EndpointMocker.Companion.mockGetEndpointWithSuccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.prisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.toJsonString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.sentenceCalculationDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.toJsonString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutRuntimeException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

// we set a low value to prevent the timeout tests from taking too long
private const val TIMEOUT_IN_SECONDS = 1L

@ExtendWith(MockitoExtension::class)
class PrisonApiClientTest {

  companion object {
    // for debugging, add .notifier(ConsoleNotifier(true)) after .dynamicPort()
    @RegisterExtension
    val wiremock =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build()
  }

  lateinit var prisonApiClient: PrisonApiClient

  @Mock
  lateinit var timeoutCounter: Counter

  @BeforeEach
  fun setUpWebClient() {
    // Mocking the WebClient is quite cumbersome and hard to read. WireMock can start
    // up quickly enough to be used for unit tests instead
    val webClient = WebClient.builder().baseUrl("http://localhost:${wiremock.port}").build()
    prisonApiClient = PrisonApiClient(webClient, TIMEOUT_IN_SECONDS, timeoutCounter)
  }

  @AfterEach
  fun removeWiremockStubs() {
    wiremock.resetAll()
  }

  @Test
  fun `retrieves booking sentence details`() {
    // given
    val bookingId = randomInt()
    val response = sentenceCalculationDates()
    testSuccessCase(
      endpoint = bookingSentenceDetails(bookingId),
      responseJson = toJsonString(response),
      endpointCall = { prisonApiClient.bookingSentenceDetails(bookingId) },
      expectedResponse = response,
    )
  }

  @Test
  fun `handles timeout exceptions raised when retrieving booking sentence details`() {
    // given
    val bookingId = randomInt()
    testTimeoutCase(
      endpoint = bookingSentenceDetails(bookingId),
      endpointCall = { prisonApiClient.bookingSentenceDetails(bookingId) },
    )
  }

  @Test
  fun `handles not found exceptions raised when retrieving booking sentence details`() {
    // given
    val bookingId = randomInt()
    testNotFoundCase(
      endpoint = bookingSentenceDetails(bookingId),
      endpointCall = { prisonApiClient.bookingSentenceDetails(bookingId) },
      expectedErrorMessage = "Prison API found no sentence details for booking ID $bookingId",
    )
  }

  @Test
  fun `retrieves offender movements`() {
    val nomsId = randomString()
    val responseList = listOf(prisonApiOffenderMovement(), prisonApiOffenderMovement())
    testSuccessCase(
      endpoint = offenderMovementsEndpoint(nomsId),
      responseJson = responseList.joinToString(",", "[", "]") { it.toJsonString() },
      endpointCall = { prisonApiClient.retrieveOffenderMovements(nomsId) },
      expectedResponse = responseList,
    )
  }

  @Test
  fun `handles timeout exceptions raised when retrieving offender movements`() {
    // given
    val nomsId = randomString()
    testTimeoutCase(
      endpoint = offenderMovementsEndpoint(nomsId),
      endpointCall = { prisonApiClient.retrieveOffenderMovements(nomsId) },
    )
  }

  @Test
  fun `handles not found exceptions raised when retrieving offender movements`() {
    // given
    val nomsId = randomString()
    testNotFoundCase(
      endpoint = offenderMovementsEndpoint(nomsId),
      endpointCall = { prisonApiClient.retrieveOffenderMovements(nomsId) },
      expectedErrorMessage = "Prison API found no movements for NOMIS ID $nomsId",
    )
  }

  private fun bookingSentenceDetails(bookingId: Int) = "/api/bookings/$bookingId/sentenceDetail"

  private fun offenderMovementsEndpoint(nomsId: String) = "/api/movements/offender/$nomsId"

  fun <T> testSuccessCase(endpoint: String, responseJson: String, endpointCall: () -> Mono<T>, expectedResponse: T) {
    // given
    mockGetEndpointWithSuccess(
      endpoint,
      responseJson,
    )

    // when
    val actualResponse = endpointCall()

    // then
    assertThat(actualResponse.block()).isEqualTo(expectedResponse)
  }

  fun <T> testTimeoutCase(endpoint: String, endpointCall: () -> Mono<T>) {
    // given
    mockGetEndpointWithSuccess(
      endpoint,
      "",
      (TIMEOUT_IN_SECONDS * 2).toInt(),
    )

    // when then
    assertThatThrownBy {
      val actualResponse = endpointCall()
      actualResponse.block()
    }
      .isInstanceOf(ClientTimeoutRuntimeException::class.java)
      .hasMessage("Prison API Client: [No response within $TIMEOUT_IN_SECONDS seconds]")
    // we make 1 try + 2 retries = 3 timeoutCounter increments
    verify(timeoutCounter, times(3)).increment()
  }

  fun <T> testNotFoundCase(endpoint: String, endpointCall: () -> Mono<T>, expectedErrorMessage: String) {
    // given
    mockGetEndpointWithFailure(endpoint, NOT_FOUND)

    // when then
    assertThatThrownBy {
      val actualResponse = endpointCall()
      actualResponse.block()
    }
      .isInstanceOf(NotFoundException::class.java)
      .hasMessage(expectedErrorMessage)
  }
}
