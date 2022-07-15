package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration

import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.flywaydb.test.annotation.FlywayTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.mockserver.integration.ClientAndServer
import org.mockserver.integration.ClientAndServer.startClientAndServer
import org.mockserver.matchers.Times
import org.mockserver.matchers.Times.exactly
import org.mockserver.model.Delay
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.helper.JwtAuthHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.contingencyPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.contingencyPlanSimpleResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.currentRiskScoresResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.historicalRiskScoresResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.roSHSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.documents.groupedDocumentsDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.allOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.convictions.convictionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.convictions.multipleConvictionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.licenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.multipleLicenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.noActiveOrInactiveLicences
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.limitedAccessOffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.mappaDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.offenderSearchDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.registrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.release.releaseSummaryDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessAllowedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessExcludedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessRestrictedResponse
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

  protected fun currentRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")

    oasysARNApi.`when`(currentScoresRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(currentRiskScoresResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noCurrentRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")

    oasysARNApi.`when`(currentScoresRequest, exactly(1)).respond(
      response().withStatusCode(404)
    )
  }

  protected fun noContingencyPlanResponse(crn: String, delaySeconds: Long = 0) {
    val contingencyPlanRequest =
      request().withPath("/assessments/risk-management-plans/$crn/ALLOW")
    oasysARNApi.`when`(contingencyPlanRequest, exactly(1)).respond(
      response().withStatusCode(404)
    )
  }

  protected fun contingencyPlanResponse(crn: String, delaySeconds: Long = 0) {
    val contingencyPlanRequest =
      request().withPath("/assessments/risk-management-plans/$crn/ALLOW")
    oasysARNApi.`when`(contingencyPlanRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(contingencyPlanResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun contingencyPlanSimpleResponse(crn: String, delaySeconds: Long = 0) {
    val contingencyPlanRequest =
      request().withPath("/assessments/risk-management-plans/$crn/ALLOW")
    oasysARNApi.`when`(contingencyPlanRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(contingencyPlanSimpleResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun historicalRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val historicalScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/rsr/history")

    oasysARNApi.`when`(historicalScoresRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(historicalRiskScoresResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noHistoricalRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val historicalScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/rsr/history")

    oasysARNApi.`when`(historicalScoresRequest, exactly(1)).respond(
      response().withStatusCode(404)
    )
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
          name = "Dylan Adam Armstrong",
          crn = "J678910",
          recommendation = Recommendation.NOT_RECALL,
          alternateActions = ""
        ),
        RecommendationEntity(
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

  protected fun noOffenderDetailsResponse(crn: String, delaySeconds: Long = 0) {
    val allOffenderDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/all")

    communityApi.`when`(allOffenderDetailsRequest, exactly(1)).respond(
      response().withStatusCode(404)
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

  protected fun noRoSHSummaryResponse(crn: String) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")
    oasysARNApi.`when`(roSHSummaryRequest, exactly(1)).respond(
      response().withStatusCode(404)
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

  protected fun convictionResponse(crn: String, staffCode: String, delaySeconds: Long = 0) {
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
        .withDelay(Delay.seconds(delaySeconds)).withStatusCode(403)
    )
  }

  protected fun userAccessRestricted(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/secure/offenders/crn/$crn/userAccess"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    communityApi.`when`(userAccessRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(userAccessRestrictedResponse())
        .withDelay(Delay.seconds(delaySeconds)).withStatusCode(403)
    )
  }

  protected fun groupedDocumentsResponse(crn: String, delaySeconds: Long = 0) {
    val groupedDocumentsRequest =
      request().withPath("/secure/offenders/crn/$crn/documents/grouped")

    communityApi.`when`(groupedDocumentsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(groupedDocumentsDeliusResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun getDocumentResponse(crn: String, documentId: String, delaySeconds: Long = 0) {
    val documentRequest =
      request().withPath("/secure/offenders/crn/$crn/documents/$documentId")

    communityApi.`when`(documentRequest, Times.exactly(1)).respond(
      response()
        .withHeader(HttpHeaders.CONTENT_TYPE, "application/pdf;charset=UTF-8")
        .withHeader(HttpHeaders.ACCEPT_RANGES, "bytes")
        .withHeader(HttpHeaders.CACHE_CONTROL, "max-age=0, must-revalidate")
        .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"myPdfTest.pdf\"")
        .withHeader(HttpHeaders.DATE, "Fri, 05 Jul 2022 09:50:45 GMT")
        .withHeader(HttpHeaders.ETAG, "9514985635950")
        .withHeader(HttpHeaders.LAST_MODIFIED, "Wed, 03 Jul 2022 13:20:35 GMT")
        .withBody(ClassPathResource("myPdfTest.pdf").file.readBytes())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noDocumentAvailable(crn: String, documentId: String) {
    val documentRequest =
      request().withPath("/secure/offenders/crn/$crn/documents/$documentId")

    communityApi.`when`(documentRequest, exactly(1)).respond(
      response().withStatusCode(404)
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
