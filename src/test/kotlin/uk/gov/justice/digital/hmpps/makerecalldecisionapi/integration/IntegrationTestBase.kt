package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration

import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.matchers.Times.exactly
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.helper.JwtAuthHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.offenderSearchByPhraseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.allOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.convictionsResponse

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  var communityApi: ClientAndServer = startClientAndServer(8092)

  private var oauthMock: ClientAndServer = startClientAndServer(9090)
  private val gson: Gson = Gson()

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  internal fun HttpHeaders.authToken(roles: List<String> = emptyList()) {
    this.setBearerAuth(
      jwtAuthHelper.createJwt(
        subject = "SOME_USER",
        roles = roles,
        clientId = "community-api"
      )
    )
  }

  @BeforeEach
  fun startUpServer() {
    communityApi.reset()
    setupOauth()
  }

  @AfterAll
  fun tearDownServer() {
    communityApi.stop()
    oauthMock.stop()
  }

  protected fun allOffenderDetailsResponse(crn: String) {
    val allOffenderDetailsRequest =
      request().withPath("/offenders/crn/$crn/all")

    communityApi.`when`(allOffenderDetailsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(allOffenderDetailsResponse())
    )
  }

  protected fun unallocatedConvictionResponse(crn: String, staffCode: String) {
    val convictionsRequest =
      request().withPath("/offenders/crn/$crn/convictions")

    communityApi.`when`(convictionsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(convictionsResponse(staffCode))
    )
  }

  protected fun offenderSearchResponse(crn: String) {
    val offenderSearchRequest =
      request().withPath("/offender-search/searchByPhrase?paged=false")
        .withBody(offenderSearchByPhraseRequest(crn))

    offenderSearchApi.`when`(offenderSearchRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(offenderSearchResponse(crn))
    )
  }

  fun setupOauth() {
    val response = response().withContentType(APPLICATION_JSON)
      .withBody(gson.toJson(mapOf("access_token" to "ABCDE", "token_type" to "bearer")))
    oauthMock.`when`(request().withPath("/auth/oauth/token")).respond(response)
  }
}
