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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.mappaDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.allOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.convictions.convictionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.convictions.multipleConvictionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.licenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.multipleLicenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.noActiveOrInactiveLicences
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.limitedAccessOffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.offenderSearchDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.registrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.release.releaseSummaryDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessAllowedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessExcludedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessRestrictedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.roSHSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Recommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository

@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@FlywayTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  var oasysARNApi: ClientAndServer = startClientAndServer(8095)

  @Autowired
  protected lateinit var repository: RecommendationRepository

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
    repository.deleteAll()
    communityApi.reset()
    offenderSearchApi.reset()
    gotenbergMock.reset()
    oasysARNApi.reset()
    setupOauth()
    setupHealthChecks()
  }

  @AfterAll
  fun tearDownServer() {
    oasysARNApi.stop()
    communityApi.stop()
    offenderSearchApi.stop()
    gotenbergMock.stop()
    oauthMock.stop()
  }

  protected fun mappaDetailsResponse(crn: String, delaySeconds: Long = 0) {
    val mappaDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/risk/mappa")

    communityApi.`when`(mappaDetailsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(mappaDetailsResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noMappaDetailsResponse(crn: String) {
    val mappaDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/risk/mappa")
    communityApi.`when`(mappaDetailsRequest, exactly(1)).respond(
      response().withStatusCode(404)
    )
  }

  fun insertRecommendations() {
    repository.saveAll(
      listOf(
        RecommendationEntity(
          id = null,
          name = "Dylan Adam Armstrong",
          crn = "J678910",
          recommendation = Recommendation.NOT_RECALL,
          alternateActions = ""
        ),
        RecommendationEntity(
          id = null,
          name = "Andrei Edwards",
          crn = "J680648",
          recommendation = Recommendation.RECALL,
          alternateActions = "increase reporting"
        )
      )
    )
  }

  protected fun allOffenderDetailsResponse(crn: String, delaySeconds: Long = 0) {
    val allOffenderDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/all")

    communityApi.`when`(allOffenderDetailsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(allOffenderDetailsResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun roSHSummaryResponse(crn: String, delaySeconds: Long = 0) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")

    oasysARNApi.`when`(roSHSummaryRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(roSHSummaryResponse())
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

  protected fun noActiveConvictionResponse(crn: String) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/convictions")
    communityApi.`when`(convictionsRequest, exactly(1)).respond(
      response().withStatusCode(404)
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

  protected fun multipleConvictionResponse(crn: String, staffCode: String, delaySeconds: Long = 0) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/convictions")

    communityApi.`when`(convictionsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(multipleConvictionsResponse(crn, staffCode))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun licenceConditionsResponse(crn: String, convictionId: Long, delaySeconds: Long = 0) {
    val licenceConditions =
      request().withPath("/secure/offenders/crn/$crn/convictions/$convictionId/licenceConditions")

    communityApi.`when`(licenceConditions, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(licenceResponse(convictionId))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun multipleLicenceConditionsResponse(crn: String, convictionId: Long, delaySeconds: Long = 0) {
    val licenceConditions =
      request().withPath("/secure/offenders/crn/$crn/convictions/$convictionId/licenceConditions")

    communityApi.`when`(licenceConditions, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(multipleLicenceResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noActiveLicenceConditions(crn: String, convictionId: Long) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/convictions/$convictionId/licenceConditions")
    communityApi.`when`(convictionsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(noActiveOrInactiveLicences())
    )
  }

  protected fun allOffenderDetailsResponseWithNoOffender(crn: String) {
    val convictionsRequest =
      request().withPath("/cases/$crn/personal-details")
    communityApi.`when`(convictionsRequest, exactly(1)).respond(
      response().withStatusCode(404)
    )
  }

  protected fun offenderSearchResponse(crn: String, delaySeconds: Long = 0) {
    val offenderSearchRequest =
      request()
        .withPath("/phrase")
        .withQueryStringParameter("paged", "false")

    offenderSearchApi.`when`(offenderSearchRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(offenderSearchDeliusResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun limitedAccessPractitionerOffenderSearchResponse(crn: String, delaySeconds: Long = 0) {
    val offenderSearchRequest =
      request()
        .withPath("/phrase")
        .withQueryStringParameter("paged", "false")

    offenderSearchApi.`when`(offenderSearchRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(limitedAccessOffenderSearchResponse(crn))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun contactSummaryResponse(crn: String, contactSummary: String, delaySeconds: Long = 0) {
    val contactSummaryUrl = "/secure/offenders/crn/$crn/contact-summary"
    val contactSummaryRequest = request()
      .withPath(contactSummaryUrl)

    communityApi.`when`(contactSummaryRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(contactSummary)
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun releaseSummaryResponse(crn: String, delaySeconds: Long = 0) {
    val releaseSummaryRequest =
      request().withPath("/secure/offenders/crn/$crn/release")

    communityApi.`when`(releaseSummaryRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(releaseSummaryDeliusResponse())
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

  protected fun userAccessAllowed(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/secure/offenders/crn/$crn/userAccess"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    communityApi.`when`(userAccessRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(userAccessAllowedResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun userAccessExcluded(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/secure/offenders/crn/$crn/userAccess"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    communityApi.`when`(userAccessRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(userAccessExcludedResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun userAccessRestricted(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/secure/offenders/crn/$crn/userAccess"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    communityApi.`when`(userAccessRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(userAccessRestrictedResponse())
        .withDelay(Delay.seconds(delaySeconds))
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
