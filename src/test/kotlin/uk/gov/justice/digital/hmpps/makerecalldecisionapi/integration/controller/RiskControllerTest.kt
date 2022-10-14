package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
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
  fun `retrieves risk summary when no MAPPA, risk scores or RoSH summary available and Risk Scores not found`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noMappaDetailsResponse(crn)
      noRiskScoresResponse(crn)
      noRoSHSummaryResponse(crn)

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
        .jsonPath("$.roshSummary.riskOfSeriousHarm").isEqualTo(null)
        .jsonPath("$.mappa.level").isEqualTo(null)
        .jsonPath("$.mappa.lastUpdatedDate").isEqualTo(null)
        .jsonPath("$.mappa.category").isEqualTo(null)
        .jsonPath("$.roshSummary.natureOfRisk").isEqualTo(null)
        .jsonPath("$.roshSummary.whoIsAtRisk").isEqualTo(null)
        .jsonPath("$.roshSummary.riskIncreaseFactors").isEqualTo(null)
        .jsonPath("$.roshSummary.riskMitigationFactors").isEqualTo(null)
        .jsonPath("$.roshSummary.riskImminence").isEqualTo(null)
        .jsonPath("$.roshSummary.error").isEqualTo("NOT_FOUND")
        .jsonPath("$.predictorScores.error").isEqualTo("NOT_FOUND")
    }
  }

  @Test
  fun `retrieves risk summary when no MAPPA available, ARN RoSH Summary fetch fails, and Risk Scores fetch fails`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noMappaDetailsResponse(crn)
      failedRiskScoresResponse(crn)
      failedRoSHSummaryResponse(crn)

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
        .jsonPath("$.roshSummary.riskOfSeriousHarm").isEqualTo(null)
        .jsonPath("$.mappa.level").isEqualTo(null)
        .jsonPath("$.mappa.lastUpdatedDate").isEqualTo(null)
        .jsonPath("$.mappa.category").isEqualTo(null)
        .jsonPath("$.roshSummary.natureOfRisk").isEqualTo(null)
        .jsonPath("$.roshSummary.whoIsAtRisk").isEqualTo(null)
        .jsonPath("$.roshSummary.riskIncreaseFactors").isEqualTo(null)
        .jsonPath("$.roshSummary.riskMitigationFactors").isEqualTo(null)
        .jsonPath("$.roshSummary.riskImminence").isEqualTo(null)
        .jsonPath("$.roshSummary.error").isEqualTo("SERVER_ERROR")
        .jsonPath("$.predictorScores.error").isEqualTo("SERVER_ERROR")
    }
  }

  @Test
  fun `retrieves risk summary when RoSH summary has empty data`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      allRiskScoresEmptyResponse(crn)
      roSHSummaryNoDataResponse(crn)

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
        .jsonPath("$.roshSummary.error").isEqualTo("NOT_FOUND")
    }
  }

  @Test
  fun `retrieves risk data when ARN Scores are null`() {
    runTest {
      userAccessAllowed(crn)
      oasysAssessmentsResponse(crn)
      roSHSummaryResponse(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      allRiskScoresEmptyResponse(crn)
      deleteAndCreateRecommendation()
      updateRecommendation(Status.DRAFT)

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
        .jsonPath("$.roshSummary.riskOfSeriousHarm.overallRisk").isEqualTo("HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToChildren").isEqualTo("HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToPublic").isEqualTo("HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToKnownAdult").isEqualTo("HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToStaff").isEqualTo("MEDIUM")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToPrisoners").isEqualTo("")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToChildren").isEqualTo("LOW")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToPublic").isEqualTo("LOW")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToKnownAdult").isEqualTo("HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToStaff").isEqualTo("VERY_HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToPrisoners").isEqualTo("VERY_HIGH")
        .jsonPath("$.mappa.level").isEqualTo(1)
        .jsonPath("$.mappa.lastUpdatedDate").isEqualTo("2021-02-10")
        .jsonPath("$.mappa.category").isEqualTo("0")
        .jsonPath("$.roshSummary.lastUpdatedDate").isEqualTo("2022-05-19T08:26:31.000Z")
        .jsonPath("$.roshSummary.natureOfRisk").isEqualTo("The nature of the risk is X")
        .jsonPath("$.roshSummary.whoIsAtRisk").isEqualTo("X, Y and Z are at risk")
        .jsonPath("$.roshSummary.riskIncreaseFactors")
        .isEqualTo("If offender in situation X the risk can be higher")
        .jsonPath("$.roshSummary.riskMitigationFactors")
        .isEqualTo("Giving offender therapy in X will reduce the risk")
        .jsonPath("$.roshSummary.riskImminence").isEqualTo("the risk is imminent and more probably in X situation")
        .jsonPath("$.predictorScores.error").isEqualTo(EMPTY_STRING)
//        .jsonPath("$.predictorScores.current.date").isEqualTo(null)
        .jsonPath("$.predictorScores.current").isEqualTo(null)
        .jsonPath("$.predictorScores.error").isEqualTo(EMPTY_STRING)
        .jsonPath("$.predictorScores.historical").isEmpty
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.assessmentStatus").isEqualTo("COMPLETE")
    }
  }

  @Test
  fun `retrieves risk data`() {
    runTest {
      userAccessAllowed(crn)
      oasysAssessmentsResponse(crn)
      roSHSummaryResponse(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      allRiskScoresResponse(crn)
      deleteAndCreateRecommendation()
      updateRecommendation(Status.DRAFT)

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
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToChildren").isEqualTo("HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToPublic").isEqualTo("HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToKnownAdult").isEqualTo("HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToStaff").isEqualTo("MEDIUM")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCommunity.riskToPrisoners").isEqualTo("")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToChildren").isEqualTo("LOW")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToPublic").isEqualTo("LOW")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToKnownAdult").isEqualTo("HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToStaff").isEqualTo("VERY_HIGH")
        .jsonPath("$.roshSummary.riskOfSeriousHarm.riskInCustody.riskToPrisoners").isEqualTo("VERY_HIGH")
        .jsonPath("$.mappa.level").isEqualTo(1)
        .jsonPath("$.mappa.lastUpdatedDate").isEqualTo("2021-02-10")
        .jsonPath("$.mappa.category").isEqualTo("0")
        .jsonPath("$.roshSummary.lastUpdatedDate").isEqualTo("2022-05-19T08:26:31.000Z")
        .jsonPath("$.roshSummary.natureOfRisk").isEqualTo("The nature of the risk is X")
        .jsonPath("$.roshSummary.whoIsAtRisk").isEqualTo("X, Y and Z are at risk")
        .jsonPath("$.roshSummary.riskIncreaseFactors")
        .isEqualTo("If offender in situation X the risk can be higher")
        .jsonPath("$.roshSummary.riskMitigationFactors")
        .isEqualTo("Giving offender therapy in X will reduce the risk")
        .jsonPath("$.roshSummary.riskImminence").isEqualTo("the risk is imminent and more probably in X situation")
        .jsonPath("$.predictorScores.error").isEqualTo(EMPTY_STRING)
        .jsonPath("$.predictorScores.current.date").isEqualTo("2022-04-16")
        .jsonPath("$.predictorScores.current.scores.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.current.scores.RSR.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.current.scores.RSR.score").isEqualTo(23)
        .jsonPath("$.predictorScores.current.scores.OGP.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.scores.OGP.type").isEqualTo("OGP")
        .jsonPath("$.predictorScores.current.scores.OGP.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OGP.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OVP.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.scores.OVP.type").isEqualTo("OVP")
        .jsonPath("$.predictorScores.current.scores.OVP.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OVP.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.current.scores.OSPC.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.scores.OSPC.score").isEqualTo(null)
        .jsonPath("$.predictorScores.current.scores.OSPI.type").isEqualTo("OSP/I")
        .jsonPath("$.predictorScores.current.scores.OSPI.level").isEqualTo("MEDIUM")
        .jsonPath("$.predictorScores.current.scores.OSPI.score").isEqualTo(null)
        .jsonPath("$.predictorScores.current.scores.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.current.scores.OGRS.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.current.scores.OGRS.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.current.scores.OGRS.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.current.date").isEqualTo("2022-04-16")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[0].scores.RSR.score").isEqualTo(23)
        .jsonPath("$.predictorScores.historical[0].scores.OGP.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.historical[0].scores.OGP.type").isEqualTo("OGP")
        .jsonPath("$.predictorScores.historical[0].scores.OGP.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OGP.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OVP.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.historical[0].scores.OVP.type").isEqualTo("OVP")
        .jsonPath("$.predictorScores.historical[0].scores.OVP.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OVP.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.historical[0].scores.OSPC.score").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.type").isEqualTo("OSP/I")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.level").isEqualTo("MEDIUM")
        .jsonPath("$.predictorScores.historical[0].scores.OSPI.score").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[0].scores.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.historical[0].scores.OGRS.level").isEqualTo("LOW")
        .jsonPath("$.predictorScores.historical[0].scores.OGRS.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[0].scores.OGRS.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[1].date").isEqualTo("2021-06-16")
        .jsonPath("$.predictorScores.historical[1].scores.OGP.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[1].scores.OGP.type").isEqualTo("OGP")
        .jsonPath("$.predictorScores.historical[1].scores.OGP.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[1].scores.OGP.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[1].scores.OVP.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[1].scores.OVP.type").isEqualTo("OVP")
        .jsonPath("$.predictorScores.historical[1].scores.OVP.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[1].scores.OVP.twoYears").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[1].scores.RSR.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[1].scores.RSR.score").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[1].scores.RSR.type").isEqualTo("RSR")
        .jsonPath("$.predictorScores.historical[1].scores.OSPC.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[1].scores.OSPC.score").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[1].scores.OSPC.type").isEqualTo("OSP/C")
        .jsonPath("$.predictorScores.historical[1].scores.OSPI.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[1].scores.OSPI.score").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[1].scores.OSPI.type").isEqualTo("OSP/I")
        .jsonPath("$.predictorScores.historical[1].scores.OGRS.level").isEqualTo("HIGH")
        .jsonPath("$.predictorScores.historical[1].scores.OGRS.type").isEqualTo("OGRS")
        .jsonPath("$.predictorScores.historical[1].scores.OGRS.oneYear").isEqualTo(0)
        .jsonPath("$.predictorScores.historical[1].scores.OGRS.twoYears").isEqualTo(0)
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.assessmentStatus").isEqualTo("COMPLETE")
    }
  }

  @Test
  fun `not found when person does not exist`() {
    userAccessAllowed(crn)
    roSHSummaryResponse(crn)
    mappaDetailsResponse(crn)
    allRiskScoresResponse(crn)
    noOffenderDetailsResponse(crn)

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
      userAccessAllowed(crn)
      webTestClient.get()
        .uri("/cases/$crn/risk")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }

  @Test
  fun `given case is excluded then only return user access details`() {
    runTest {
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
  fun `Error message in rosh summary property given on OASYS ARN rosh summary timeout`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      roSHSummaryResponse(crn, delaySeconds = oasysArnClientTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$.roshSummary.error")
        .isEqualTo("TIMEOUT")
    }
  }

  @Test
  fun `Error message in predictor scores property given on OASYS ARN all scores timeout`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      mappaDetailsResponse(crn)
      roSHSummaryResponse(crn)
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
  fun `Error message in mappa property given on Community API mappa timeout`() {
    runTest {
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
        .isOk
        .expectBody()
        .jsonPath("$.mappa.error")
        .isEqualTo("TIMEOUT")
    }
  }
}
