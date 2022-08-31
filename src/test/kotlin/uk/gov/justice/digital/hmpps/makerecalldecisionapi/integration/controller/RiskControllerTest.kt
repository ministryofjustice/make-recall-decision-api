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

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RiskControllerTest(
  @Value("\${oasys.arn.client.timeout}") private val oasysArnClientTimeout: Long,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  @Test
  fun `retrieves personal details data when no MAPPA, RoSH, or Contingency plan details available`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noMappaDetailsResponse(crn)
      noHistoricalRiskScoresResponse(crn)
      noCurrentRiskScoresResponse(crn)
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
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.description").isEqualTo("What circumstances are likely to increase the risk?")
        .jsonPath("$.circumstancesIncreaseRisk.description").isEqualTo("")
        .jsonPath("$.factorsToReduceRisk.oasysHeading.number").isEqualTo(10.5)
        .jsonPath("$.factorsToReduceRisk.oasysHeading.description").isEqualTo("What factors are likely to reduce the risk?")
        .jsonPath("$.factorsToReduceRisk.description").isEqualTo("")
        .jsonPath("$.whenRiskHighest.oasysHeading.number").isEqualTo(10.3)
        .jsonPath("$.whenRiskHighest.oasysHeading.description").isEqualTo("When is the risk likely to be greatest?")
        .jsonPath("$.whenRiskHighest.description").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].date").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.level").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.score").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.level").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.score").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.level").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.score").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.type").isEqualTo("OSP/I")
        // TODO not available from rsr/history - TBD
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.level").isEqualTo("")
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.score").isEqualTo("")
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.current.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.current.RSR.level").isEqualTo("")
        .jsonPath("$.predictorScores.current.RSR.score").isEqualTo("")
        .jsonPath("$.predictorScores.current.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.current.OSPC.level").isEqualTo("")
        .jsonPath("$.predictorScores.current.OSPC.score").isEqualTo("")
        .jsonPath("$.predictorScores.current.OSPI.type").isEqualTo("OSP/I")
        .jsonPath("$.predictorScores.current.OSPI.level").isEqualTo("")
        .jsonPath("$.predictorScores.current.OSPI.score").isEqualTo("")
        .jsonPath("$.predictorScores.current.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.current.OGRS.level").isEqualTo("")
        .jsonPath("$.predictorScores.current.OGRS.score").isEqualTo("")
        .jsonPath("$.contingencyPlan.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.contingencyPlan.oasysHeading.description").isEqualTo("Contingency plan")
        .jsonPath("$.contingencyPlan.description").isEqualTo("")
    }
  }

  @Test
  fun `retrieves personal details data when no MAPPA, or Contingency plan details available and ARN RoSHSummary fetch fails`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noMappaDetailsResponse(crn)
      failedHistoricalRiskScoresResponse(crn)
      failedCurrentRiskScoresResponse(crn)
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
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.description").isEqualTo("What circumstances are likely to increase the risk?")
        .jsonPath("$.circumstancesIncreaseRisk.description").isEqualTo("")
        .jsonPath("$.factorsToReduceRisk.oasysHeading.number").isEqualTo(10.5)
        .jsonPath("$.factorsToReduceRisk.oasysHeading.description").isEqualTo("What factors are likely to reduce the risk?")
        .jsonPath("$.factorsToReduceRisk.description").isEqualTo("")
        .jsonPath("$.whenRiskHighest.oasysHeading.number").isEqualTo(10.3)
        .jsonPath("$.whenRiskHighest.oasysHeading.description").isEqualTo("When is the risk likely to be greatest?")
        .jsonPath("$.whenRiskHighest.description").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].date").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.level").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.score").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.level").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.score").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.level").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.score").isEqualTo("")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.type").isEqualTo("OSP/I")
        // TODO not available from rsr/history - TBD
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.level").isEqualTo("")
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.score").isEqualTo("")
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.current.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.current.RSR.level").isEqualTo("")
        .jsonPath("$.predictorScores.current.RSR.score").isEqualTo("")
        .jsonPath("$.predictorScores.current.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.current.OSPC.level").isEqualTo("")
        .jsonPath("$.predictorScores.current.OSPC.score").isEqualTo("")
        .jsonPath("$.predictorScores.current.OSPI.type").isEqualTo("OSP/I")
        .jsonPath("$.predictorScores.current.OSPI.level").isEqualTo("")
        .jsonPath("$.predictorScores.current.OSPI.score").isEqualTo("")
        .jsonPath("$.predictorScores.current.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.current.OGRS.level").isEqualTo("")
        .jsonPath("$.predictorScores.current.OGRS.score").isEqualTo("")
        .jsonPath("$.contingencyPlan.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.contingencyPlan.oasysHeading.description").isEqualTo("Contingency plan")
        .jsonPath("$.contingencyPlan.description").isEqualTo("")
    }
  }

  @Test
  fun `retrieves risk data`() {
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      roSHSummaryResponse(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      historicalRiskScoresResponse(crn)
      currentRiskScoresResponse(crn)
      deleteAndCreateRecommendation()
      contingencyPlanResponse(crn)

      val arnContingencyPlanResponse = JSONObject(contingencyPlanResponse())
      val assessmentsFromArnContingencyPlanResponse = JSONArray(arnContingencyPlanResponse.get("assessments").toString())
      val latestCompleteAssessment = JSONObject(assessmentsFromArnContingencyPlanResponse.get(0).toString())

      val expectedContingencyPlanDescription = latestCompleteAssessment.get("keyConsiderationsCurrentSituation").toString() + latestCompleteAssessment.get("furtherConsiderationsCurrentSituation").toString() + latestCompleteAssessment.get("monitoringAndControl").toString() + latestCompleteAssessment.get("interventionsAndTreatment").toString() + latestCompleteAssessment.get("victimSafetyPlanning").toString()

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
        .jsonPath("$.circumstancesIncreaseRisk.oasysHeading.description").isEqualTo("What circumstances are likely to increase the risk?")
        .jsonPath("$.circumstancesIncreaseRisk.description").isEqualTo("If offender in situation X the risk can be higher")
        .jsonPath("$.factorsToReduceRisk.oasysHeading.number").isEqualTo(10.5)
        .jsonPath("$.factorsToReduceRisk.oasysHeading.description").isEqualTo("What factors are likely to reduce the risk?")
        .jsonPath("$.factorsToReduceRisk.description").isEqualTo("Giving offender therapy in X will reduce the risk")
        .jsonPath("$.whenRiskHighest.oasysHeading.number").isEqualTo(10.3)
        .jsonPath("$.whenRiskHighest.oasysHeading.description").isEqualTo("When is the risk likely to be greatest?")
        .jsonPath("$.whenRiskHighest.description").isEqualTo("the risk is imminent and more probably in X situation")
        .jsonPath("$.predictorScores.historical[0].date").isEqualTo("12 September 2018 12:00")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.score").isEqualTo(18)
        .jsonPath("$.predictorScores.historical[0].scores.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.score").isEqualTo(6.2)
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.level").isEqualTo("MEDIUM")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.score").isEqualTo(8.1)
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.type").isEqualTo("OSP/I")
        // TODO not available from rsr/history - TBD
        //        .jsonPath("$.predictorScores.historical[0].scores.OGRS.level").isEqualTo("MEDIUM")
        //        .jsonPath("$.predictorScores.historical[0].scores.OGRS.score").isEqualTo(40)
        //        .jsonPath("$.predictorScores.historical[0].scores.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.current.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.current.RSR.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.current.RSR.score").isEqualTo(23)
        .jsonPath("$.predictorScores.current.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.current.OSPC.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.OSPC.score").isEqualTo(3.45)
        .jsonPath("$.predictorScores.current.OSPI.type").isEqualTo("OSP/I")
        .jsonPath("$.predictorScores.current.OSPI.level").isEqualTo("MEDIUM")
        .jsonPath("$.predictorScores.current.OSPI.score").isEqualTo(5)
        .jsonPath("$.predictorScores.current.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.current.OGRS.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.OGRS.score").isEqualTo(12)
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.contingencyPlan.oasysHeading.number").isEqualTo(10.1)
        .jsonPath("$.contingencyPlan.oasysHeading.description").isEqualTo("Contingency plan")
        .jsonPath("$.contingencyPlan.description").isEqualTo(expectedContingencyPlanDescription)
    }
  }

  @Test
  fun `not found when person does not exist`() {
    val crn = "A12345"
    userAccessAllowed(crn)
    roSHSummaryResponse(crn)
    mappaDetailsResponse(crn)
    historicalRiskScoresResponse(crn)
    currentRiskScoresResponse(crn)
    noOffenderDetailsResponse(crn)
    // TODO reintroduce once ARN-1026 is complete
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
  fun `gateway timeout 503 given on OASYS ARN current scores endpoint`() {
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      roSHSummaryResponse(crn)
      historicalRiskScoresResponse(crn)
      contingencyPlanResponse(crn)
      currentRiskScoresResponse(crn, delaySeconds = oasysArnClientTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: ARN API Client - current scores endpoint: [No response within $oasysArnClientTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on OASYS ARN historical scores endpoint`() {
    runTest {
      val crn = "A12345"
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      roSHSummaryResponse(crn)
      currentRiskScoresResponse(crn)
      contingencyPlanResponse(crn)
      historicalRiskScoresResponse(crn, delaySeconds = oasysArnClientTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: ARN API Client - historical scores endpoint: [No response within $oasysArnClientTimeout seconds]")
    }

    @Test
    fun `gateway timeout 503 given on OASYS ARN contingency plan endpoint`() {
      runTest {
        val crn = "A12345"
        userAccessAllowed(crn)
        allOffenderDetailsResponse(crn)
        mappaDetailsResponse(crn)
        roSHSummaryResponse(crn)
        currentRiskScoresResponse(crn)
        historicalRiskScoresResponse(crn)
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
            .jsonPath("$.userAccessResponse.restrictionMessage").isEqualTo("You are restricted from viewing this offender record. Please contact OM John Smith")
            .jsonPath("$.userAccessResponse.exclusionMessage").isEmpty
            .jsonPath("$.personalDetailsOverview").isEmpty
        }
      }

      @Test
      fun `gateway timeout 503 given on Community Api timeout`() {
        runTest {
          val crn = "A12345"
          userAccessAllowed(crn)
          roSHSummaryResponse(crn)
          allOffenderDetailsResponse(crn)
          currentRiskScoresResponse(crn)
          historicalRiskScoresResponse(crn)
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
        }
      }
    }
  }
}
