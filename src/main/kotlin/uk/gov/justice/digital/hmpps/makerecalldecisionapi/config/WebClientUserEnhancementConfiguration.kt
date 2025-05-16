package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CvlApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.risk.ArnApiClient
import java.net.URI

@Configuration
class WebClientUserEnhancementConfiguration(
  @Value("\${arn.api.endpoint.url}") private val arnApiRootUri: String,
  @Value("\${cvl.api.endpoint.url}") private val cvlApiRootUri: String,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  @Value("\${oasys.arn.client.timeout}") private val arnTimeout: Long,
  @Value("\${cvl.client.timeout}") private val cvlTimeout: Long,
  @Autowired private val meterRegistry: MeterRegistry,
) {

  @Bean
  @RequestScope
  fun assessRisksNeedsWebClientUserEnhancedAppScope(builder: WebClient.Builder): WebClient {
    return builder.baseUrl(arnApiRootUri)
      .filter(ServletBearerExchangeFilterFunction())
      .build()
  }

  @Bean
  fun assessRisksNeedsApiClientUserEnhanced(@Qualifier("assessRisksNeedsWebClientUserEnhancedAppScope") webClient: WebClient): ArnApiClient {
    return ArnApiClient(webClient, arnTimeout, arnApiClientEnhancedTimeoutCounter())
  }

  @Bean
  fun arnApiClientEnhancedTimeoutCounter(): Counter = timeoutCounter(arnApiRootUri)

  @Bean
  @RequestScope
  fun cvlWebClientUserEnhancedAppScope(
    clientRegistrationRepository: ClientRegistrationRepository,
    builder: WebClient.Builder,
  ): WebClient {
    return getOAuthWebClient(
      authorizedClientManagerUserEnhanced(clientRegistrationRepository),
      builder,
      cvlApiRootUri,
      "cvl-api",
    )
  }

  @Bean
  fun cvlApiClientUserEnhanced(@Qualifier("cvlWebClientUserEnhancedAppScope") webClient: WebClient): CvlApiClient {
    return CvlApiClient(webClient, cvlTimeout, cvlApiClientUserEnhancedTimeoutCounter())
  }

  @Bean
  fun cvlApiClientUserEnhancedTimeoutCounter(): Counter = timeoutCounter(cvlApiRootUri)

  private fun authorizedClientManagerUserEnhanced(clients: ClientRegistrationRepository?): OAuth2AuthorizedClientManager {
    val service: OAuth2AuthorizedClientService = InMemoryOAuth2AuthorizedClientService(clients)
    val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, service)

    val defaultClientCredentialsTokenResponseClient = DefaultClientCredentialsTokenResponseClient()
    val authentication = SecurityContextHolder.getContext().authentication

    defaultClientCredentialsTokenResponseClient.setRequestEntityConverter { grantRequest: OAuth2ClientCredentialsGrantRequest ->
      val converter = CustomOAuth2ClientCredentialsGrantRequestEntityConverter()
      val username = authentication.name
      converter.enhanceWithUsername(grantRequest, username)
    }

    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials { clientCredentialsGrantBuilder: OAuth2AuthorizedClientProviderBuilder.ClientCredentialsGrantBuilder ->
        clientCredentialsGrantBuilder.accessTokenResponseClient(defaultClientCredentialsTokenResponseClient)
      }
      .build()

    manager.setAuthorizedClientProvider(authorizedClientProvider)
    return manager
  }

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

  private fun timeoutCounter(endpointUrl: String): Counter {
    val metricName = "http_client_requests_timeout"
    val host = URI(endpointUrl).host
    return meterRegistry.counter(metricName, Tags.of("clientName", host))
  }
}
