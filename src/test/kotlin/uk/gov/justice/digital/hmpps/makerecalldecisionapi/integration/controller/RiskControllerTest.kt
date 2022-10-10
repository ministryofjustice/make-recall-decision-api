package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn.contingencyPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RiskControllerTest(
  @Value("\${oasys.arn.client.timeout}") private val oasysArnClientTimeout: Long,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  @Test
  fun `retrieves risk summary when no MAPPA, RoSH, or Contingency plan details available and Risk Scores not found`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noMappaDetailsResponse(crn)
      noRiskScoresResponse(crn)
      noRoSHSummaryResponse(crn)
      noContingencyPlanResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.riskOfSeriousHarm.overallRisk").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.riskToChildren").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.riskToPublic").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.riskToKnownAdult").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.riskToStaff").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.lastUpdated").isEqualTo("")
        .jsonPath("$.mappa.level").isEqualTo(null)
        .jsonPath("$.mappa.isNominal").isEqualTo(true)
        .jsonPath("$.mappa.lastUpdated").isEqualTo("")
        .jsonPath("$.mappa.category").isEqualTo(null)
        .jsonPath("$.natureOfRisk.oasysHeading.number").isEqualTo(10.2)
        .jsonPath("$.natureOfRisk.oasysHeading.description").isEqualTo("What is the nature of the risk?")
        .jsonPath("$.natureOfRisk.description").isEqualTo("")
        .jsonPath("$.whoIsAtRisk.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.whoIsAtRisk.oasysHeading.description").isEqualTo("Who is at risk?")
        .jsonPath("$.whoIsAtRisk.description").isEqualTo("")
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.number").isEqualTo(10.4)
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.description")
        .isEqualTo("What circumstances are likely to increase the risk?")
        .jsonPath("$.circumstancesIncreaseRisk.description").isEqualTo("")
        .jsonPath("$.factorsToReduceRisk.oasysHeading.number").isEqualTo(10.5)
        .jsonPath("$.factorsToReduceRisk.oasysHeading.description")
        .isEqualTo("What factors are likely to reduce the risk?")
        .jsonPath("$.factorsToReduceRisk.description").isEqualTo("")
        .jsonPath("$.whenRiskHighest.oasysHeading.number").isEqualTo(10.3)
        .jsonPath("$.whenRiskHighest.oasysHeading.description").isEqualTo("When is the risk likely to be greatest?")
        .jsonPath("$.whenRiskHighest.description").isEqualTo("")
        .jsonPath("$.contingencyPlan.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.contingencyPlan.oasysHeading.description").isEqualTo("Contingency plan")
        .jsonPath("$.contingencyPlan.description").isEqualTo("")
        .jsonPath("$.predictorScores.error").isEqualTo("NOT_FOUND")
    }
  }

  @Test
  fun `retrieves risk summary when no MAPPA, or Contingency plan details available, ARN RoSHSummary fetch fails, and Risk Scores fetch fails`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noMappaDetailsResponse(crn)
      failedRiskScoresResponse(crn)
      failedRoSHSummaryResponse(crn)
      failedContingencyPlanResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.riskOfSeriousHarm.overallRisk").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.riskToChildren").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.riskToPublic").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.riskToKnownAdult").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.riskToStaff").isEqualTo("")
        .jsonPath("$.riskOfSeriousHarm.lastUpdated").isEqualTo("")
        .jsonPath("$.mappa.level").isEqualTo(null)
        .jsonPath("$.mappa.isNominal").isEqualTo(true)
        .jsonPath("$.mappa.lastUpdated").isEqualTo("")
        .jsonPath("$.mappa.category").isEqualTo(null)
        .jsonPath("$.natureOfRisk.oasysHeading.number").isEqualTo(10.2)
        .jsonPath("$.natureOfRisk.oasysHeading.description").isEqualTo("What is the nature of the risk?")
        .jsonPath("$.natureOfRisk.description").isEqualTo("")
        .jsonPath("$.whoIsAtRisk.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.whoIsAtRisk.oasysHeading.description").isEqualTo("Who is at risk?")
        .jsonPath("$.whoIsAtRisk.description").isEqualTo("")
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.number").isEqualTo(10.4)
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.description")
        .isEqualTo("What circumstances are likely to increase the risk?")
        .jsonPath("$.circumstancesIncreaseRisk.description").isEqualTo("")
        .jsonPath("$.factorsToReduceRisk.oasysHeading.number").isEqualTo(10.5)
        .jsonPath("$.factorsToReduceRisk.oasysHeading.description")
        .isEqualTo("What factors are likely to reduce the risk?")
        .jsonPath("$.factorsToReduceRisk.description").isEqualTo("")
        .jsonPath("$.whenRiskHighest.oasysHeading.number").isEqualTo(10.3)
        .jsonPath("$.whenRiskHighest.oasysHeading.description").isEqualTo("When is the risk likely to be greatest?")
        .jsonPath("$.whenRiskHighest.description").isEqualTo("")
        .jsonPath("$.contingencyPlan.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.contingencyPlan.oasysHeading.description").isEqualTo("Contingency plan")
        .jsonPath("$.contingencyPlan.description").isEqualTo("")
        .jsonPath("$.predictorScores.error").isEqualTo("SERVER_ERROR")
    }
  }

  @Test
  fun `retrieves risk data when ARN Scores are null`() {
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      oasysAssessmentsResponse(crn)
      roSHSummaryResponse(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      allRiskScoresEmptyResponse(crn)
      deleteAndCreateRecommendation()
      updateRecommendation(Status.DRAFT)
      contingencyPlanResponse(crn)

      val arnContingencyPlanResponse = JSONObject(contingencyPlanResponse())
      val assessmentsFromArnContingencyPlanResponse =
        JSONArray(arnContingencyPlanResponse.get("assessments").toString())
      val latestCompleteAssessment = JSONObject(assessmentsFromArnContingencyPlanResponse.get(0).toString())

      val expectedContingencyPlanDescription = latestCompleteAssessment.get("keyConsiderationsCurrentSituation")
        .toString() + latestCompleteAssessment.get("furtherConsiderationsCurrentSituation")
        .toString() + latestCompleteAssessment.get("monitoringAndControl")
        .toString() + latestCompleteAssessment.get("interventionsAndTreatment")
        .toString() + latestCompleteAssessment.get("victimSafetyPlanning").toString()

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.riskOfSeriousHarm.overallRisk").isEqualTo("HIGH")
        .jsonPath("$.riskOfSeriousHarm.riskToChildren").isEqualTo("HIGH")
        .jsonPath("$.riskOfSeriousHarm.riskToPublic").isEqualTo("HIGH")
        .jsonPath("$.riskOfSeriousHarm.riskToKnownAdult").isEqualTo("HIGH")
        .jsonPath("$.riskOfSeriousHarm.riskToStaff").isEqualTo("MEDIUM")
        .jsonPath("$.riskOfSeriousHarm.lastUpdated").isEqualTo("2022-05-19")
        .jsonPath("$.mappa.level").isEqualTo(1)
        .jsonPath("$.mappa.isNominal").isEqualTo(true)
        .jsonPath("$.mappa.lastUpdated").isEqualTo("10 May 2021")
        .jsonPath("$.mappa.category").isEqualTo("0")
        .jsonPath("$.natureOfRisk.oasysHeading.number").isEqualTo(10.2)
        .jsonPath("$.natureOfRisk.oasysHeading.description").isEqualTo("What is the nature of the risk?")
        .jsonPath("$.natureOfRisk.description").isEqualTo("The nature of the risk is X")
        .jsonPath("$.whoIsAtRisk.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.whoIsAtRisk.oasysHeading.description").isEqualTo("Who is at risk?")
        .jsonPath("$.whoIsAtRisk.description").isEqualTo("X, Y and Z are at risk")
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.number").isEqualTo(10.4)
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.description")
        .isEqualTo("What circumstances are likely to increase the risk?")
        .jsonPath("$.circumstancesIncreaseRisk.description")
        .isEqualTo("If offender in situation X the risk can be higher")
        .jsonPath("$.factorsToReduceRisk.oasysHeading.number").isEqualTo(10.5)
        .jsonPath("$.factorsToReduceRisk.oasysHeading.description")
        .isEqualTo("What factors are likely to reduce the risk?")
        .jsonPath("$.factorsToReduceRisk.description").isEqualTo("Giving offender therapy in X will reduce the risk")
        .jsonPath("$.whenRiskHighest.oasysHeading.number").isEqualTo(10.3)
        .jsonPath("$.whenRiskHighest.oasysHeading.description").isEqualTo("When is the risk likely to be greatest?")
        .jsonPath("$.whenRiskHighest.description").isEqualTo("the risk is imminent and more probably in X situation")
        .jsonPath("$.predictorScores.error").isEqualTo(EMPTY_STRING)
        .jsonPath("$.predictorScores.current.date").isEqualTo("2021-06-16")
        .jsonPath("$.predictorScores.current.scores.RSR").isEqualTo(null)
        .jsonPath("$.predictorScores.current.scores.OGP").isEqualTo(null)
        .jsonPath("$.predictorScores.current.scores.OVP").isEqualTo(null)
        .jsonPath("$.predictorScores.current.scores.OSPC").isEqualTo(null)
        .jsonPath("$.predictorScores.current.scores.OSPI").isEqualTo(null)
        .jsonPath("$.predictorScores.current.scores.OGRS").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[0].date").isEqualTo("2020-06-16")
        .jsonPath("$.predictorScores.historical[0].scores.OGP").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[0].scores.OVP").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[0].scores.RSR").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[0].scores.OSPC").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[0].scores.OSPI").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[0].scores.OGRS").isEqualTo(null)
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.contingencyPlan.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.contingencyPlan.oasysHeading.description").isEqualTo("Contingency plan")
        .jsonPath("$.contingencyPlan.description").isEqualTo(expectedContingencyPlanDescription)
        .jsonPath("$.assessmentStatus").isEqualTo("COMPLETE")
    }
  }

  @Test
  fun `retrieves risk data`() {
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      oasysAssessmentsResponse(crn)
      roSHSummaryResponse(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      allRiskScoresResponse(crn)
      deleteAndCreateRecommendation()
      updateRecommendation(Status.DRAFT)
      contingencyPlanResponse(crn)

      val arnContingencyPlanResponse = JSONObject(contingencyPlanResponse())
      val assessmentsFromArnContingencyPlanResponse =
        JSONArray(arnContingencyPlanResponse.get("assessments").toString())
      val latestCompleteAssessment = JSONObject(assessmentsFromArnContingencyPlanResponse.get(0).toString())

      val expectedContingencyPlanDescription = latestCompleteAssessment.get("keyConsiderationsCurrentSituation")
        .toString() + latestCompleteAssessment.get("furtherConsiderationsCurrentSituation")
        .toString() + latestCompleteAssessment.get("monitoringAndControl")
        .toString() + latestCompleteAssessment.get("interventionsAndTreatment")
        .toString() + latestCompleteAssessment.get("victimSafetyPlanning").toString()

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.riskOfSeriousHarm.overallRisk").isEqualTo("HIGH")
        .jsonPath("$.riskOfSeriousHarm.riskToChildren").isEqualTo("HIGH")
        .jsonPath("$.riskOfSeriousHarm.riskToPublic").isEqualTo("HIGH")
        .jsonPath("$.riskOfSeriousHarm.riskToKnownAdult").isEqualTo("HIGH")
        .jsonPath("$.riskOfSeriousHarm.riskToStaff").isEqualTo("MEDIUM")
        .jsonPath("$.riskOfSeriousHarm.lastUpdated").isEqualTo("2022-05-19")
        .jsonPath("$.mappa.level").isEqualTo(1)
        .jsonPath("$.mappa.isNominal").isEqualTo(true)
        .jsonPath("$.mappa.lastUpdated").isEqualTo("10 May 2021")
        .jsonPath("$.mappa.category").isEqualTo("0")
        .jsonPath("$.natureOfRisk.oasysHeading.number").isEqualTo(10.2)
        .jsonPath("$.natureOfRisk.oasysHeading.description").isEqualTo("What is the nature of the risk?")
        .jsonPath("$.natureOfRisk.description").isEqualTo("The nature of the risk is X")
        .jsonPath("$.whoIsAtRisk.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.whoIsAtRisk.oasysHeading.description").isEqualTo("Who is at risk?")
        .jsonPath("$.whoIsAtRisk.description").isEqualTo("X, Y and Z are at risk")
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.number").isEqualTo(10.4)
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.description")
        .isEqualTo("What circumstances are likely to increase the risk?")
        .jsonPath("$.circumstancesIncreaseRisk.description")
        .isEqualTo("If offender in situation X the risk can be higher")
        .jsonPath("$.factorsToReduceRisk.oasysHeading.number").isEqualTo(10.5)
        .jsonPath("$.factorsToReduceRisk.oasysHeading.description")
        .isEqualTo("What factors are likely to reduce the risk?")
        .jsonPath("$.factorsToReduceRisk.description").isEqualTo("Giving offender therapy in X will reduce the risk")
        .jsonPath("$.whenRiskHighest.oasysHeading.number").isEqualTo(10.3)
        .jsonPath("$.whenRiskHighest.oasysHeading.description").isEqualTo("When is the risk likely to be greatest?")
        .jsonPath("$.whenRiskHighest.description").isEqualTo("the risk is imminent and more probably in X situation")
        .jsonPath("$.predictorScores.error").isEqualTo(EMPTY_STRING)
        .jsonPath("$.predictorScores.current.date").isEqualTo("2022-04-16")
        .jsonPath("$.predictorScores.current.scores.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.current.scores.RSR.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.current.scores.RSR.score").isEqualTo(23)
        .jsonPath("$.predictorScores.current.scores.OGP.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.scores.OGP.type").isEqualTo("OGP")
        .jsonPath("$.predictorScores.current.scores.OGP.ogp1Year").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OGP.ogp2Year").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OVP.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.scores.OVP.type").isEqualTo("OVP")
        .jsonPath("$.predictorScores.current.scores.OVP.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OVP.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.current.scores.OSPC.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.scores.OSPC.score").isEqualTo(3.45)
        .jsonPath("$.predictorScores.current.scores.OSPI.type").isEqualTo("OSP/I")
        .jsonPath("$.predictorScores.current.scores.OSPI.level").isEqualTo("MEDIUM")
        .jsonPath("$.predictorScores.current.scores.OSPI.score").isEqualTo(5)
        .jsonPath("$.predictorScores.current.scores.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.current.scores.OGRS.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.scores.OGRS.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OGRS.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].date").isEqualTo("2021-06-16")
        .jsonPath("$.predictorScores.historical[0].scores.OGP.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[0].scores.OGP.type").isEqualTo("OGP")
        .jsonPath("$.predictorScores.historical[0].scores.OGP.ogp1Year").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OGP.ogp2Year").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OVP.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[0].scores.OVP.type").isEqualTo("OVP")
        .jsonPath("$.predictorScores.historical[0].scores.OVP.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OVP.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.RSR.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.score").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.score").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.score").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.type").isEqualTo("OSP/I")
        .jsonPath("$.predictorScores.historical[0].scores.OGRS.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[0].scores.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.historical[0].scores.OGRS.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OGRS.twoYears").isEqualTo(0)
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.contingencyPlan.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.contingencyPlan.oasysHeading.description").isEqualTo("Contingency plan")
        .jsonPath("$.contingencyPlan.description").isEqualTo(expectedContingencyPlanDescription)
        .jsonPath("$.assessmentStatus").isEqualTo("COMPLETE")
    }
  }

  @Test
  fun `retrieves risk data when assessment status is incomplete`() {
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      oasysAssessmentsResponse(crn, superStatus = "INCOMPLETE")
      roSHSummaryResponse(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      allRiskScoresResponse(crn)
      deleteAndCreateRecommendation()
      updateRecommendation(Status.DRAFT)
      contingencyPlanResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.assessmentStatus").isEqualTo("INCOMPLETE")
    }
  }

  @Test
  fun `not found when person does not exist`() {
    val crn = "A12345"
    userAccessAllowed(crn)
    roSHSummaryResponse(crn)
    mappaDetailsResponse(crn)
    allRiskScoresResponse(crn)
    noOffenderDetailsResponse(crn)
    noContingencyPlanResponse(crn)

    webTestClient.get()
      .uri("/cases/$crn/risk")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.developerMessage").isEqualTo("No details available for crn: A12345")
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runTest {
      val crn = "X123456"
      userAccessAllowed(crn)
      webTestClient.get()
        .uri("/cases/$crn/risk")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }

  @Test
  fun `gateway timeout 503 given on OASYS ARN Api timeout`() {
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      contingencyPlanResponse(crn)
      roSHSummaryResponse(crn, delaySeconds = oasysArnClientTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: ARN API Client - risk summary endpoint: [No response within $oasysArnClientTimeout seconds]")
    }
  }

  @Test
  fun `Error message in predictor scores property given on OASYS ARN all scores timeout`() {
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      roSHSummaryResponse(crn)
      contingencyPlanResponse(crn)
      allRiskScoresResponse(crn, delaySeconds = oasysArnClientTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$.predictorScores.error")
        .isEqualTo("TIMEOUT")
    }
  }

  @Test
  fun `gateway timeout 503 given on OASYS ARN contingency plan endpoint`() {
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      roSHSummaryResponse(crn)
      allRiskScoresResponse(crn)
      contingencyPlanResponse(crn, delaySeconds = oasysArnClientTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: ARN API Client - risk contingency plan endpoint: [No response within $oasysArnClientTimeout seconds]")
    }

    @Test
    fun `given case is excluded then only return user access details`() {
      runTest {
        val crn = "A12345"
        userAccessRestricted(crn)

        webTestClient.get()
          .uri("/cases/$crn/risk")
          .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(true)
          .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(false)
          .jsonPath("$.userAccessResponse.restrictionMessage")
          .isEqualTo("You are restricted from viewing this offender record. Please contact OM John Smith")
          .jsonPath("$.userAccessResponse.exclusionMessage").isEmpty
          .jsonPath("$.personalDetailsOverview").isEmpty
          .jsonPath("$.mappa").isEqualTo(null)
      }
    }

    @Test
    fun `gateway timeout 503 given on Community Api timeout`() {
      runTest {
        val crn = "A12345"
        userAccessAllowed(crn)
        roSHSummaryResponse(crn)
        allOffenderDetailsResponse(crn)
        allRiskScoresResponse(crn)
        mappaDetailsResponse(crn, delaySeconds = nDeliusTimeout + 2)

        webTestClient.get()
          .uri("/cases/$crn/risk")
          .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
          .exchange()
          .expectStatus()
          .is5xxServerError
          .expectBody()
          .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
          .jsonPath("$.userMessage")
          .isEqualTo("Client timeout: Community API Client - mappa endpoint: [No response within $nDeliusTimeout seconds]")
          .jsonPath("$.mappa").isEqualTo(null)
      }
    }
  }
}
