package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.flywaydb.test.annotation.FlywayTest
import org.json.JSONArray
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
import org.mockserver.model.HttpStatusCode
import org.mockserver.model.JsonBody.json
import org.mockserver.model.MediaType.APPLICATION_JSON
import org.mockserver.model.MediaType.JPEG
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.FindByNameRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ppud.toJsonBody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toJson
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.helper.JwtAuthHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.recommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.recommendationStatusRequest
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationBookRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationCreateOffenderResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationCreateRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationCreateSentenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationSearchActiveUsersResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation.ppudAutomationUpdateReleaseResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.cvl.licenceIdResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.cvl.licenceMatchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.deliusMappaAndRoshHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.deliusNoMappaOrRoshHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.deliusRecommendationModelResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.deliusRoshHistoryOnlyResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.findByCrnResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.findByCrnResponseNotFound
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.findByNameResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.licenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.licenceResponseMultipleConvictions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.licenceResponseNoConvictions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.multipleLicenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.noActiveOrInactiveLicences
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions.nonCustodialLicencesResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.overviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.overviewResponseNoConvictions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.overviewResponseNonCustodial
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.personalDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.providerResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessAllowedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessExcludedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess.userAccessRestrictedResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison.prisonResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationSupportingDocumentRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.riskSummaryUnavailableResponse
import java.io.File
import java.nio.file.Paths
import java.sql.DriverManager
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.userResponse as userResponseJson

