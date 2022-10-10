package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.Assessment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentOffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GroupReconvictionScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCustody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskManagementPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskManagementResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.SexualPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ViolencePredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDateTime

@ActiveProfiles("test")
class ArnApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var arnApiClient: ArnApiClient

  @Test
  fun `retrieves assessments`() {
    // given
    val crn = "X123456"
    oasysAssessmentsResponse(crn, offenceType = "CURRENT")

    // and
    val expected = AssessmentsResponse(
      crn = crn,
      limitedAccessOffender = true,
      assessments = assessments()
    )

    // when
    val actual = arnApiClient.getAssessments(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  private fun assessments() = listOf(
    Assessment(
      offenceDetails = listOf(
        AssessmentOffenceDetail(
          type = "NOT_CURRENT",
          offenceCode = "88",
          offenceSubCode = "88",
          offenceDate = "2022-04-24T20:39:47"
        ),
        AssessmentOffenceDetail(
          type = "CURRENT",
          offenceCode = "12",
          offenceSubCode = "34",
          offenceDate = "2022-04-24T20:39:47"
        )
      ),
      assessmentStatus = "COMPLETE",
      superStatus = "COMPLETE",
      dateCompleted = "2022-04-24T15:00:08",
      initiationDate = "2022-09-12T15:00:08",
      laterWIPAssessmentExists = false,
      laterSignLockAssessmentExists = false,
      laterPartCompUnsignedAssessmentExists = false,
      laterPartCompSignedAssessmentExists = false,
      laterCompleteAssessmentExists = false,
      offence = "Juicy offence details.",
      keyConsiderationsCurrentSituation = null,
      furtherConsiderationsCurrentSituation = null,
      supervision = null,
      monitoringAndControl = null,
      interventionsAndTreatment = null,
      victimSafetyPlanning = null,
      contingencyPlans = null
    ),
    Assessment(
      offenceDetails = listOf(
        AssessmentOffenceDetail(
          type = "NOT_CURRENT",
          offenceCode = "78",
          offenceSubCode = "90",
          offenceDate = "2022-04-24T20:39:47"
        )
      ),
      assessmentStatus = "COMPLETE",
      superStatus = "COMPLETE",
      initiationDate = "2022-08-12T15:00:08",
      dateCompleted = "2022-04-23T15:00:08",
      laterWIPAssessmentExists = false,
      laterSignLockAssessmentExists = false,
      laterPartCompUnsignedAssessmentExists = false,
      laterPartCompSignedAssessmentExists = false,
      laterCompleteAssessmentExists = false,
      offence = "Not so juicy offence details.",
      keyConsiderationsCurrentSituation = null,
      furtherConsiderationsCurrentSituation = null,
      supervision = null,
      monitoringAndControl = null,
      interventionsAndTreatment = null,
      victimSafetyPlanning = null,
      contingencyPlans = null
    )
  )

  @Test
  fun `retrieves scores`() {
    // given
    val crn = "X123456"
    allRiskScoresResponse(crn)

    // and
    val expected = listOf<RiskScoreResponse>(
      RiskScoreResponse(
        completedDate = "2021-06-16T11:40:54.243",
        generalPredictorScore = GeneralPredictorScore(ogpStaticWeightedScore = "0", ogpDynamicWeightedScore = "0", ogpTotalWeightedScore = "0", ogpRisk = "HIGH", ogp1Year = "0", ogp2Year = "0"),
        riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "0", scoreLevel = "HIGH"),
        sexualPredictorScore = SexualPredictorScore(ospIndecentPercentageScore = "0", ospContactPercentageScore = "0", ospIndecentScoreLevel = "HIGH", ospContactScoreLevel = "HIGH"),
        groupReconvictionScore = GroupReconvictionScore(oneYear = "0", twoYears = "0", scoreLevel = "HIGH"),
        violencePredictorScore = ViolencePredictorScore(ovpStaticWeightedScore = "0", ovpDynamicWeightedScore = "0", ovpTotalWeightedScore = "0", ovpRisk = "HIGH", oneYear = "0", twoYears = "0")
      ),
      RiskScoreResponse(
        completedDate = "2022-04-16T11:40:54.243",
        generalPredictorScore = GeneralPredictorScore(ogpStaticWeightedScore = "0", ogpDynamicWeightedScore = "0", ogpTotalWeightedScore = "12", ogpRisk = "LOW", ogp1Year = "0", ogp2Year = "0"),
        riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "23", scoreLevel = "HIGH"),
        sexualPredictorScore = SexualPredictorScore(ospIndecentPercentageScore = "5", ospContactPercentageScore = "3.45", ospIndecentScoreLevel = "MEDIUM", ospContactScoreLevel = "LOW"),
        groupReconvictionScore = GroupReconvictionScore(oneYear = "0", twoYears = "0", scoreLevel = "LOW"),
        violencePredictorScore = ViolencePredictorScore(ovpStaticWeightedScore = "0", ovpDynamicWeightedScore = "0", ovpTotalWeightedScore = "0", ovpRisk = "LOW", oneYear = "0", twoYears = "0")
      )
    )

    // when
    val actual = arnApiClient.getRiskScores(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves risk summary`() {
    // given
    val crn = "X123456"
    roSHSummaryResponse(crn)

    // and
    val expected = RiskSummaryResponse(
      whoIsAtRisk = "X, Y and Z are at risk",
      natureOfRisk = "The nature of the risk is X",
      riskImminence = "the risk is imminent and more probably in X situation",
      riskIncreaseFactors = "If offender in situation X the risk can be higher",
      riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
      riskInCommunity = RiskInCommunity(
        veryHigh = null,
        high = listOf(
          "Children",
          "Public",
          "Known adult"
        ),
        medium = listOf("Staff"),
        low = listOf("Prisoners")
      ),
      riskInCustody = RiskInCustody(
        veryHigh = listOf(
          "Staff",
          "Prisoners"
        ),
        high = listOf("Known adult"),
        medium = null,
        low = listOf(
          "Children",
          "Public"
        )
      ),
      assessedOn = LocalDateTime.parse("2022-05-19T08:26:31.349"),
      overallRiskLevel = "HIGH"
    )

    // when
    val actual = arnApiClient.getRiskSummary(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves risk management plan`() {
    // given
    val crn = "X123456"
    riskManagementPlanResponse(crn)

    // and
    val expected = RiskManagementResponse(
      crn = crn,
      limitedAccessOffender = true,
      riskManagementPlan = listOf(
        RiskManagementPlanResponse(
          assessmentId = 0,
          dateCompleted = "2022-10-01T14:20:27",
          partcompStatus = "Part comp status",
          initiationDate = "2022-10-02T14:20:27",
          assessmentStatus = "COMPLETE",
          assessmentType = "LAYER1",
          superStatus = "COMPLETE",
          keyInformationCurrentSituation = "patternOfOffending",
          furtherConsiderationsCurrentSituation = "string",
          supervision = "string",
          monitoringAndControl = "string",
          interventionsAndTreatment = "string",
          victimSafetyPlanning = "string",
          contingencyPlans = "I am the contingency plan text",
          laterWIPAssessmentExists = true,
          latestWIPDate = "2022-10-03T14:20:27",
          laterSignLockAssessmentExists = true,
          latestSignLockDate = "2022-10-04T14:20:27",
          laterPartCompUnsignedAssessmentExists = true,
          latestPartCompUnsignedDate = "2022-10-05T14:20:27",
          laterPartCompSignedAssessmentExists = true,
          latestPartCompSignedDate = "2022-10-06T14:20:27",
          laterCompleteAssessmentExists = true,
          latestCompleteDate = "2022-10-07T14:20:27"
        )
      )
    )

    // when
    val actual = arnApiClient.getRiskManagementPlan(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }
}
