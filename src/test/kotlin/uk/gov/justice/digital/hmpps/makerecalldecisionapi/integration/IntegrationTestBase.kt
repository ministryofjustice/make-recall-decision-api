package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration

import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.flywaydb.test.annotation.FlywayTest
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
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
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.helper.JwtAuthHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.recommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.allRiskScoresEmptyResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.allRiskScoresResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.assessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.riskManagementResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.risksDataResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.roSH404LatestCompleteNotFoundResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.roSH404NoOffenderFoundResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.roSHSummaryNoDataResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.roSHSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.cvl.licenceIdResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.cvl.licenceMatchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.documents.groupedDocumentsDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.allOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.convictions.convictionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.convictions.multipleConvictionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.convictions.nonCustodialConvictionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.licenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.multipleLicenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.noActiveOrInactiveLicences
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.limitedAccessOffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.mappaDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.offenderSearchDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.registrationsDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.release.releaseSummaryDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.roshHistoryDeliusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessAllowedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessExcludedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessRestrictedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.riskSummaryUnavailableResponse
import java.util.concurrent.TimeUnit

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

  var cvlApi: ClientAndServer = startClientAndServer(8096)

  @Autowired
  protected lateinit var repository: RecommendationRepository

  var communityApi: ClientAndServer = startClientAndServer(8092)
  var offenderSearchApi: ClientAndServer = startClientAndServer(8093)
  var gotenbergMock: ClientAndServer = startClientAndServer(8094)
  var oauthMock: ClientAndServer = startClientAndServer(9090)

  private val gson: Gson = Gson()

  val crn = "A12345"
  val nomsId = "A1234CR"
  val convictionId = 2500614567

  var createdRecommendationId: Int = 0

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

  companion object {
    @JvmStatic
    var postgresStarted = false

    @JvmStatic
    @BeforeAll
    fun setUpDb() {
      if (postgresStarted) return
      val postgresProcess = ProcessBuilder("docker-compose", "-f", "docker-compose-integration-test-postgres.yml", "up", "-d").start()
      postgresProcess.waitFor(120L, TimeUnit.SECONDS)
      val waitForProcess = ProcessBuilder("./scripts/wait-for-it.sh", "127.0.0.1:5432", "--strict", "-t", "600", "--", "sleep", "10").start()
      waitForProcess.waitFor(60L, TimeUnit.SECONDS)
      postgresStarted = true
    }
  }

  @BeforeEach
  fun startUpServer() {
    communityApi.reset()
    offenderSearchApi.reset()
    gotenbergMock.reset()
    oasysARNApi.reset()
    cvlApi.reset()
    setupOauth()
    setupHealthChecks()
  }

  @AfterAll
  fun tearDownServer() {
    oasysARNApi.stop()
    cvlApi.stop()
    communityApi.stop()
    offenderSearchApi.stop()
    gotenbergMock.stop()
    oauthMock.stop()
  }

  fun deleteAndCreateRecommendation(featureFlagString: String? = null) {
    deleteRecommendation()
    createRecommendation(featureFlagString)
  }

  fun deleteRecommendation() {
    repository.deleteAllInBatch()
  }

  private fun createRecommendation(featureFlagString: String? = null) {
    licenceConditionsResponse(crn, convictionId)

    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn))
        )
        .headers {
          (
            listOf(
              it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")),
              it.set("X-Feature-Flags", featureFlagString)
            )
            )
        }
        .exchange()
        .expectStatus().isCreated
    )

    createdRecommendationId = response.get("id") as Int
  }

  fun updateRecommendation(status: Status = Status.DRAFT) {
    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(updateRecommendationRequest(status))
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().is2xxSuccessful
  }

  fun convertResponseToJSONObject(response: WebTestClient.ResponseSpec): JSONObject {
    val responseBodySpec = response.expectBody<String>()
    val responseEntityExchangeResult = responseBodySpec.returnResult()
    val responseString = responseEntityExchangeResult.responseBody
    return JSONObject(responseString)
  }

  protected fun riskManagementPlanResponse(crn: String, delaySeconds: Long = 0) {
    val riskManagementPlanRequest =
      request().withPath("/risks/crn/$crn/risk-management-plan")
    oasysARNApi.`when`(riskManagementPlanRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(riskManagementResponse(crn))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun risksWithFullTextResponse(crn: String, delaySeconds: Long = 0) {
    val risksRequest =
      request().withPath("/risks/crn/$crn/fulltext")
    oasysARNApi.`when`(risksRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(risksDataResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun allRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")

    oasysARNApi.`when`(currentScoresRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(allRiskScoresResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun allRiskScoresEmptyResponse(crn: String) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")

    oasysARNApi.`when`(currentScoresRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(allRiskScoresEmptyResponse())
    )
  }

  protected fun oasysAssessmentsResponse(crn: String, delaySeconds: Long = 0, laterCompleteAssessmentExists: Boolean? = false, offenceType: String? = "CURRENT", superStatus: String? = "COMPLETE") {
    val assessmentsRequest =
      request().withPath("/assessments/crn/$crn/offence")

    oasysARNApi.`when`(assessmentsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(assessmentsResponse(crn, laterCompleteAssessmentExists = laterCompleteAssessmentExists, offenceType = offenceType, superStatus = superStatus))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")

    oasysARNApi.`when`(currentScoresRequest, exactly(1)).respond(
      response().withStatusCode(404)
    )
  }

  protected fun failedRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")

    oasysARNApi.`when`(currentScoresRequest).respond(
      response().withStatusCode(500)
    )
  }

  protected fun mappaDetailsResponse(crn: String, delaySeconds: Long = 0, level: Int? = 1, category: Int? = 0) {
    val mappaDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/risk/mappa")

    communityApi.`when`(mappaDetailsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(mappaDetailsResponse(level, category))
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

  protected fun errorMappaDetailsResponse(crn: String) {
    val mappaDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/risk/mappa")
    communityApi.`when`(mappaDetailsRequest, exactly(1)).respond(
      response().withStatusCode(500)
    )
  }

  protected fun allOffenderDetailsResponse(crn: String, delaySeconds: Long = 0) {
    val allOffenderDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/all")

    communityApi.`when`(allOffenderDetailsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(allOffenderDetailsResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun allOffenderDetailsResponseOneTimeOnly(crn: String, delaySeconds: Long = 0, district: String? = "Sheffield City Centre", firstName: String? = "John") {
    val allOffenderDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/all")

    communityApi.`when`(allOffenderDetailsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(allOffenderDetailsResponse(district = district, firstName = firstName))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noOffenderDetailsResponse(crn: String, delaySeconds: Long = 0) {
    val allOffenderDetailsRequest =
      request().withPath("/secure/offenders/crn/$crn/all")

    communityApi.`when`(allOffenderDetailsRequest).respond(
      response().withStatusCode(404)
    )
  }

  protected fun roSHSummaryResponse(crn: String, delaySeconds: Long = 0) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")

    oasysARNApi.`when`(roSHSummaryRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(roSHSummaryResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun roSHSummaryNoDataResponse(crn: String, delaySeconds: Long = 0) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")

    oasysARNApi.`when`(roSHSummaryRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(roSHSummaryNoDataResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noOffenderFoundRoshSummaryResponse(crn: String) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")
    oasysARNApi.`when`(roSHSummaryRequest, exactly(1)).respond(
      response().withBody(roSH404NoOffenderFoundResponse(crn)).withStatusCode(404)
    )
  }

  protected fun noLatestCompleteRoshSummaryResponse(crn: String) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")
    oasysARNApi.`when`(roSHSummaryRequest, exactly(1)).respond(
      response().withBody(roSH404LatestCompleteNotFoundResponse(crn)).withStatusCode(404)
    )
  }

  protected fun failedRoSHSummaryResponse(crn: String) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")
    oasysARNApi.`when`(roSHSummaryRequest).respond(
      response().withBody(riskSummaryUnavailableResponse()).withStatusCode(500)
    )
  }

  protected fun registrationsResponse(delaySeconds: Long = 0) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/registrations")

    communityApi.`when`(convictionsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(registrationsDeliusResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun roshHistoryResponse(delaySeconds: Long = 0) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/registrations")

    communityApi.`when`(convictionsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(roshHistoryDeliusResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noActiveConvictionResponse(crn: String) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/convictions")
    communityApi.`when`(convictionsRequest).respond(
      response().withStatusCode(404)
    )
  }

  protected fun convictionResponse(crn: String, staffCode: String, delaySeconds: Long = 0, active: Boolean? = true, offenceCode: String? = "1234", offenceDate: String? = "2022-04-24T20:39:47.778Z") {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/convictions")

    communityApi.`when`(convictionsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(convictionsResponse(crn, staffCode, active, offenceCode, offenceDate))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun nonCustodialConvictionResponse(crn: String, staffCode: String, delaySeconds: Long = 0) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/convictions")

    communityApi.`when`(convictionsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(nonCustodialConvictionsResponse(crn, staffCode))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun multipleConvictionResponse(crn: String, staffCode: String, delaySeconds: Long = 0) {
    val convictionsRequest =
      request().withPath("/secure/offenders/crn/$crn/convictions")

    communityApi.`when`(convictionsRequest).respond(
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

    communityApi.`when`(licenceConditions).respond(
      response().withContentType(APPLICATION_JSON).withBody(multipleLicenceResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun noActiveLicenceConditions(crn: String, convictionId: Long) {
    val licenceConditions =
      request().withPath("/secure/offenders/crn/$crn/convictions/$convictionId/licenceConditions")
    communityApi.`when`(licenceConditions).respond(
      response().withContentType(APPLICATION_JSON).withBody(noActiveOrInactiveLicences())
    )
  }

  protected fun allOffenderDetailsResponseWithNoOffender(crn: String) {
    val personalDetails =
      request().withPath("/cases/$crn/personal-details")
    communityApi.`when`(personalDetails, exactly(1)).respond(
      response().withStatusCode(404)
    )
  }

  protected fun offenderSearchResponse(crn: String, delaySeconds: Long = 0) {
    val offenderSearchRequest =
      request()
        .withPath("/phrase")
        .withQueryStringParameter("paged", "false")

    offenderSearchApi.`when`(offenderSearchRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(offenderSearchDeliusResponse(crn))
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

    communityApi.`when`(releaseSummaryRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(releaseSummaryDeliusResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun releaseSummaryResponseWithStatusCode(crn: String, releaseSummary: String, statusCode: Int) {
    val releaseSummaryRequest =
      request().withPath("/secure/offenders/crn/$crn/release")

    communityApi.`when`(releaseSummaryRequest).respond(
      response().withContentType(APPLICATION_JSON).withStatusCode(statusCode).withBody(releaseSummary)
    )
  }

  protected fun userAccessAllowed(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/secure/offenders/crn/$crn/userAccess"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    communityApi.`when`(userAccessRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(userAccessAllowedResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun userAccessAllowedOnce(crn: String, delaySeconds: Long = 0) {
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

  protected fun userNotFound(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/secure/offenders/crn/$crn/userAccess"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    communityApi.`when`(userAccessRequest, exactly(1)).respond(
      response()
        .withDelay(Delay.seconds(delaySeconds)).withStatusCode(404)
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

    communityApi.`when`(groupedDocumentsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(groupedDocumentsDeliusResponse())
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun getDocumentResponse(crn: String, documentId: String, delaySeconds: Long = 0) {
    val documentRequest =
      request().withPath("/secure/offenders/crn/$crn/documents/$documentId")

    communityApi.`when`(documentRequest).respond(
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

  protected fun cvlLicenceMatchResponse(nomisId: String, crn: String, delaySeconds: Long = 0) {
    val licenceMatchRequest =
      request().withPath("/licence/match")

    cvlApi.`when`(licenceMatchRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(licenceMatchResponse(nomisId, crn))
        .withDelay(Delay.seconds(delaySeconds))
    )
  }

  protected fun cvlLicenceByIdResponse(licenceId: Int, nomisId: String, crn: String, delaySeconds: Long = 0) {
    val licenceIdRequesst =
      request().withPath("/licence/id/$licenceId")

    cvlApi.`when`(licenceIdRequesst).respond(
      response().withContentType(APPLICATION_JSON).withBody(licenceIdResponse(licenceId, nomisId, crn))
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
