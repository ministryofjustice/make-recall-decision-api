package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config


import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.lang.NonNull
import org.springframework.lang.Nullable
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
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import java.net.URI
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;


@Configuration //@Slf4j
//@AllArgsConstructor
class ClientTrackingInterceptor : HandlerInterceptor {
  private val clientDetails: ClientDetails? = null
  override fun preHandle(req: HttpServletRequest, @NonNull response: HttpServletResponse, @NonNull handler: Any): Boolean {
    val token = req.getHeader(HttpHeaders.AUTHORIZATION)
    if (StringUtils.startsWithIgnoreCase(token, "Bearer ")) {
      try {
        val claimsSet = getClaimsFromJWT(token)
        val username = Optional.ofNullable(claimsSet.getClaim("user_name"))
          .map { obj: Any -> obj.toString() }
          .orElse(null)
        val clientId = Optional.ofNullable(claimsSet.getClaim("client_id"))
          .map { obj: Any -> obj.toString() }
          .orElse(null)
        clientDetails!!.setClientDetails(clientId, username)
      } catch (e: ParseException) {
//        log.warn("problem decoding jwt public key for application insights", e)
      }
    }
    return true
  }

  @Throws(ParseException::class)
  private fun getClaimsFromJWT(token: String): JWTClaimsSet {
    val signedJWT = SignedJWT.parse(token.replace("Bearer ", ""))
    return signedJWT.jwtClaimsSet
  }
}

data class CommunityApiError (
   val status: Int? = null,
   val developerMessage: String? = null
  )




/**
 * Client details extracted from incoming token before oauth token validation.
 *
 * Note that these details are Thread scoped and so must not be accessed by Reactor operations which may run on a
 * separate thread. If you need to access these values from within a reactive context, pass them using the Reactor
 * Context API.
 */
@Component //@Slf4j
class ClientDetails(
  // TODO how to have overridden getter in kotlin
) : AuditorAware<String> {
  val clientId: String?
    get() = Companion.clientId.get()
  val username: String?
    get() = Companion.username.get()

  fun setClientDetails(clientId: String?, username: String?) {
    Companion.clientId.set(clientId)
    Companion.username.set(username)
  }

  override fun getCurrentAuditor(): Optional<String> {
    if (Companion.clientId.get() == null && Companion.username.get() == null) {
//      log.warn(
//        "Unable to retrieve clientId or username from ClientDetails, getCurrentAuditor() may have been " +
//                "called from an asynchronous Reactor context"
//      )
      return Optional.empty()
    }
    return Optional.ofNullable(
      String.format(
        "%s(%s)",
        Optional.ofNullable(Companion.username.get()).orElse(""),
        Optional.ofNullable(Companion.clientId.get()).orElse("")
      )
    )
  }

  companion object {
    private val clientId = ThreadLocal<String?>()
    private val username = ThreadLocal<String?>()
  }
}

//@Slf4j
//@AllArgsConstructor
class RestClientHelper(
   val client: WebClient? = null,
           val oauthClient: String? = null,
           val disableAuthentication: Boolean? = false
) {

  @JvmOverloads
  operator fun get(path: String, queryParams: MultiValueMap<String?, String?>? = LinkedMultiValueMap(0)): RequestHeadersSpec<*> {
    val spec = client!!
      .get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder
          .path(path)
          .queryParams(queryParams)
          .build()
      }
      .accept(MediaType.APPLICATION_JSON)
    return addSpecAuthAttribute(spec, path)
  }

  operator fun get(path: String, mediaType: MediaType?): RequestHeadersSpec<*> {
    val spec = client!!
      .get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder
          .path(path)
          .build()
      }
      .accept(mediaType)
    return addSpecAuthAttribute(spec, path)
  }

  private fun addSpecAuthAttribute(spec: RequestHeadersSpec<*>, path: String): RequestHeadersSpec<*> {
    if (disableAuthentication!!) {
//      log.info(String.format("Skipping authentication with community api for call to %s", path))
      return spec
    }
//    log.info(String.format("Authenticating with %s for call to %s", oauthClient, path))
    return spec.attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId(oauthClient))
  }

