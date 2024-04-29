package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CvlApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DocumentManagementClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.net.URI
import java.time.Duration
import java.util.concurrent.TimeoutException

@Configuration
class WebClientConfiguration(
  @Value("\${delius.integration.endpoint.url}") private val deliusIntegrationRootUri: String,
  @Value("\${offender.search.endpoint.url}") private val offenderSearchApiRootUri: String,
  @Value("\${arn.api.endpoint.url}") private val arnApiRootUri: String,

  @Value("\${document-management.api.endpoint.url}") private val documentManagementRootUri: String,

  @Value("\${cvl.api.endpoint.url}") private val cvlApiRootUri: String,
  @Value("\${gotenberg.endpoint.url}") private val gotenbergRootUri: String,
  @Value("\${ppud-automation.api.endpoint.url}") private val ppudAutomationApiRootUri: String,
  @Value("\${prison.api.endpoint.url}") private val prisonRootUri: String,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  @Value("\${oasys.arn.client.timeout}") private val arnTimeout: Long,
  @Value("\${cvl.client.timeout}") private val cvlTimeout: Long,

  @Value("\${document-management.client.timeout}") private val documentManagementTimeout: Long,

  @Value("\${ppud-automation.client.timeout}") private val ppudAutomationTimeout: Long,
  @Value("\${prison.client.timeout}") private val prisonTimeout: Long,
  @Autowired private val meterRegistry: MeterRegistry,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun <T> Mono<T>.withRetry(): Mono<T> {
      return this
        .retryWhen(
          Retry.backoff(2, Duration.ofMillis(500))
            .filter(::shouldBeRetried)
            .doBeforeRetry(::logRetrySignal)
            .onRetryExhaustedThrow { _, retrySignal ->
              retrySignal.failure()
            },
        )
    }

    private fun logRetrySignal(retrySignal: Retry.RetrySignal) {
      val exception = retrySignal.failure()?.cause ?: retrySignal.failure()
      log.error(
        "Exception occurred but operation will be retried. Total retries: ${retrySignal.totalRetries()}",
        exception,
      )
    }

    private val transientStatusCodes: Set<Int> = setOf(
      // Client disconnect as reported by App Insights
      0,
      HttpStatus.REQUEST_TIMEOUT.value(),
      HttpStatus.BAD_GATEWAY.value(),
      HttpStatus.SERVICE_UNAVAILABLE.value(),
      HttpStatus.GATEWAY_TIMEOUT.value(),
      // Client disconnect as reported by Kibana
      499,
    )

    private fun shouldBeRetried(ex: Throwable): Boolean {
      return ex is ClientTimeoutException ||
        ex is TimeoutException ||
        ex is WebClientRequestException ||
        (ex is WebClientResponseException && transientStatusCodes.contains(ex.statusCode.value()))
    }
  }

  @Bean
  fun webClientNoAuthNoMetrics(): WebClient {
    return WebClient.create()
  }

  @Bean
  fun authorizedClientManagerAppScope(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
    )
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun offenderSearchWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, offenderSearchApiRootUri, "offender-search-api")
  }

  @Bean
  fun offenderSearchApiClient(@Qualifier("offenderSearchWebClientAppScope") webClient: WebClient): OffenderSearchApiClient {
    return OffenderSearchApiClient(webClient, nDeliusTimeout, offenderSearchApiClientTimeoutCounter())
  }

  @Bean
  fun offenderSearchApiClientTimeoutCounter(): Counter = timeoutCounter(offenderSearchApiRootUri)

  @Bean
  fun deliusWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, deliusIntegrationRootUri, "delius")
  }

  @Bean
  fun deliusClient(@Qualifier("deliusWebClientAppScope") webClient: WebClient): DeliusClient {
    return DeliusClient(webClient, nDeliusTimeout, deliusClientTimeoutCounter())
  }

  @Bean
  fun deliusClientTimeoutCounter(): Counter = timeoutCounter(deliusIntegrationRootUri)

  @Bean
  fun arnWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, arnApiRootUri, "arn-api")
  }

  @Bean
  fun arnApiClient(@Qualifier("arnWebClientAppScope") webClient: WebClient): ArnApiClient {
    return ArnApiClient(webClient, arnTimeout, arnApiClientTimeoutCounter())
  }

  @Bean
  fun arnApiClientTimeoutCounter(): Counter = timeoutCounter(arnApiRootUri)

  @Bean
  fun documentManagementWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, documentManagementRootUri, "document-management-api")
  }

  @Bean
  fun documentManagementApiClient(@Qualifier("documentManagementWebClientAppScope") webClient: WebClient): DocumentManagementClient {
    return DocumentManagementClient(webClient, documentManagementTimeout, documentManagementApiClientTimeoutCounter())
  }

  @Bean
  fun documentManagementApiClientTimeoutCounter(): Counter = timeoutCounter(documentManagementRootUri)

  @Bean
  fun cvlWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, cvlApiRootUri, "cvl-api")
  }

  @Bean
  fun cvlApiClient(@Qualifier("cvlWebClientAppScope") webClient: WebClient): CvlApiClient {
    return CvlApiClient(webClient, cvlTimeout, cvlApiClientTimeoutCounter())
  }

  @Bean
  fun cvlApiClientTimeoutCounter(): Counter = timeoutCounter(cvlApiRootUri)

  @Bean
  fun gotenbergClient(): WebClient {
    return getPlainWebClient(WebClient.builder(), gotenbergRootUri)
  }

  @Bean
  fun gotenbergClientTimeoutCounter(): Counter = timeoutCounter(gotenbergRootUri)

  private fun getOAuthWebClient(
    authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
    rootUri: String,
    registrationId: String,
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(registrationId)
    return builder.baseUrl(rootUri)
      .apply(oauth2Client.oauth2Configuration())
      .build()
  }

  @Bean
  fun ppudAutomationWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, ppudAutomationApiRootUri, "ppud-automation-api")
  }

  @Bean
  fun ppudAutomationApiClient(
    @Qualifier("ppudAutomationWebClientAppScope") webClient: WebClient,
    objectMapper: ObjectMapper,
  ): PpudAutomationApiClient {
    return PpudAutomationApiClient(
      webClient,
      ppudAutomationTimeout,
      ppudAutomationApiClientTimeoutCounter(),
      objectMapper,
    )
  }

  @Bean
  fun ppudAutomationApiClientTimeoutCounter(): Counter = timeoutCounter(ppudAutomationApiRootUri)

  @Bean
  fun prisonWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, prisonRootUri, "prison-api")
  }

  @Bean
  fun prisonApiClient(@Qualifier("prisonWebClientAppScope") webClient: WebClient): PrisonApiClient {
    return PrisonApiClient(webClient, prisonTimeout, prisonClientTimeoutCounter())
  }

  @Bean
  fun prisonClientTimeoutCounter(): Counter = timeoutCounter(prisonRootUri)

  private fun getPlainWebClient(
    builder: WebClient.Builder,
    rootUri: String,
  ): WebClient {
    return builder.baseUrl(rootUri)
      .build()
  }

  private fun timeoutCounter(endpointUrl: String): Counter {
    val metricName = "http_client_requests_timeout"
    val host = URI(endpointUrl).host
    return meterRegistry.counter(metricName, Tags.of("clientName", host))
  }
}
