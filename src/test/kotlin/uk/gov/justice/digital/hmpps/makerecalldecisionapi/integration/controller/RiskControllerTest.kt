package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RiskControllerTest(
  @Value("\${oasys.arn.client.timeout}") private val oasysArnClientTimeout: Long,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  @Test
  fun `retrieves risk data when no mappa details or historical scores available`() {
    runBlockingTest {
      val crn = "A12345"
      roSHSummaryResponse(crn)
      allOffenderDetailsResponse(crn)
      noMappaDetailsResponse(crn)
      noHistoricalRiskScoresResponse(crn)

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
        .jsonPath("$.mappa.level").isEqualTo("")
        .jsonPath("$.mappa.isNominal").isEqualTo(true)
        .jsonPath("$.mappa.lastUpdated").isEqualTo("")
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
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.level").isEqualTo("MEDIUM")
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.score").isEqualTo(40)
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.type").isEqualTo("OGRS")
//        .jsonPath("$.predictorScores.current.RSR.type").isEqualTo("RSR")
//        .jsonPath("$.predictorScores.current.RSR.level").isEqualTo("HIGH")
//        .jsonPath("$.predictorScores.current.RSR.score").isEqualTo(23)
//        .jsonPath("$.predictorScores.current.OSPC.type").isEqualTo("OSP/C")
//        .jsonPath("$.predictorScores.current.OSPC.level").isEqualTo("LOW")
//        .jsonPath("$.predictorScores.current.OSPC.score").isEqualTo(3.45)
//        .jsonPath("$.predictorScores.current.OSPI.type").isEqualTo("OSP/I")
//        .jsonPath("$.predictorScores.current.OSPI.level").isEqualTo("MEDIUM")
//        .jsonPath("$.predictorScores.current.OSPI.score").isEqualTo(5)
//        .jsonPath("$.predictorScores.current.OGRS.type").isEqualTo("RSR")
//        .jsonPath("$.predictorScores.current.OGRS.level").isEqualTo("LOW")
//        .jsonPath("$.predictorScores.current.OGRS.score").isEqualTo(12)
//        .jsonPath("$.contingencyPlan.oasysHeading.number").isEqualTo(11.9)
//        .jsonPath("$.contingencyPlan.oasysHeading.description").isEqualTo("Contingency plan")
//        .jsonPath("$.contingencyPlan.description").isEqualTo(
//          "If Mr Edwin enters enters pubs in Enfield Town - issue licence compliance letter\nIf Mr Edwin associates with Mr Daniels, Mr Moreland or Mr Barksdale - issue decision not to recall letter or recall. Supervision session to discuss reasons for association.\nIf Mr Edwin loses his accommodation, refer to housing support. \nIf Mr Edwin loses his employment, refer to ETE services to establish alternative employment\nIf Mr Edwin returns to drinking or taking drugs, cosndier increase in MAPPA level, refer to CGL support, increase reporting or recall."
//        )
    }
  }

  @Test
  fun `retrieves risk data`() {
    runBlockingTest {
      val crn = "A12345"
      roSHSummaryResponse(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      historicalRiskScoresResponse(crn)

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
        .jsonPath("$.mappa.level").isEqualTo("MAPPA Level 1")
        .jsonPath("$.mappa.isNominal").isEqualTo(true)
        .jsonPath("$.mappa.lastUpdated").isEqualTo("10 May 2021")
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
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.level").isEqualTo("MEDIUM")
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.score").isEqualTo(40)
//        .jsonPath("$.predictorScores.historical[0].scores.OGRS.type").isEqualTo("OGRS")
//        .jsonPath("$.predictorScores.current.RSR.type").isEqualTo("RSR")
//        .jsonPath("$.predictorScores.current.RSR.level").isEqualTo("HIGH")
//        .jsonPath("$.predictorScores.current.RSR.score").isEqualTo(23)
//        .jsonPath("$.predictorScores.current.OSPC.type").isEqualTo("OSP/C")
//        .jsonPath("$.predictorScores.current.OSPC.level").isEqualTo("LOW")
//        .jsonPath("$.predictorScores.current.OSPC.score").isEqualTo(3.45)
//        .jsonPath("$.predictorScores.current.OSPI.type").isEqualTo("OSP/I")
//        .jsonPath("$.predictorScores.current.OSPI.level").isEqualTo("MEDIUM")
//        .jsonPath("$.predictorScores.current.OSPI.score").isEqualTo(5)
//        .jsonPath("$.predictorScores.current.OGRS.type").isEqualTo("RSR")
//        .jsonPath("$.predictorScores.current.OGRS.level").isEqualTo("LOW")
//        .jsonPath("$.predictorScores.current.OGRS.score").isEqualTo(12)
//        .jsonPath("$.contingencyPlan.oasysHeading.number").isEqualTo(11.9)
//        .jsonPath("$.contingencyPlan.oasysHeading.description").isEqualTo("Contingency plan")
//        .jsonPath("$.contingencyPlan.description").isEqualTo(
//          "If Mr Edwin enters enters pubs in Enfield Town - issue licence compliance letter\nIf Mr Edwin associates with Mr Daniels, Mr Moreland or Mr Barksdale - issue decision not to recall letter or recall. Supervision session to discuss reasons for association.\nIf Mr Edwin loses his accommodation, refer to housing support. \nIf Mr Edwin loses his employment, refer to ETE services to establish alternative employment\nIf Mr Edwin returns to drinking or taking drugs, cosndier increase in MAPPA level, refer to CGL support, increase reporting or recall."
//        )
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runBlockingTest {
      val crn = "X123456"
      webTestClient.get()
        .uri("/cases/$crn/risk")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }

  @Test
  fun `gateway timeout 503 given on OASYS ARN Api riskSummary endpoint`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
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
  fun `gateway timeout 503 given on OASYS ARN historical scores endpoint`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      roSHSummaryResponse(crn)
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
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout`() {
    runBlockingTest {
      val crn = "A12345"
      roSHSummaryResponse(crn)
      allOffenderDetailsResponse(crn)
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