@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@FlywayTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var repository: RecommendationRepository

  @Autowired
  protected lateinit var statusRepository: RecommendationStatusRepository

  @Autowired
  protected lateinit var recommendationSupportingDocumentRepository: RecommendationSupportingDocumentRepository

  var gotenbergMock: ClientAndServer = startClientAndServer(8094)
  var oasysARNApi: ClientAndServer = startClientAndServer(8095)
  var cvlApi: ClientAndServer = startClientAndServer(8096)
  var documentManagementApi: ClientAndServer = startClientAndServer(9072)
  var deliusIntegration: ClientAndServer = startClientAndServer(8097)
  var oauthMock: ClientAndServer = startClientAndServer(9090)
  var prisonApi: ClientAndServer = startClientAndServer(8098)
  var ppudAutomationApi: ClientAndServer = startClientAndServer(8099)

  private val gson: Gson = Gson()

  val crn = "A12345"
  val nomsId = "A1234CR"
  val convictionId = 2500614567
  val dateOfBirth: LocalDate = LocalDate.of(1982, 10, 24)

  var createdRecommendationId: Int = 0

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  internal fun HttpHeaders.authToken(roles: List<String> = emptyList(), subject: String? = "SOME_USER") {
    this.setBearerAuth(
      jwtAuthHelper.createJwt(
        subject = "$subject",
        roles = roles,
        clientId = "make-recall-decisions-api",
      ),
    )
  }

  companion object {
    @JvmStatic
    var postgresStarted = false

    @JvmStatic
    @BeforeAll
    fun setUpDb() {
      if (postgresStarted) return
      runDbMigrations()
      postgresStarted = true
    }

    /**
     * The programmatic execution of the migration files below was introduced with
     * https://github.com/ministryofjustice/make-recall-decision-api/pull/983 . It is
     * unclear (to me, anyway) why the changes meant this programmatic migration is
     * now required. MRD-2686 has been opened to investigate.
     */
    private fun runDbMigrations() {
      val url = "jdbc:postgresql://localhost:5432/make_recall_decision"
      val user = "mrd_user"
      val password = "secret"

      val sqlFiles = listOf(
        "V1_0__RECOMMENDATIONS_TABLE.sql",
        "V1_1__RECOMMENDATIONS_TABLE.sql",
        "V1_2__RECOMMENDATIONS_TABLE.sql",
        "V1_11__RECOMMENDATION_STATUS_TABLE.sql",
        "V1_13__RECOMMENDATION_STATUS_TABLE.sql",
        "V1_14__RECOMMENDATION_HISTORY_TABLE.sql",
        "V1_15__RECOMMENDATION_STATUS_TABLE.sql",
        "V1_16__DATA_MIGRATION.sql",
        "V1_24__PPUD_USERS_TABLE.sql",
        "V1_25__RECOMMENDATION_DOCUMENT_TABLE.sql",
        "V1_27__RECOMMENDATION_DOCUMENT_TABLE.sql",
        "V1_28__RECOMMENDATION_DOCUMENT_TABLE.sql",
        "V1_30__PPUD_USERS_TABLE_ADD_PPUD_USER_NAME_COL.sql",
        "V1_31__create-NOMIS_TO_PPUD_ESTABLISHMENT_MAPPING-table.sql",
        "V1_32__insert-establishment-mappings.sql",
      )

      val currentDirectory = File(".").absoluteFile.parentFile
      val resourcesDirectoryPath =
        Paths.get(currentDirectory.toString(), "src", "main", "resources", "db", "migration").toString()
      Class.forName("org.postgresql.Driver")
      DriverManager.getConnection(url, user, password).use { connection ->
        for (sqlFile in sqlFiles) {
          val file = File(resourcesDirectoryPath, sqlFile)
          val script = file.readText()
          connection.prepareStatement(script).use { statement ->
            statement.execute()
          }
        }
      }
    }
  }

  @BeforeEach
  fun startUpServer() {
    oauthMock.reset()
    cvlApi.reset()
    documentManagementApi.reset()
    deliusIntegration.reset()
    gotenbergMock.reset()
    oasysARNApi.reset()
    prisonApi.reset()
    ppudAutomationApi.reset()
    setupOauth()
    setupHealthChecks()
  }

  @AfterAll
  fun tearDownServer() {
    documentManagementApi.stop()
    cvlApi.stop()
    deliusIntegration.stop()
    gotenbergMock.stop()
    oasysARNApi.stop()
    oauthMock.stop()
    prisonApi.stop()
    ppudAutomationApi.stop()
  }

  fun deleteAndCreateRecommendation(featureFlagString: String? = null) {
    deleteRecommendation()
    createRecommendation(featureFlagString)
  }

  fun deleteRecommendation() {
    repository.deleteAllInBatch()
  }

  fun createRecommendation(featureFlagString: String? = null) {
    licenceConditionsResponse(crn, convictionId)

    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn)),
        )
        .headers {
          (
            listOf(
              it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")),
              it.set("X-Feature-Flags", featureFlagString),
            )
            )
        }
        .exchange()
        .expectStatus().isCreated,
    )

    createdRecommendationId = response.get("id") as Int
  }

  fun updateRecommendation(status: Status = Status.DRAFT) {
    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(updateRecommendationRequest(status)),
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().is2xxSuccessful
  }

  fun updateRecommendation(recommendationRequest: String, refreshPage: String? = null) {
    val refreshPageQueryString = if (refreshPage != null) "?refreshProperty=$refreshPage" else ""
    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId$refreshPageQueryString")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(recommendationRequest),
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
  }

  fun createOrUpdateRecommendationStatus(
    activate: List<String>,
    deactivate: List<String> = emptyList(),
    subject: String? = "SOME_USER",
  ) = convertResponseToJSONArray(
    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId/status")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          recommendationStatusRequest(
            activate = activate,
            deactivate = deactivate,
          ),
        ),
      )
      .headers {
        (
          listOf(
            it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO"), subject = subject),
          )
          )
      }
      .exchange()
      .expectStatus().isOk,
  )

  fun convertResponseToJSONObject(response: WebTestClient.ResponseSpec): JSONObject {
    val responseBodySpec = response.expectBody<String>()
    val responseEntityExchangeResult = responseBodySpec.returnResult()
    val responseString = responseEntityExchangeResult.responseBody
    return JSONObject(responseString)
  }

  fun convertResponseToJSONArray(response: WebTestClient.ResponseSpec): JSONArray {
    val responseBodySpec = response.expectBody<String>()
    val responseEntityExchangeResult = responseBodySpec.returnResult()
    val responseString = responseEntityExchangeResult.responseBody
    return JSONArray(responseString)
  }

  protected fun riskManagementPlanResponse(crn: String, delaySeconds: Long = 0) {
    val riskManagementPlanRequest =
      request().withPath("/risks/crn/$crn/risk-management-plan")
    oasysARNApi.`when`(riskManagementPlanRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(riskManagementResponse(crn))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun risksWithFullTextResponse(crn: String, delaySeconds: Long = 0) {
    val risksRequest =
      request().withPath("/risks/crn/$crn/fulltext")
    oasysARNApi.`when`(risksRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(risksDataResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun allRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")
    oasysARNApi.`when`(currentScoresRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(allRiskScoresResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun allRiskScoresEmptyResponse(crn: String) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")

    oasysARNApi.`when`(currentScoresRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(allRiskScoresEmptyResponse()),
    )
  }

  protected fun oasysAssessmentsResponse(
    crn: String,
    delaySeconds: Long = 0,
    laterCompleteAssessmentExists: Boolean? = false,
    offenceType: String? = "CURRENT",
    superStatus: String? = "COMPLETE",
  ) {
    val assessmentsRequest =
      request().withPath("/assessments/crn/$crn/offence")

    oasysARNApi.`when`(assessmentsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(
        assessmentsResponse(
          crn,
          laterCompleteAssessmentExists = laterCompleteAssessmentExists,
          offenceType = offenceType,
          superStatus = superStatus,
        ),
      )
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun noRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")

    oasysARNApi.`when`(currentScoresRequest, exactly(1)).respond(
      response().withStatusCode(404),
    )
  }

  protected fun failedRiskScoresResponse(crn: String, delaySeconds: Long = 0) {
    val currentScoresRequest =
      request().withPath("/risks/crn/$crn/predictors/all")

    oasysARNApi.`when`(currentScoresRequest).respond(
      response().withStatusCode(500),
    )
  }

  protected fun personalDetailsResponse(crn: String, delaySeconds: Long = 0, nomisId: String? = "A1234CR") {
    val personalDetailsRequest =
      request().withPath("/case-summary/$crn/personal-details")

    deliusIntegration.`when`(personalDetailsRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(personalDetailsResponse(nomisId = nomisId))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun personalDetailsResponseOneTimeOnly(
    crn: String,
    delaySeconds: Long = 0,
    district: String? = "Sheffield City Centre",
    firstName: String? = "Joe",
  ) {
    val personalDetailsRequest =
      request().withPath("/case-summary/$crn/personal-details")

    deliusIntegration.`when`(personalDetailsRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(personalDetailsResponse(district = district, firstName = firstName))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun overviewResponse(crn: String, delaySeconds: Long = 0) {
    val overviewRequest =
      request().withPath("/case-summary/$crn/overview")

    deliusIntegration.`when`(overviewRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(overviewResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun overviewResponseNonCustodial(crn: String, delaySeconds: Long = 0) {
    val overviewRequest =
      request().withPath("/case-summary/$crn/overview")

    deliusIntegration.`when`(overviewRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(overviewResponseNonCustodial())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun overviewResponseNoConvictions(crn: String, delaySeconds: Long = 0) {
    val overviewRequest =
      request().withPath("/case-summary/$crn/overview")

    deliusIntegration.`when`(overviewRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(overviewResponseNoConvictions())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun mappaAndRoshHistoryResponse(crn: String, delaySeconds: Long = 0) {
    val request =
      request().withPath("/case-summary/$crn/mappa-and-rosh-history")

    deliusIntegration.`when`(request).respond(
      response().withContentType(APPLICATION_JSON).withBody(deliusMappaAndRoshHistoryResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun roshHistoryOnlyResponse(crn: String, delaySeconds: Long = 0) {
    val request =
      request().withPath("/case-summary/$crn/mappa-and-rosh-history")

    deliusIntegration.`when`(request).respond(
      response().withContentType(APPLICATION_JSON).withBody(deliusRoshHistoryOnlyResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun noMappaOrRoshHistoryResponse(crn: String, delaySeconds: Long = 0) {
    val request =
      request().withPath("/case-summary/$crn/mappa-and-rosh-history")

    deliusIntegration.`when`(request).respond(
      response().withContentType(APPLICATION_JSON).withBody(deliusNoMappaOrRoshHistoryResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun mappaAndRoshHistoryNotFound(crn: String, delaySeconds: Long = 0) {
    val request =
      request().withPath("/case-summary/$crn/mappa-and-rosh-history")

    deliusIntegration.`when`(request).respond(response().withStatusCode(404))
  }

  protected fun recommendationModelResponse(crn: String, delaySeconds: Long = 0, firstName: String = "Joe") {
    val request =
      request().withPath("/case-summary/$crn/recommendation-model")

    deliusIntegration.`when`(request).respond(
      response().withContentType(APPLICATION_JSON).withBody(deliusRecommendationModelResponse(firstName))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun deliusInternalServerErrorResponse() {
    deliusIntegration.`when`(request()).respond(
      response()
        .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR_500.code()),
    )
  }

  protected fun roSHSummaryResponse(crn: String, delaySeconds: Long = 0) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")

    oasysARNApi.`when`(roSHSummaryRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(roSHSummaryResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun roSHSummaryNoDataResponse(crn: String, delaySeconds: Long = 0) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")

    oasysARNApi.`when`(roSHSummaryRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(roSHSummaryNoDataResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun noOffenderFoundRoshSummaryResponse(crn: String) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")
    oasysARNApi.`when`(roSHSummaryRequest, exactly(1)).respond(
      response().withBody(roSH404NoOffenderFoundResponse(crn)).withStatusCode(404),
    )
  }

  protected fun noLatestCompleteRoshSummaryResponse(crn: String) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")
    oasysARNApi.`when`(roSHSummaryRequest, exactly(1)).respond(
      response().withBody(roSH404LatestCompleteNotFoundResponse(crn)).withStatusCode(404),
    )
  }

  protected fun failedRoSHSummaryResponse(crn: String) {
    val roSHSummaryRequest =
      request().withPath("/risks/crn/$crn/summary")
    oasysARNApi.`when`(roSHSummaryRequest).respond(
      response().withBody(riskSummaryUnavailableResponse()).withStatusCode(500),
    )
  }

  protected fun licenceConditionsResponse(
    crn: String,
    delaySeconds: Long = 0,
    releasedOnLicence: Boolean? = false,
    licenceStartDate: String? = "2020-06-25",
  ) {
    val licenceConditions =
      request().withPath("/case-summary/$crn/licence-conditions")

    deliusIntegration.`when`(licenceConditions).respond(
      response().withContentType(APPLICATION_JSON).withBody(licenceResponse(releasedOnLicence, licenceStartDate))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun userResponse(username: String, email: String, delaySeconds: Long = 0) {
    val userResponse =
      request().withPath("/user/$username")
    deliusIntegration.`when`(userResponse, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(userResponseJson(username, email))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun providerByCodeResponse(code: String, name: String, delaySeconds: Long = 0) {
    val request = request().withPath("/provider/$code")
    deliusIntegration.`when`(request, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(providerResponse(code, name))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  @Suppress("SameParameterValue")
  protected fun providerNotFoundResponse(code: String) {
    val request = request().withPath("/provider/$code")
    deliusIntegration.`when`(request, exactly(1)).respond(
      response().withStatusCode(404),
    )
  }

  protected fun multipleLicenceConditionsResponse(crn: String, delaySeconds: Long = 0) {
    val licenceConditions =
      request().withPath("/case-summary/$crn/licence-conditions")

    deliusIntegration.`when`(licenceConditions).respond(
      response().withContentType(APPLICATION_JSON).withBody(multipleLicenceResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun noActiveLicenceConditions(crn: String) {
    val licenceConditions =
      request().withPath("/case-summary/$crn/licence-conditions")

    deliusIntegration.`when`(licenceConditions).respond(
      response().withContentType(APPLICATION_JSON).withBody(noActiveOrInactiveLicences()),
    )
  }

  protected fun nonCustodialLicenceConditionsResponse(crn: String, delaySeconds: Long = 0) {
    val licenceConditions =
      request().withPath("/case-summary/$crn/licence-conditions")

    deliusIntegration.`when`(licenceConditions, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(nonCustodialLicencesResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun licenceConditionsResponseWithMultipleActiveConvictions(crn: String, delaySeconds: Long = 0) {
    val licenceConditions =
      request().withPath("/case-summary/$crn/licence-conditions")

    deliusIntegration.`when`(licenceConditions).respond(
      response().withContentType(APPLICATION_JSON).withBody(licenceResponseMultipleConvictions())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun licenceConditionsResponseWithNoActiveConvictions(crn: String, delaySeconds: Long = 0) {
    val licenceConditions =
      request().withPath("/case-summary/$crn/licence-conditions")

    deliusIntegration.`when`(licenceConditions).respond(
      response().withContentType(APPLICATION_JSON).withBody(licenceResponseNoConvictions())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun personalDetailsNotFound(crn: String) {
    val personalDetails =
      request().withPath("/case-summary/$crn/personal-details")
    deliusIntegration.`when`(personalDetails, exactly(1)).respond(
      response().withStatusCode(404),
    )
  }

  @Suppress("SameParameterValue")
  protected fun personalDetailsError(crn: String) {
    val personalDetails =
      request().withPath("/case-summary/$crn/personal-details")
    deliusIntegration.`when`(personalDetails).respond(response().withStatusCode(500))
  }

  @Suppress("SameParameterValue")
  protected fun personalDetailsSuccessAfterRetry(crn: String) {
    val personalDetails =
      request().withPath("/case-summary/$crn/personal-details")
    deliusIntegration.`when`(personalDetails, exactly(1))
      .respond(response().withStatusCode(HttpStatusCode.GATEWAY_TIMEOUT_504.code()))
    deliusIntegration.`when`(personalDetails).respond(
      response().withContentType(APPLICATION_JSON).withBody(personalDetailsResponse()),
    )
  }

  protected fun findByCrnSuccess(
    crn: String = "X123456",
    firstName: String = "Joe",
    surname: String = "Bloggs",
    dateOfBirth: String = "2000-11-09",
    delaySeconds: Long = 0,
  ) {
    deliusIntegration.`when`(request().withPath("/case-summary/$crn")).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(findByCrnResponse(crn, firstName, surname, dateOfBirth))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun findByCrnNotFound(
    crn: String = "X123456",
    firstName: String = "Joe",
    surname: String = "Bloggs",
    dateOfBirth: String = "2000-11-09",
    delaySeconds: Long = 0,
  ) {
    deliusIntegration.`when`(request().withPath("/case-summary/$crn")).respond(
      response().withContentType(APPLICATION_JSON)
        .withStatusCode(404)
        .withBody(findByCrnResponseNotFound(crn))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun findByNameSuccess(
    crn: String = "Y654321",
    firstName: String = "Joe",
    surname: String = "Bloggs",
    dateOfBirth: String = "1980-12-01",
    pageNumber: Int = 0,
    pageSize: Int = 1,
    totalPages: Int = 1,
    delaySeconds: Long = 0,
  ) {
    val offenderSearchRequest = request()
      .withPath("/case-summary/search")
      .withQueryStringParameter("page", pageNumber.toString())
      .withQueryStringParameter("size", pageSize.toString())
      .withBody(json(objectMapper.writeValueAsString(FindByNameRequest(firstName, surname))))

    deliusIntegration.`when`(offenderSearchRequest).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(
          findByNameResponse(
            crn = crn,
            firstName = firstName,
            surname = surname,
            dateOfBirth = dateOfBirth,
            pageNumber = pageNumber,
            pageSize = pageSize,
            totalPages = totalPages,
          ),
        )
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun findByNameNoResults(
    firstName: String = "Joe",
    surname: String = "Bloggs",
    pageNumber: Int = 0,
    pageSize: Int = 1,
    totalPages: Int = 1,
    delaySeconds: Long = 0,
  ) {
    val offenderSearchRequest = request()
      .withPath("/case-summary/search")
      .withQueryStringParameter("page", pageNumber.toString())
      .withQueryStringParameter("size", pageSize.toString())
      .withBody(json(objectMapper.writeValueAsString(FindByNameRequest(firstName, surname))))

    deliusIntegration.`when`(offenderSearchRequest).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(
          """
            {
              "content": [],
              "page": {
                "size": $pageSize,
                "number": $pageNumber,
                "totalElements": 0,
                "totalPages": $totalPages
              }
            }
          """.trimIndent(),
        )
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun deliusContactHistoryResponse(crn: String, body: String, delaySeconds: Long = 0) {
    val contactSummaryUrl = "/case-summary/$crn/contact-history"
    val contactSummaryRequest = request()
      .withPath(contactSummaryUrl)

    deliusIntegration.`when`(contactSummaryRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(body)
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun userAccessAllowed(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/user/.*/access/$crn"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    deliusIntegration.`when`(userAccessRequest).respond(
      response().withContentType(APPLICATION_JSON).withBody(userAccessAllowedResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun userAccessAllowedOnce(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/user/.*/access/$crn"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    deliusIntegration.`when`(userAccessRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(userAccessAllowedResponse())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun userAccessExcluded(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/user/.*/access/$crn"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    deliusIntegration.`when`(userAccessRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(userAccessExcludedResponse())
        .withDelay(Delay.seconds(delaySeconds)).withStatusCode(200),
    )
  }

  protected fun userNotFound(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/user/.*/access/$crn"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    deliusIntegration.`when`(userAccessRequest, exactly(1)).respond(
      response()
        .withDelay(Delay.seconds(delaySeconds)).withStatusCode(404),
    )
  }

  protected fun userAccessRestricted(crn: String, delaySeconds: Long = 0) {
    val userAccessUrl = "/user/.*/access/$crn"
    val userAccessRequest = request()
      .withPath(userAccessUrl)

    deliusIntegration.`when`(userAccessRequest, exactly(1)).respond(
      response().withContentType(APPLICATION_JSON).withBody(userAccessRestrictedResponse())
        .withDelay(Delay.seconds(delaySeconds)).withStatusCode(200),
    )
  }

  protected fun getDocumentResponse(crn: String, documentId: String, delaySeconds: Long = 0) {
    val documentRequest =
      request().withPath("/document/$crn/$documentId")

    deliusIntegration.`when`(documentRequest).respond(
      response()
        .withHeader(HttpHeaders.CONTENT_TYPE, "application/pdf;charset=UTF-8")
        .withHeader(HttpHeaders.ACCEPT_RANGES, "bytes")
        .withHeader(HttpHeaders.CACHE_CONTROL, "max-age=0, must-revalidate")
        .withHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"myPdfTest.pdf\"")
        .withHeader(HttpHeaders.DATE, "Fri, 05 Jul 2022 09:50:45 GMT")
        .withHeader(HttpHeaders.ETAG, "9514985635950")
        .withHeader(HttpHeaders.LAST_MODIFIED, "Wed, 03 Jul 2022 13:20:35 GMT")
        .withBody(ClassPathResource("myPdfTest.pdf").file.readBytes())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun noDocumentAvailable(crn: String, documentId: String) {
    val documentRequest =
      request().withPath("/document/$crn/$documentId")

    deliusIntegration.`when`(documentRequest, exactly(1)).respond(
      response().withStatusCode(404),
    )
  }

  protected fun cvlLicenceMatchResponse(
    nomisId: String,
    crn: String,
    delaySeconds: Long = 0,
    licenceStatus: String? = "IN_PROGRESS",
    licenceId: Int? = 123344,
  ) {
    val licenceMatchRequest =
      request().withPath("/licence/match")

    cvlApi.`when`(licenceMatchRequest).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(licenceMatchResponse(nomisId, crn, licenceStatus, licenceId))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun documentManagementApiUploadResponse(
    documentUuid: String,
    delaySeconds: Long = 0,
  ) {
    val documentManagementApiRequest = request()
      .withMethod("POST")
      .withPath("/documents/PPUD_RECALL/.*")

    documentManagementApi.`when`(documentManagementApiRequest).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody("""{"documentUuid": "$documentUuid"}""")
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun documentManagementApiDownloadResponse(responseBody: String = "hello there!", delaySeconds: Long = 0) {
    val responseBodyBytes = responseBody.toByteArray()

    val documentManagementApiRequest = request()
      .withMethod("GET")
      .withPath("/documents/.*")

    documentManagementApi.`when`(documentManagementApiRequest).respond(
      response().withStatusCode(200)
        .withBody(responseBodyBytes)
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun documentManagementApiDeleteResponse(delaySeconds: Long = 0) {
    val requestMatcher = request()
      .withMethod("DELETE")
      .withPath("/documents/.*")

    val response = response()
      .withStatusCode(204)
      .withDelay(TimeUnit.SECONDS, delaySeconds)

    documentManagementApi
      .`when`(requestMatcher)
      .respond(response)
  }

  protected fun prisonApiOffenderMatchResponse(
    nomsId: String,
    description: String,
    facialImageId: String,
    agencyId: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/api/offenders/" + nomsId)

    prisonApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(prisonResponse(description, facialImageId, agencyId))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun prisonApiImageResponse(
    facialImageId: String,
    data: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/api/images/" + facialImageId + "/data")

    prisonApi.`when`(request).respond(
      response().withContentType(JPEG)
        .withBody(data.toByteArray())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun mockPrisonApiAgencyResponse(
    agencyId: String,
    agency: Agency,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/api/agencies/$agencyId").withQueryStringParameter("activeOnly", "false")

    prisonApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(agency.toJson())
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationSearchApiMatchResponse(
    nomsId: String,
    croNumber: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/search")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationSearchResponse(nomsId, croNumber))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationDetailsMatchResponse(
    id: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/" + id)

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationDetailsResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationBookRecallApiMatchResponse(
    nomsId: String,
    id: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/$nomsId/recall")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationBookRecallResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationCreateOffenderApiMatchResponse(
    id: String,
    createOffenderRequest: PpudCreateOffenderRequest,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender").withBody(createOffenderRequest.toJsonBody())

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationCreateOffenderResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationUpdateOffenderApiMatchResponse(
    offenderId: String,
    updateOffenderRequest: PpudUpdateOffenderRequest,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/$offenderId").withBody(updateOffenderRequest.toJsonBody())

    ppudAutomationApi.`when`(request).respond(
      response().withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationCreateSentenceApiMatchResponse(
    offenderId: String,
    createSentenceRequest: PpudCreateOrUpdateSentenceRequest,
    id: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/$offenderId/sentence").withBody(createSentenceRequest.toJson())

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationCreateSentenceResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationUpdateSentenceApiMatchResponse(
    offenderId: String,
    sentenceId: String,
    updateSentenceRequest: PpudCreateOrUpdateSentenceRequest,
    delaySeconds: Long = 0,
  ) {
    val request =
      request().withPath("/offender/$offenderId/sentence/$sentenceId").withBody(updateSentenceRequest.toJson())

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationUpdateOffenceApiMatchResponse(
    offenderId: String,
    sentenceId: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/" + offenderId + "/sentence/" + sentenceId + "/offence")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationUpdateReleaseApiMatchResponse(
    offenderId: String,
    sentenceId: String,
    updateReleaseRequest: PpudCreateOrUpdateReleaseRequest,
    id: String,
    delaySeconds: Long = 0,
  ) {
    val request =
      request().withPath("/offender/$offenderId/sentence/$sentenceId/release").withBody(updateReleaseRequest.toJson())

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationUpdateReleaseResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationCreateRecallApiMatchResponse(
    offenderId: String,
    releaseId: String,
    id: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/offender/" + offenderId + "/release/" + releaseId + "/recall")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationCreateRecallResponse(id))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationUploadMandatoryDocumentApiMatchResponse(
    recallId: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/recall/$recallId/mandatory-document")

    ppudAutomationApi.`when`(request).respond(
      response().withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationUploadAdditionalDocumentApiMatchResponse(
    recallId: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/recall/$recallId/additional-document")

    ppudAutomationApi.`when`(request).respond(
      response().withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationCreateMinuteApiMatchResponse(
    recallId: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/recall/$recallId/minutes")

    ppudAutomationApi.`when`(request).respond(
      response().withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationReferenceListApiMatchResponse(
    name: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/reference/$name")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody("{ \"values\": [\"one\",\"two\"] }")
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun ppudAutomationSearchActiveUsersApiMatchResponse(
    userFullName: String,
    userName: String,
    teamName: String,
    delaySeconds: Long = 0,
  ) {
    val request = request().withPath("/user/search")

    ppudAutomationApi.`when`(request).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(ppudAutomationSearchActiveUsersResponse(userFullName, teamName))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  protected fun cvlLicenceByIdResponse(
    licenceId: Int,
    nomisId: String,
    crn: String,
    delaySeconds: Long = 0,
    licenceStartDate: String? = "14/06/2022",
  ) {
    val licenceIdRequesst =
      request().withPath("/licence/id/$licenceId")

    cvlApi.`when`(licenceIdRequesst).respond(
      response().withContentType(APPLICATION_JSON)
        .withBody(licenceIdResponse(licenceId, nomisId, crn, licenceStartDate))
        .withDelay(Delay.seconds(delaySeconds)),
    )
  }

  @Suppress("SameParameterValue")
  protected fun cvlLicenceByIdResponseThrowsException(licenceId: Int) {
    val licenceIdRequesst =
      request().withPath("/licence/id/$licenceId")

    cvlApi.`when`(licenceIdRequesst).respond(
      response().withStatusCode(500),
    )
  }

  fun setupOauth() {
    oauthMock
      .`when`(request().withPath("/auth/oauth/token"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("access_token" to "ABCDE", "token_type" to "bearer"))),
      )
  }

  fun setupHealthChecks() {
    oauthMock
      .`when`(request().withPath("/auth/health/ping"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "OK"))),
      )

    deliusIntegration
      .`when`(request().withPath("/health"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "OK"))),
      )

    gotenbergMock
      .`when`(request().withPath("/health"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "up"))),
      )

    ppudAutomationApi
      .`when`(request().withPath("/health/ping"))
      .respond(
        response()
          .withContentType(APPLICATION_JSON)
          .withBody(gson.toJson(mapOf("status" to "UP"))),
      )
  }
}
