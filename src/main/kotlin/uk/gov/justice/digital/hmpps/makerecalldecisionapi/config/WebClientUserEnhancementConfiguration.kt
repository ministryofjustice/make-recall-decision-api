package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.lang.Nullable
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import java.net.URI

//// TODO move these out
//@Component
//@Order(4)
//internal class UserContextFilter : Filter {
//  @Throws(IOException::class, ServletException::class)
//  override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
//    val httpServletRequest = servletRequest as HttpServletRequest
//    val authToken = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)
//    authToken?.let {
//      UserContext.setAuthToken(authToken)
//    }
//
//    filterChain.doFilter(httpServletRequest, servletResponse)
//  }
//
//  override fun init(filterConfig: FilterConfig) {}
//  override fun destroy() {}
//}
//
//@Component
//object UserContext {
//  var authToken = ThreadLocal<String>()
//
//  fun setAuthToken(aToken: String) {
//    authToken.set(aToken)
//  }
//
//  fun getAuthToken(): String {
//    return authToken.get()
//  }
//}

@Configuration
class WebClientUserEnhancementConfiguration(
  @Value("\${community.api.endpoint.url}") private val communityApiRootUri: String,
  @Value("\${offender.search.endpoint.url}") private val offenderSearchApiRootUri: String,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  @Autowired private val meterRegistry: MeterRegistry
) {

  @Bean
  @RequestScope
  fun offenderSearchWebClientUserEnhancedAppScope(
    clientRegistrationRepository: ClientRegistrationRepository,
    builder: WebClient.Builder
  ): WebClient {
    return getOAuthWebClient(authorizedClientManagerUserEnhanced(clientRegistrationRepository), builder, offenderSearchApiRootUri, "offender-search-api")
  }

  @Bean
  fun offenderSearchApiClientUserEnhanced(@Qualifier("offenderSearchWebClientUserEnhancedAppScope") webClient: WebClient): OffenderSearchApiClient {
    return OffenderSearchApiClient(webClient, nDeliusTimeout, offenderSearchApiClientTimeoutCounter2())
  }

  @Bean
  fun offenderSearchApiClientTimeoutCounter2(): Counter = timeoutCounter(offenderSearchApiRootUri)

  @Bean
  @RequestScope
  fun communityWebClientUserEnhancedAppScope(
    clientRegistrationRepository: ClientRegistrationRepository,
    builder: WebClient.Builder
  ): WebClient {
    return getOAuthWebClient(authorizedClientManagerUserEnhanced(clientRegistrationRepository), builder, communityApiRootUri, "community-api")
  }

  @Bean
  fun communityApiClientUserEnhanced(@Qualifier("communityWebClientUserEnhancedAppScope") webClient: WebClient): CommunityApiClient {
    return CommunityApiClient(webClient, nDeliusTimeout, communityApiClientUserEnhancedTimeoutCounter())
  }

  @Bean
  fun communityApiClientUserEnhancedTimeoutCounter(): Counter = timeoutCounter(communityApiRootUri)


  private fun authorizedClientManagerUserEnhanced(@Nullable username: String, clientRegistrationRepository: ClientRegistrationRepository?): OAuth2AuthorizedClientManager {
    val converter = UserAwareEntityConverter()
    val clientCredentialsTokenResponseClient = DefaultClientCredentialsTokenResponseClient()
    clientCredentialsTokenResponseClient.setRequestEntityConverter { grantRequest: OAuth2ClientCredentialsGrantRequest? ->
      converter.enhanceWithUsername(
        grantRequest,
        username
      )
    }
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials { builder: OAuth2AuthorizedClientProviderBuilder.ClientCredentialsGrantBuilder ->
        builder.accessTokenResponseClient(
          clientCredentialsTokenResponseClient
        )
      }
      .build()

        val service: OAuth2AuthorizedClientService = InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, service)

    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
//    val service: OAuth2AuthorizedClientService = InMemoryOAuth2AuthorizedClientService(clients)
//    val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clients, service)
//
//    val defaultClientCredentialsTokenResponseClient = DefaultClientCredentialsTokenResponseClient()
//    val authentication = SecurityContextHolder.getContext().authentication
//
//    defaultClientCredentialsTokenResponseClient.setRequestEntityConverter { grantRequest: OAuth2ClientCredentialsGrantRequest ->
//      val converter = CustomOAuth2ClientCredentialsGrantRequestEntityConverter()
//      val username = authentication.name
//      converter.enhanceWithUsername(grantRequest, username)
//    }
//
//    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
//      .clientCredentials { clientCredentialsGrantBuilder: OAuth2AuthorizedClientProviderBuilder.ClientCredentialsGrantBuilder ->
//        clientCredentialsGrantBuilder.accessTokenResponseClient(defaultClientCredentialsTokenResponseClient)
//      }
//      .build()
//
//    manager.setAuthorizedClientProvider(authorizedClientProvider)
//    return manager
  }

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

  private fun timeoutCounter(endpointUrl: String): Counter {
    val metricName = "http_client_requests_timeout"
    val host = URI(endpointUrl).host
    return meterRegistry.counter(metricName, Tags.of("clientName", host))
  }
}
