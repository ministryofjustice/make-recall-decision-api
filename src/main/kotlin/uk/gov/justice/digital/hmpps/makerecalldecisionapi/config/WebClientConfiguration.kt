package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import java.net.URI

@Configuration
class WebClientConfiguration(
  @Value("\${community.api.endpoint.url}") private val communityApiRootUri: String,
  @Value("\${offender.search.endpoint.url}") private val offenderSearchApiRootUri: String,
  @Value("\${arn.api.endpoint.url}") private val arnApiRootUri: String,
  @Value("\${gotenberg.endpoint.url}") private val gotenbergRootUri: String,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  @Autowired private val meterRegistry: MeterRegistry
) {

  @Bean
  fun webClientNoAuthNoMetrics(): WebClient {
    return WebClient.create()
  }

  @Bean
  fun authorizedClientManagerAppScope(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService
    )
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun offenderSearchWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder
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
  fun communityWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, communityApiRootUri, "community-api")
  }

  @Bean
  fun communityApiClient(@Qualifier("communityWebClientAppScope") webClient: WebClient): CommunityApiClient {
    return CommunityApiClient(webClient, nDeliusTimeout, communityApiClientTimeoutCounter())
  }

  @Bean
  fun communityApiClientTimeoutCounter(): Counter = timeoutCounter(communityApiRootUri)

  @Bean
  fun arnWebClientAppScope(
    @Qualifier(value = "authorizedClientManagerAppScope") authorizedClientManager: OAuth2AuthorizedClientManager,
    builder: WebClient.Builder
  ): WebClient {
    return getOAuthWebClient(authorizedClientManager, builder, arnApiRootUri, "arn-api")
  }

  @Bean
  fun arnApiClient(@Qualifier("arnWebClientAppScope") webClient: WebClient): ArnApiClient {
    return ArnApiClient(webClient, nDeliusTimeout, arnApiClientTimeoutCounter())
  }

  @Bean
  fun arnApiClientTimeoutCounter(): Counter = timeoutCounter(arnApiRootUri)

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
    registrationId: String
  ): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(registrationId)
    return builder.baseUrl(rootUri)
      .apply(oauth2Client.oauth2Configuration())
      .build()
  }

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
