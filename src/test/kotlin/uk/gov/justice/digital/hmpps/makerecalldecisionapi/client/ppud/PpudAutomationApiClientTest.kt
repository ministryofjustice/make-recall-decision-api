package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ppud

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.Counter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateSentenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ppudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ppudCreateSentenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.Duration
import java.util.function.Supplier

private const val TIMEOUT_IN_SECONDS = 15L

private const val EXPECTED_TIMEOUT_EXCEPTION_MESSAGE =
  "PPUD Automation API Client: [No response within $TIMEOUT_IN_SECONDS seconds]"

@ExtendWith(MockitoExtension::class)
class PpudAutomationApiClientTest {

  // We can't use @InjectMocks, as the PpudAutomationApiClient has a primitive value
  // as one of its constructor's parameters and primitive values cannot be mocked
  lateinit var ppudAutomationApiClient: PpudAutomationApiClient

  @Mock
  lateinit var webClient: WebClient

  @Mock
  lateinit var timeoutCounter: Counter

  @Mock
  lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun setup() {
    ppudAutomationApiClient = PpudAutomationApiClient(webClient, TIMEOUT_IN_SECONDS, timeoutCounter, objectMapper)
  }

  @Test
  fun `creates a new sentence`() {
    val offenderId = randomString()
    val url = "/offender/$offenderId/sentence"
    val request = ppudCreateOrUpdateSentenceRequest()
    val responseTypeReferenceObject = object : ParameterizedTypeReference<PpudCreateSentenceResponse>() {}
    val expectedResponse = ppudCreateSentenceResponse()

    sendsPostMessageSuccessfully(url, responseTypeReferenceObject, expectedResponse, offenderId, request)
  }

  @Test
  fun `handles timeout exceptions raised when creating a new sentence`() {
    val offenderId = randomString()
    val url = "/offender/$offenderId/sentence"
    val request = ppudCreateOrUpdateSentenceRequest()
    val responseTypeReferenceObject = object : ParameterizedTypeReference<PpudCreateSentenceResponse>() {}
    val createSentenceEndpointCall = { ppudAutomationApiClient.createSentence(offenderId, request) }

    handlesTimeoutExceptionWhenMakingPostCall(url, responseTypeReferenceObject, createSentenceEndpointCall)
  }

  @Test
  fun `updates an existing sentence`() {
    val offenderId = randomString()
    val sentenceId = randomString()
    val url = "/offender/$offenderId/sentence/$sentenceId"
    val request = ppudCreateOrUpdateSentenceRequest()

    val expectedResponse = mockSuccessfulWebClientPutCall(url)

    val actualResponse = ppudAutomationApiClient.updateSentence(offenderId, sentenceId, request)

    assertThat(actualResponse.block()).isEqualTo(expectedResponse.block())
  }

  @Test
  fun `handles timeout exceptions raised when updating a sentence`() {
    val offenderId = randomString()
    val sentenceId = randomString()
    val url = "/offender/$offenderId/sentence/$sentenceId"
    val request = ppudCreateOrUpdateSentenceRequest()
    val updateSentenceEndpointCall = { ppudAutomationApiClient.updateSentence(offenderId, sentenceId, request) }

    handlesTimeoutExceptionWhenMakingPutCall(url, updateSentenceEndpointCall)
  }

  private fun sendsPostMessageSuccessfully(
    url: String,
    responseTypeReferenceObject: ParameterizedTypeReference<PpudCreateSentenceResponse>,
    expectedResponse: PpudCreateSentenceResponse,
    offenderId: String,
    request: PpudCreateOrUpdateSentenceRequest,
  ) {
    mockSuccessfulWebClientPostCall(url, responseTypeReferenceObject, expectedResponse)

    val actualResponse = ppudAutomationApiClient.createSentence(offenderId, request)

    assertThat(actualResponse.block()).isEqualTo(expectedResponse)
  }

  private fun <ResponseType> mockSuccessfulWebClientPostCall(
    uri: String,
    responseTypeReferenceObject: ParameterizedTypeReference<ResponseType>,
    response: ResponseType,
  ) {
    val responseSpec = mockWebClientPostCall(uri)
    whenever(responseSpec.bodyToMono(eq(responseTypeReferenceObject))).thenReturn(Mono.just(response))
  }