//  fun handleOffenderError(crn: String?, clientResponse: ClientResponse): Mono<out Throwable?> {
//    if (HttpStatus.NOT_FOUND == clientResponse.statusCode()) {
//      return Mono.error(PersonNotFoundException(crn!!)) // TODO enrich
//    }
//    return if (HttpStatus.FORBIDDEN == clientResponse.statusCode()) {
//      clientResponse.bodyToMono(CommunityApiError::class.java)
//        .flatMap { error -> Mono.error { error } }
//    } else handleError(clientResponse)
//  }

//  fun handleConvictionError(crn: String?, convictionId: Long?, clientResponse: ClientResponse): Mono<out Throwable?> {
//    return if (HttpStatus.NOT_FOUND == clientResponse.statusCode()) {
//      Mono.error(ConvictionNotFoundException(crn, convictionId))
//    } else handleError(clientResponse)
//  }

//  fun handleCustodialStatusError(crn: String?, convictionId: Long?, sentenceId: Long?, clientResponse: ClientResponse): Mono<out Throwable?> {
//    return if (HttpStatus.NOT_FOUND == clientResponse.statusCode()) {
//      Mono.error(CustodialStatusNotFoundException(crn, convictionId, sentenceId))
//    } else handleError(clientResponse)
//  }

//  fun handleNsiError(crn: String?, convictionId: Long?, nsiId: Long?, clientResponse: ClientResponse): Mono<out Throwable?> {
//    return if (HttpStatus.NOT_FOUND == clientResponse.statusCode()) {
//      Mono.error(NsiNotFoundException(crn, convictionId, nsiId))
//    } else handleError(clientResponse)
//  }

  private fun handleError(clientResponse: ClientResponse): Mono<out Throwable?> {
    val httpStatus = clientResponse.statusCode()
    throw WebClientResponseException.create(
      httpStatus.value(),
      httpStatus.name,
      clientResponse.headers().asHttpHeaders(),
      clientResponse.toString().toByteArray(),
      StandardCharsets.UTF_8
    )
  }
}


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
  fun communityApiClientUserEnhanced(@Qualifier("communityWebClientUserEnhancedAppScope") webClient: WebClient, clientDetails: ClientDetails, clientRegistrationRepository: ClientRegistrationRepository): CommunityApiClient {

    val restClientHelper = buildCommunityRestClientHelper(clientDetails.username, clientRegistrationRepository) //TODO click through this in loc 75 OffenderRestClientFactory
    /// and add uname ... use that bean or try UserContext again
    return CommunityApiClient(restClientHelper?.client!!, nDeliusTimeout, communityApiClientUserEnhancedTimeoutCounter())
  }

  @Bean
  fun communityApiClientUserEnhancedTimeoutCounter(): Counter = timeoutCounter(communityApiRootUri)



  fun buildCommunityRestClientHelper(@Nullable username: String?, clientRegistrationRepository: ClientRegistrationRepository?): RestClientHelper? {
    val webClient = buildWebClient(communityApiRootUri, 262144, username, clientRegistrationRepository) //TODO no haecode
    return RestClientHelper(webClient, "community-api-client", false)// TODO play around with this .... true???
  }

  fun buildWebClient(baseUrl: String?, bufferByteCount: Int, @Nullable username: String?, clientRegistrationRepository: ClientRegistrationRepository?): WebClient? {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManagerUserEnhanced(username, clientRegistrationRepository))
    val httpClient = HttpClient.create()
      .tcpConfiguration { client: TcpClient ->
        client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000) // TODO correct
          .doOnConnected { conn: Connection ->
            conn
              .addHandlerLast(ReadTimeoutHandler(nDeliusTimeout, TimeUnit.SECONDS))
              .addHandlerLast(WriteTimeoutHandler(nDeliusTimeout, TimeUnit.SECONDS))
          }
      }
    return WebClient
      .builder()
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(bufferByteCount) }
      .baseUrl(baseUrl)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .apply(oauth2Client.oauth2Configuration())
      .build()
  }

  private fun authorizedClientManagerUserEnhanced(@Nullable username: String?, clientRegistrationRepository: ClientRegistrationRepository?): OAuth2AuthorizedClientManager {
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

}
