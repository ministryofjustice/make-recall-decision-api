package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client;

import ch.qos.logback.classic.Level
import io.micrometer.core.instrument.Counter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.riskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import java.time.Duration
import java.util.function.Supplier

private const val timeoutInSeconds = 15L

@ExtendWith(MockitoExtension::class)
class ArnApiClientTest {

  // We can't use @InjectMockks, as the ArnApiClient has a primitive value as
  // one of its constructor's parameters and primitive values cannot be mocked
  lateinit var arnApiClient: ArnApiClient

  @Mock
  lateinit var webClient: WebClient;

  @Mock
  lateinit var timeoutCounter: Counter;

  private val logAppender = findLogAppender(ArnApiClient::class.java)

  @BeforeEach
  fun setup() {
    arnApiClient = ArnApiClient(webClient, timeoutInSeconds, timeoutCounter)
  }

  @Test
  fun `retrieves risk scores`() {
    val singleResponse = riskScoreResponse()
    val responseList = listOf(singleResponse)
    val responseTypeObject = object : ParameterizedTypeReference<List<RiskScoreResponse>>() {}
    val riskValueName = "risk scores"

    retrievesRiskValues(riskValueName, responseList, responseTypeObject)
  }

  @Test
  fun `handles timeout exceptions raised when retrieveing risk scores`() {
    val crn = randomString()
    val uri = "/risks/crn/$crn/predictors/all"
    val riskScoreCall = { arnApiClient.getRiskScores(crn) }

    handlesTimeoutExceptionWhenRetrievingValues(uri, "risk scores", riskScoreCall)
  }

  private fun <RiskValueType> retrievesRiskValues(
    riskValueName: String,
    responseList: RiskValueType,
    responseTypeObject: ParameterizedTypeReference<RiskValueType>,
  ) {
    val crn = randomString()

    val responseSpec = mockWebClientCall("/risks/crn/$crn/predictors/all")
    whenever(responseSpec.bodyToMono(eq(responseTypeObject))).thenReturn(
      Mono.just(responseList),
    )

    val actualResponse = arnApiClient.getRiskScores(crn)

    assertThat(actualResponse.block()).isEqualTo(responseList)
    with(logAppender.list) {
      assertThat(size).isEqualTo(2)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("About to get $riskValueName for $crn")
      }
      with(get(1)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("Returning $riskValueName for $crn")
      }
    }
  }

  private fun handlesTimeoutExceptionWhenRetrievingValues(
    uri: String,
    endpointName: String,
    arnEndpointCall: Supplier<Mono<*>>,
  ) {
    val responseSpec = mockWebClientCall(uri)
    val responseType = object : ParameterizedTypeReference<List<RiskScoreResponse>>() {}
    whenever(responseSpec.bodyToMono(eq(responseType))).thenReturn(
      Mono.never(),
    )

    StepVerifier
      .withVirtualTime(arnEndpointCall)
      .expectSubscription()
      .thenAwait(Duration.ofSeconds(timeoutInSeconds * 2)) // we wait twice because the service
      .thenAwait(Duration.ofSeconds(timeoutInSeconds * 2)) // will retry once on failure
      .expectErrorMatches { exception ->
        exception is ClientTimeoutException &&
          exception.message == expectedExceptionMessage(endpointName, timeoutInSeconds)
      }
      .verify()
  }

  private fun mockWebClientCall(uri: String): WebClient.ResponseSpec {
    val requestHeadersUriSpec = mock<RequestHeadersUriSpec<*>>()
    whenever(webClient.get()).thenReturn(requestHeadersUriSpec)
    val requestHeaderSpec = mock<WebClient.RequestHeadersSpec<*>>()
    whenever(requestHeadersUriSpec.uri(uri)).thenReturn(requestHeaderSpec)
    val responseSpec = mock<WebClient.ResponseSpec>()
    whenever(requestHeaderSpec.retrieve()).thenReturn(responseSpec)
    return responseSpec
  }

  private fun expectedExceptionMessage(endpointName: String, timeoutInSeconds: Long): String {
    return "ARN API Client - $endpointName endpoint: [No response within $timeoutInSeconds seconds]"
  }
}
