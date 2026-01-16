package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.FourBandRiskScoreBand
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.FourLevelRiskScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OGP
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OGRS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPDC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPI
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPIIC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OVP
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.RSR
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.StaticOrDynamic
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ThreeBandRiskScoreBand
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ThreeLevelRiskScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RiskControllerTest(
  @Value("\${mrd.url}") private val mrdUrl: String?,
  @Value("\${mrd.api.url}") private val mrdApiUrl: String?,
  @Value("\${oasys.arn.client.timeout}") private val oasysArnClientTimeout: Long,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
) : IntegrationTestBase() {
  @Test
  fun `retrieves risk summary when no MAPPA or risk scores and RoSH summary available`() {
    runTest {
      userAccessAllowed(crn)
      roshHistoryOnlyResponse(crn)
      noRiskScoresResponse(crn)
      noOffenderFoundRoshSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("Joe Bloggs")
        .jsonPath("$.personalDetailsOverview.dateOfBirth")
        .isEqualTo(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .jsonPath("$.personalDetailsOverview.age").isEqualTo(Period.between(dateOfBirth, LocalDate.now()).years)
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.roshSummary.riskOfSeriousHarm").isEqualTo(null)
        .jsonPath("$.mappa").isEqualTo(null)
        .jsonPath("$.roshSummary.natureOfRisk").isEqualTo(null)
        .jsonPath("$.roshSummary.whoIsAtRisk").isEqualTo(null)
        .jsonPath("$.roshSummary.riskIncreaseFactors").isEqualTo(null)
        .jsonPath("$.roshSummary.riskMitigationFactors").isEqualTo(null)
        .jsonPath("$.roshSummary.riskImminence").isEqualTo(null)
        .jsonPath("$.roshSummary.error").isEqualTo("NOT_FOUND")
        .jsonPath("$.predictorScores.error").isEqualTo("NOT_FOUND")
        .jsonPath("$.roshHistory.error").isEqualTo(null)
        .jsonPath("$.roshHistory.registrations[0].type.code").isEqualTo("RVHR")
        .jsonPath("$.roshHistory.registrations[0].type.description").isEqualTo("Very High RoSH")
        .jsonPath("$.roshHistory.registrations[0].startDate").isEqualTo("2021-01-30")
        .jsonPath("$.roshHistory.registrations[0].notes").isEqualTo("Notes on Very High RoSH case")
    }
  }

  @Test
  fun `retrieves risk summary when no MAPPA or risk scores are available and RoSH summary is no latest complete 404`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      noMappaOrRoshHistoryResponse(crn)
      noRiskScoresResponse(crn)
      noLatestCompleteRoshSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("Joe Bloggs")
        .jsonPath("$.personalDetailsOverview.dateOfBirth")
        .isEqualTo(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .jsonPath("$.personalDetailsOverview.age").isEqualTo(Period.between(dateOfBirth, LocalDate.now()).years)
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.roshSummary.riskOfSeriousHarm").isEqualTo(null)
        .jsonPath("$.mappa").isEqualTo(null)
        .jsonPath("$.roshSummary.natureOfRisk").isEqualTo(null)
        .jsonPath("$.roshSummary.whoIsAtRisk").isEqualTo(null)
        .jsonPath("$.roshSummary.riskIncreaseFactors").isEqualTo(null)
        .jsonPath("$.roshSummary.riskMitigationFactors").isEqualTo(null)
        .jsonPath("$.roshSummary.riskImminence").isEqualTo(null)
        .jsonPath("$.roshSummary.error").isEqualTo("NOT_FOUND_LATEST_COMPLETE")
        .jsonPath("$.predictorScores.error").isEqualTo("NOT_FOUND")
    }
  }

  @Test
  fun `retrieves risk summary when no MAPPA available, ARN RoSH Summary fetch fails, and Risk Scores fetch fails`() {
    runTest {
      userAccessAllowed(crn)
      personalDetailsResponse(crn)
      noMappaOrRoshHistoryResponse(crn)
      failedRiskScoresResponse(crn)
      failedRoSHSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("Joe Bloggs")
        .jsonPath("$.personalDetailsOverview.dateOfBirth")
        .isEqualTo(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .jsonPath("$.personalDetailsOverview.age").isEqualTo(Period.between(dateOfBirth, LocalDate.now()).years)
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.roshSummary.riskOfSeriousHarm").isEqualTo(null)
        .jsonPath("$.mappa").isEqualTo(null)
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
      personalDetailsResponse(crn)
      noMappaOrRoshHistoryResponse(crn)
      allRiskScoresEmptyResponse(crn)
      roSHSummaryNoDataResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("Joe Bloggs")
        .jsonPath("$.personalDetailsOverview.dateOfBirth")
        .isEqualTo(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .jsonPath("$.personalDetailsOverview.age").isEqualTo(Period.between(dateOfBirth, LocalDate.now()).years)
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.roshSummary.error").isEqualTo("MISSING_DATA")
    }
  }

  @Test
  fun `retrieves risk data when ARN Scores are null`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      oasysAssessmentsResponse(crn)
      roSHSummaryResponse(crn)
      personalDetailsResponse(crn)
      mappaAndRoshHistoryResponse(crn)
      allRiskScoresEmptyResponse(crn)
      deleteAndCreateRecommendation(featureFlagString)
      updateRecommendation(Status.DRAFT)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("Joe Bloggs")
        .jsonPath("$.personalDetailsOverview.dateOfBirth")
        .isEqualTo(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .jsonPath("$.personalDetailsOverview.age").isEqualTo(Period.between(dateOfBirth, LocalDate.now()).years)
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
        .jsonPath("$.activeRecommendation.recallConsideredList.length()").isEqualTo(1)
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userName").isEqualTo("some_user")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].createdDate").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].id").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userId").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].recallConsideredDetail")
        .isEqualTo("This is an updated recall considered detail")
        .jsonPath("$.activeRecommendation.status").isEqualTo("DRAFT")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.details")
        .isEqualTo("details of no recall selected")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].text").isEqualTo("Do not recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].value").isEqualTo("RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].text").isEqualTo("Recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.isSentToDelius").isEqualTo(false)
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdBy").isEqualTo("Joe Bloggs")
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdDate").isEqualTo("2023-01-01T15:00:08.000Z")
        .jsonPath("$.assessmentStatus").isEqualTo("COMPLETE")
    }
  }

  @Test
  fun `retrieves risk data`() {
    runTest {
      val featureFlagString = "{\"flagConsiderRecall\": true }"

      userAccessAllowed(crn)
      oasysAssessmentsResponse(crn)
      roSHSummaryResponse(crn)
      personalDetailsResponse(crn)
      mappaAndRoshHistoryResponse(crn)
      allRiskScoresResponse(crn)
      deleteAndCreateRecommendation(featureFlagString)
      updateRecommendation(Status.DRAFT)

      webTestClient.get()
        .uri("/cases/$crn/risk")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("Joe Bloggs")
        .jsonPath("$.personalDetailsOverview.dateOfBirth")
        .isEqualTo(dateOfBirth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        .jsonPath("$.personalDetailsOverview.age").isEqualTo(Period.between(dateOfBirth, LocalDate.now()).years)
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
        // predictorScores CURRENT: always V2 (2023-06-16)
        .jsonPath("$.predictorScores.current.date").isEqualTo("2023-06-16")
        // V1 fields must NOT exist
        .jsonPath("$.predictorScores.current.scores.RSR").doesNotExist()
        .jsonPath("$.predictorScores.current.scores.OGP").doesNotExist()
        .jsonPath("$.predictorScores.current.scores.OVP").doesNotExist()
        .jsonPath("$.predictorScores.current.scores.OSPDC").doesNotExist()
        .jsonPath("$.predictorScores.current.scores.OSPIIC").doesNotExist()
        .jsonPath("$.predictorScores.current.scores.OSPC").doesNotExist()
        .jsonPath("$.predictorScores.current.scores.OSPI").doesNotExist()
        .jsonPath("$.predictorScores.current.scores.OGRS").doesNotExist()
        // V2 fields
        .jsonPath("$.predictorScores.current.scores.allReoffendingPredictor.score").isEqualTo(12.5)
        .jsonPath("$.predictorScores.current.scores.allReoffendingPredictor.band").isEqualTo(FourBandRiskScoreBand.MEDIUM)
        .jsonPath("$.predictorScores.current.scores.allReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.STATIC)
        .jsonPath("$.predictorScores.current.scores.violentReoffendingPredictor.score").isEqualTo(8.0)
        .jsonPath("$.predictorScores.current.scores.violentReoffendingPredictor.band").isEqualTo(FourBandRiskScoreBand.LOW)
        .jsonPath("$.predictorScores.current.scores.violentReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.DYNAMIC)
        .jsonPath("$.predictorScores.current.scores.seriousViolentReoffendingPredictor.score").isEqualTo(15.2)
        .jsonPath("$.predictorScores.current.scores.seriousViolentReoffendingPredictor.band").isEqualTo(FourBandRiskScoreBand.HIGH)
        .jsonPath("$.predictorScores.current.scores.seriousViolentReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.STATIC)
        .jsonPath("$.predictorScores.current.scores.directContactSexualReoffendingPredictor.score").isEqualTo(6.3)
        .jsonPath("$.predictorScores.current.scores.directContactSexualReoffendingPredictor.band").isEqualTo(FourBandRiskScoreBand.LOW)
        .jsonPath("$.predictorScores.current.scores.directContactSexualReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.STATIC)
        .jsonPath("$.predictorScores.current.scores.indirectImageContactSexualReoffendingPredictor.score").isEqualTo(9.8)
        .jsonPath("$.predictorScores.current.scores.indirectImageContactSexualReoffendingPredictor.band").isEqualTo(
          ThreeBandRiskScoreBand.MEDIUM,
        )
        .jsonPath("$.predictorScores.current.scores.indirectImageContactSexualReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.STATIC)
        .jsonPath("$.predictorScores.current.scores.combinedSeriousReoffendingPredictor.score").isEqualTo(18.7)
        .jsonPath("$.predictorScores.current.scores.combinedSeriousReoffendingPredictor.band").isEqualTo(FourBandRiskScoreBand.VERY_HIGH)
        .jsonPath("$.predictorScores.current.scores.combinedSeriousReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.DYNAMIC)
        .jsonPath("$.predictorScores.current.scores.combinedSeriousReoffendingPredictor.algorithmVersion").isEqualTo("v2.1.0")
        // Historic[0] should be same as current (2023-06-16)
        .jsonPath("$.predictorScores.historical[0].date").isEqualTo("2023-06-16")
        // V1 fields must NOT exist
        .jsonPath("$.predictorScores.historical[0].scores.RSR").doesNotExist()
        .jsonPath("$.predictorScores.historical[0].scores.OGP").doesNotExist()
        .jsonPath("$.predictorScores.historical[0].scores.OVP").doesNotExist()
        .jsonPath("$.predictorScores.historical[0].scores.OSPDC").doesNotExist()
        .jsonPath("$.predictorScores.historical[0].scores.OSPIIC").doesNotExist()
        .jsonPath("$.predictorScores.historical[0].scores.OSPC").doesNotExist()
        .jsonPath("$.predictorScores.historical[0].scores.OSPI").doesNotExist()
        .jsonPath("$.predictorScores.historical[0].scores.OGRS").doesNotExist()
        // V2 fields
        .jsonPath("$.predictorScores.historical[0].scores.allReoffendingPredictor.score").isEqualTo(12.5)
        .jsonPath("$.predictorScores.historical[0].scores.allReoffendingPredictor.band").isEqualTo(FourBandRiskScoreBand.MEDIUM)
        .jsonPath("$.predictorScores.historical[0].scores.allReoffendingPredictor.staticOrDynamic").isEqualTo(
          StaticOrDynamic.STATIC,
        )
        .jsonPath("$.predictorScores.historical[0].scores.violentReoffendingPredictor.score").isEqualTo(8.0)
        .jsonPath("$.predictorScores.historical[0].scores.violentReoffendingPredictor.band").isEqualTo(FourBandRiskScoreBand.LOW)
        .jsonPath("$.predictorScores.historical[0].scores.violentReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.DYNAMIC)
        .jsonPath("$.predictorScores.historical[0].scores.seriousViolentReoffendingPredictor.score").isEqualTo(15.2)
        .jsonPath("$.predictorScores.historical[0].scores.seriousViolentReoffendingPredictor.band").isEqualTo(FourBandRiskScoreBand.HIGH)
        .jsonPath("$.predictorScores.historical[0].scores.seriousViolentReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.STATIC)
        .jsonPath("$.predictorScores.historical[0].scores.directContactSexualReoffendingPredictor.score").isEqualTo(6.3)
        .jsonPath("$.predictorScores.historical[0].scores.directContactSexualReoffendingPredictor.band").isEqualTo(FourBandRiskScoreBand.LOW)
        .jsonPath("$.predictorScores.historical[0].scores.directContactSexualReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.STATIC)
        .jsonPath("$.predictorScores.historical[0].scores.indirectImageContactSexualReoffendingPredictor.score").isEqualTo(9.8)
        .jsonPath("$.predictorScores.historical[0].scores.indirectImageContactSexualReoffendingPredictor.band").isEqualTo(
          ThreeBandRiskScoreBand.MEDIUM,
        )
        .jsonPath("$.predictorScores.historical[0].scores.indirectImageContactSexualReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.STATIC)
        .jsonPath("$.predictorScores.historical[0].scores.combinedSeriousReoffendingPredictor.score").isEqualTo(18.7)
        .jsonPath("$.predictorScores.historical[0].scores.combinedSeriousReoffendingPredictor.band").isEqualTo(
          FourBandRiskScoreBand.VERY_HIGH,
        )
        .jsonPath("$.predictorScores.historical[0].scores.combinedSeriousReoffendingPredictor.staticOrDynamic").isEqualTo(StaticOrDynamic.DYNAMIC)
        .jsonPath("$.predictorScores.historical[0].scores.combinedSeriousReoffendingPredictor.algorithmVersion").isEqualTo("v2.1.0")
        // Historic[1] should be V1 with 2022-06-16
        .jsonPath("$.predictorScores.historical[1].date").isEqualTo("2022-06-16")
        .jsonPath("$.predictorScores.historical[1].scores.RSR.type").isEqualTo(RSR.printName)
        .jsonPath("$.predictorScores.historical[1].scores.RSR.level").isEqualTo(ThreeLevelRiskScoreLevel.HIGH)
        .jsonPath("$.predictorScores.historical[1].scores.RSR.score").isEqualTo(23.0)
        .jsonPath("$.predictorScores.historical[1].scores.OGP.type").isEqualTo(OGP.printName)
        .jsonPath("$.predictorScores.historical[1].scores.OGP.level").isEqualTo(FourLevelRiskScoreLevel.LOW)
        .jsonPath("$.predictorScores.historical[1].scores.OGP.oneYear").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[1].scores.OGP.twoYears").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[1].scores.OVP.type").isEqualTo(OVP.printName)
        .jsonPath("$.predictorScores.historical[1].scores.OVP.level").isEqualTo(FourLevelRiskScoreLevel.LOW)
        .jsonPath("$.predictorScores.historical[1].scores.OVP.oneYear").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[1].scores.OVP.twoYears").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[1].scores.OSPDC.type").isEqualTo(OSPDC.printName)
        .jsonPath("$.predictorScores.historical[1].scores.OSPDC.level").isEqualTo(FourLevelRiskScoreLevel.LOW)
        .jsonPath("$.predictorScores.historical[1].scores.OSPIIC.type").isEqualTo(OSPIIC.printName)
        .jsonPath("$.predictorScores.historical[1].scores.OSPIIC.level").isEqualTo(ThreeLevelRiskScoreLevel.MEDIUM)
        .jsonPath("$.predictorScores.historical[1].scores.OGRS.type").isEqualTo(OGRS.printName)
        .jsonPath("$.predictorScores.historical[1].scores.OGRS.level").isEqualTo(FourLevelRiskScoreLevel.LOW)
        .jsonPath("$.predictorScores.historical[1].scores.OGRS.oneYear").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[1].scores.OGRS.twoYears").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[1].scores.OSPC").doesNotExist()
        .jsonPath("$.predictorScores.historical[1].scores.OSPI").doesNotExist()
        // V2 fields must NOT exist
        .jsonPath("$.predictorScores.historical[1].scores.allReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[1].scores.violentReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[1].scores.seriousViolentReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[1].scores.directContactSexualReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[1].scores.indirectImageContactSexualReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[1].scores.combinedSeriousReoffendingPredictor").doesNotExist()
        // Historic[2] should ve V1 with 2021-06-16
        .jsonPath("$.predictorScores.historical[2].date").isEqualTo("2021-06-16")
        .jsonPath("$.predictorScores.historical[2].scores.RSR.type").isEqualTo(RSR.printName)
        .jsonPath("$.predictorScores.historical[2].scores.RSR.level").isEqualTo(ThreeLevelRiskScoreLevel.HIGH)
        .jsonPath("$.predictorScores.historical[2].scores.RSR.score").isEqualTo(23.0)
        .jsonPath("$.predictorScores.historical[2].scores.OGP.type").isEqualTo(OGP.printName)
        .jsonPath("$.predictorScores.historical[2].scores.OGP.level").isEqualTo(FourLevelRiskScoreLevel.LOW)
        .jsonPath("$.predictorScores.historical[2].scores.OGP.oneYear").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[2].scores.OGP.twoYears").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[2].scores.OVP.type").isEqualTo(OVP.printName)
        .jsonPath("$.predictorScores.historical[2].scores.OVP.level").isEqualTo(FourLevelRiskScoreLevel.LOW)
        .jsonPath("$.predictorScores.historical[2].scores.OVP.oneYear").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[2].scores.OVP.twoYears").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[2].scores.OSPI.type").isEqualTo(OSPI.printName)
        .jsonPath("$.predictorScores.historical[2].scores.OSPI.level").isEqualTo(ThreeLevelRiskScoreLevel.HIGH)
        .jsonPath("$.predictorScores.historical[2].scores.OSPI.score").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[2].scores.OSPC.type").isEqualTo(OSPC.printName)
        .jsonPath("$.predictorScores.historical[2].scores.OSPC.level").isEqualTo(ThreeLevelRiskScoreLevel.HIGH)
        .jsonPath("$.predictorScores.historical[2].scores.OSPC.score").isEqualTo(null)
        .jsonPath("$.predictorScores.historical[2].scores.OGRS.type").isEqualTo(OGRS.printName)
        .jsonPath("$.predictorScores.historical[2].scores.OGRS.level").isEqualTo(FourLevelRiskScoreLevel.LOW)
        .jsonPath("$.predictorScores.historical[2].scores.OGRS.oneYear").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[2].scores.OGRS.twoYears").isEqualTo(0.0)
        .jsonPath("$.predictorScores.historical[2].scores.OSPDC").doesNotExist()
        .jsonPath("$.predictorScores.historical[2].scores.OSPIIC").doesNotExist()
        // V2 fields must NOT exist
        .jsonPath("$.predictorScores.historical[2].scores.allReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[2].scores.violentReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[2].scores.seriousViolentReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[2].scores.directContactSexualReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[2].scores.indirectImageContactSexualReoffendingPredictor").doesNotExist()
        .jsonPath("$.predictorScores.historical[2].scores.combinedSeriousReoffendingPredictor").doesNotExist()
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallType.selected.value").isEqualTo("FIXED_TERM")
        .jsonPath("$.activeRecommendation.recallConsideredList.length()").isEqualTo(1)
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userName").isEqualTo("some_user")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].createdDate").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].id").isNotEmpty
        .jsonPath("$.activeRecommendation.recallConsideredList[0].userId").isEqualTo("SOME_USER")
        .jsonPath("$.activeRecommendation.recallConsideredList[0].recallConsideredDetail")
        .isEqualTo("This is an updated recall considered detail")
        .jsonPath("$.activeRecommendation.status").isEqualTo("DRAFT")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.selected.details")
        .isEqualTo("details of no recall selected")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].value").isEqualTo("NO_RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[1].text").isEqualTo("Do not recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].value").isEqualTo("RECALL")
        .jsonPath("$.activeRecommendation.managerRecallDecision.allOptions[0].text").isEqualTo("Recall")
        .jsonPath("$.activeRecommendation.managerRecallDecision.isSentToDelius").isEqualTo(false)
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdBy").isEqualTo("Joe Bloggs")
        .jsonPath("$.activeRecommendation.managerRecallDecision.createdDate").isEqualTo("2023-01-01T15:00:08.000Z")
        .jsonPath("$.assessmentStatus").isEqualTo("COMPLETE")
    }
  }

  @Test
  fun `not found when person does not exist`() {
    userAccessAllowed(crn)
    roSHSummaryResponse(crn)
    mappaAndRoshHistoryNotFound(crn)
    allRiskScoresResponse(crn)

    webTestClient.get()
      .uri("/cases/$crn/risk")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.developerMessage")
      .isEqualTo("No details available for endpoint: /case-summary/A12345/mappa-and-rosh-history")
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
        .isEqualTo("You are restricted from viewing this offender record. Please contact OM Joe Bloggs")
        .jsonPath("$.userAccessResponse.exclusionMessage").isEmpty
        .jsonPath("$.personalDetailsOverview").isEmpty
        .jsonPath("$.mappa").isEqualTo(null)
    }
  }

  @Test
  fun `Error message in rosh summary property given on OASYS ARN rosh summary timeout`() {
    runTest {
      userAccessAllowed(crn)
      mappaAndRoshHistoryResponse(crn)
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
      mappaAndRoshHistoryResponse(crn)
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
}