  private fun handlesTimeoutExceptionWhenMakingPostCall(
    url: String,
    responseTypeReferenceObject: ParameterizedTypeReference<PpudCreateSentenceResponse>,
    ppudAutomationEndpointCall: Supplier<Mono<*>>,
  ) {
    mockTimeoutWebClientPostCall(url, responseTypeReferenceObject)

    StepVerifier.withVirtualTime(ppudAutomationEndpointCall)
      .expectSubscription()
      .thenAwait(Duration.ofSeconds(TIMEOUT_IN_SECONDS * 2)) // we wait twice because the service
      .thenAwait(Duration.ofSeconds(TIMEOUT_IN_SECONDS * 2)) // will retry once on failure
      .expectErrorMatches { exception ->
        exception is ClientTimeoutException &&
          exception.message == EXPECTED_TIMEOUT_EXCEPTION_MESSAGE
      }
      .verify()
  }

  private fun <ResponseType> mockTimeoutWebClientPostCall(
    uri: String,
    responseTypeReferenceObject: ParameterizedTypeReference<ResponseType>,
  ) {
    val responseSpec = mockWebClientPostCall(uri)
    whenever(responseSpec.bodyToMono(eq(responseTypeReferenceObject))).thenReturn(Mono.never())
  }

  private fun mockWebClientPostCall(uri: String): ResponseSpec {
    val requestBodyUriSpec = mock<RequestBodyUriSpec>()
    whenever(webClient.post()).thenReturn(requestBodyUriSpec)
    val requestBodySpec = mock<RequestBodySpec>()
    whenever(requestBodyUriSpec.uri(uri)).thenReturn(requestBodySpec)
    val headerRequestBodySpec = mock<RequestBodySpec>()
    whenever(requestBodySpec.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)).thenReturn(headerRequestBodySpec)
    val bodyRequestBodySpec = mock<RequestBodySpec>()
    // we can't match BodyInserters arguments, which is what we pass in
    // to the body method here, so we have to use any() instead :/
    whenever(headerRequestBodySpec.body(any())).thenReturn(bodyRequestBodySpec)
    val responseSpec = mock<ResponseSpec>()
    whenever(bodyRequestBodySpec.retrieve()).thenReturn(responseSpec)
    return responseSpec
  }

  private fun handlesTimeoutExceptionWhenMakingPutCall(
    url: String,
    ppudAutomationEndpointCall: Supplier<Mono<*>>,
  ) {
    mockTimeoutWebClientPutCall(url)

    StepVerifier.withVirtualTime(ppudAutomationEndpointCall)
      .expectSubscription()
      .thenAwait(Duration.ofSeconds(TIMEOUT_IN_SECONDS * 2)) // we wait twice because the service
      .thenAwait(Duration.ofSeconds(TIMEOUT_IN_SECONDS * 2)) // will retry once on failure
      .expectErrorMatches { exception ->
        exception is ClientTimeoutException &&
          exception.message == EXPECTED_TIMEOUT_EXCEPTION_MESSAGE
      }
      .verify()
  }

  private fun mockSuccessfulWebClientPutCall(uri: String): Mono<ResponseEntity<Void>> {
    val responseSpec = mockWebClientPutCall(uri)
    val emptyResponse = Mono.just(ResponseEntity<Void>(HttpStatus.OK))
    whenever(responseSpec.toBodilessEntity()).thenReturn(emptyResponse)
    return emptyResponse
  }

  private fun mockTimeoutWebClientPutCall(uri: String) {
    val responseSpec = mockWebClientPutCall(uri)
    whenever(responseSpec.toBodilessEntity()).thenReturn(Mono.never())
  }

  private fun mockWebClientPutCall(uri: String): ResponseSpec {
    val requestBodyUriSpec = mock<RequestBodyUriSpec>()
    whenever(webClient.put()).thenReturn(requestBodyUriSpec)
    val requestBodySpec = mock<RequestBodySpec>()
    whenever(requestBodyUriSpec.uri(uri)).thenReturn(requestBodySpec)
    val headerRequestBodySpec = mock<RequestBodySpec>()
    whenever(requestBodySpec.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)).thenReturn(headerRequestBodySpec)
    val bodyRequestBodySpec = mock<RequestBodySpec>()
    // we can't match BodyInserters arguments, which is what we pass in
    // to the body method here, so we have to use any() instead :/
    whenever(headerRequestBodySpec.body(any())).thenReturn(bodyRequestBodySpec)
    val responseSpec = mock<ResponseSpec>()
    whenever(bodyRequestBodySpec.retrieve()).thenReturn(responseSpec)
    return responseSpec
  }
}
