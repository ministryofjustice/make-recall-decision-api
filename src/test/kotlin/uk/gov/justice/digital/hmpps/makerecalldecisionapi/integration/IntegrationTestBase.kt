package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration

import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.flywaydb.test.annotation.FlywayTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.matchers.Times.exactly
import org.mockserver.model.Delay
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.helper.JwtAuthHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.allOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.convictionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.offenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.registrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.releaseSummaryResponse

@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@FlywayTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  var communityApi: ClientAndServer = startClientAndServer(8092)
  var offenderSearchApi: ClientAndServer = startClientAndServer(8093)
  var gotenbergMock: ClientAndServer = startClientAndServer(8094)
  var oauthMock: ClientAndServer = startClientAndServer(9090)

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
    offenderSearchApi.reset()
    gotenbergMock.reset()
    setupOauth()
    setupHealthChecks()
  }

  @AfterAll
  fun tearDownServer() {
    communityApi.stop()
    offenderSearchApi.stop()
    gotenbergMock.stop()
    oauthMock.stop()
  }

  protected fun allOffenderDetailsResponse(crn: String, delaySeconds: Long = 0) {
    val allOffenderDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/all")

    communityApi.`when`(allOffenderDetailsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(allOffenderDetailsResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun unallocatedConvictionResponse(crn: String, staffCode: String, delaySeconds: Long = 0) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/convictions")

    communityApi.`when`(convictionsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(convictionsResponse(crn, staffCode))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun registrationsResponse(crn: String, delaySeconds: Long = 0) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/registrations")

    communityApi.`when`(convictionsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(registrationsResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun unallocatedOffenderSearchResponse(crn: String, delaySeconds: Long = 0) {
    val offenderSearchRequest =
      request()
        .withPath("/phrase")
        .withQueryStringParameter("paged", "false")

    offenderSearchApi.`when`(offenderSearchRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(offenderSearchResponse(crn))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun contactSummaryResponse(crn: String, contactSummary: String, delaySeconds: Long = 0) {
    val contactSummaryRequest =
      request().withPath("/secure/offenders/crn/$crn/contact-summary")
        .withQueryStringParameter("contactTypes", "MO5", "LCL", "C204", "CARR", "C123", "C071", "COAP", "RECI")

    communityApi.`when`(contactSummaryRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(contactSummary)
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun releaseSummaryResponse(crn: String, delaySeconds: Long = 0) {
    val releaseSummaryRequest =
      request().withPath("/secure/offenders/crn/$crn/release")

    communityApi.`when`(releaseSummaryRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(releaseSummaryResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun releaseSummaryResponseWithStatusCode(crn: String, releaseSummary: String, statusCode: Int) {
    val releaseSummaryRequest =
      request().withPath("/secure/offenders/crn/$crn/release")

    communityApi.`when`(releaseSummaryRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withStatusCode(statusCode).withBody(releaseSummary)
    )
  }

  fun setupOauth() {
    oauthMock
      .`when`(request().withPath("/auth/oauth/token"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("access_token" to "ABCDE", "token_type" to "bearer")))
      )
  }

  fun setupHealthChecks() {
    oauthMock
      .`when`(request().withPath("/auth/health/ping"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "OK")))
      )

    communityApi
      .`when`(request().withPath("/ping"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "OK")))
      )

    offenderSearchApi
      .`when`(request().withPath("/health/ping"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "OK")))
      )

    gotenbergMock
      .`when`(request().withPath("/health"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "up")))
      )
  }
}
