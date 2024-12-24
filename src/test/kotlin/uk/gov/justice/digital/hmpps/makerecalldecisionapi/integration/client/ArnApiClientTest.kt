package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.risk.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.Assessment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentOffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GroupReconvictionScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OgpScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OgrsScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspcScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspdcScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspiScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspiicScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OtherRisksResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OvpScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskManagementPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskManagementResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryRiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskToSelfResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskVulnerabilityTypeResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RsrScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.SexualPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ViolencePredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

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
      assessments = assessments(),
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
          offenceDate = "2022-04-24T20:39:47",
        ),
        AssessmentOffenceDetail(
          type = "CURRENT",
          offenceCode = "12",
          offenceSubCode = "34",
          offenceDate = "2022-04-24T20:39:47",
        ),
      ),
      assessmentStatus = "COMPLETE",
      superStatus = "COMPLETE",
      dateCompleted = "2022-04-24T15:00:08.000",
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
      contingencyPlans = null,
    ),
    Assessment(
      offenceDetails = listOf(
        AssessmentOffenceDetail(
          type = "NOT_CURRENT",
          offenceCode = "78",
          offenceSubCode = "90",
          offenceDate = "2022-04-24T20:39:47",
        ),
      ),
      assessmentStatus = "COMPLETE",
      superStatus = "COMPLETE",
      initiationDate = "2022-08-12T15:00:08",
      dateCompleted = "2022-04-23T15:00:08.000",
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
      contingencyPlans = null,
    ),
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
        generalPredictorScore = GeneralPredictorScore(
          ogpStaticWeightedScore = "0",
          ogpDynamicWeightedScore = "0",
          ogpTotalWeightedScore = "0",
          ogpRisk = OgpScoreLevel.HIGH.toString(),
          ogp1Year = "0",
          ogp2Year = "0",
        ),
        riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(
          percentageScore = "0",
          scoreLevel = RsrScoreLevel.HIGH.toString(),
        ),
        sexualPredictorScore = SexualPredictorScore(
          ospIndecentPercentageScore = "0",
          ospContactPercentageScore = "0",
          ospIndecentScoreLevel = OspiScoreLevel.HIGH.toString(),
          ospContactScoreLevel = OspcScoreLevel.HIGH.toString(),
          ospIndirectImagePercentageScore = null,
          ospDirectContactPercentageScore = null,
          ospIndirectImageScoreLevel = null,
          ospDirectContactScoreLevel = null,
        ),
        groupReconvictionScore = GroupReconvictionScore(
          oneYear = "0",
          twoYears = "0",
          scoreLevel = OgrsScoreLevel.HIGH.toString(),
        ),
        violencePredictorScore = ViolencePredictorScore(
          ovpStaticWeightedScore = "0",
          ovpDynamicWeightedScore = "0",
          ovpTotalWeightedScore = "0",
          ovpRisk = OvpScoreLevel.HIGH.toString(),
          oneYear = "0",
          twoYears = "0",
        ),
      ),
      RiskScoreResponse(
        completedDate = "2022-04-16T11:40:54.243",
        generalPredictorScore = GeneralPredictorScore(
          ogpStaticWeightedScore = "0",
          ogpDynamicWeightedScore = "0",
          ogpTotalWeightedScore = "12",
          ogpRisk = OgpScoreLevel.LOW.toString(),
          ogp1Year = "0",
          ogp2Year = "0",
        ),
        riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(
          percentageScore = "23",
          scoreLevel = RsrScoreLevel.HIGH.toString(),
        ),
        sexualPredictorScore = SexualPredictorScore(
          ospIndecentPercentageScore = null,
          ospContactPercentageScore = null,
          ospIndecentScoreLevel = null,
          ospContactScoreLevel = null,
          ospIndirectImagePercentageScore = "5",
          ospDirectContactPercentageScore = "3.45",
          ospIndirectImageScoreLevel = OspiicScoreLevel.MEDIUM.toString(),
          ospDirectContactScoreLevel = OspdcScoreLevel.LOW.toString(),
        ),
        groupReconvictionScore = GroupReconvictionScore(oneYear = "0", twoYears = "0", scoreLevel = OgrsScoreLevel.LOW.toString()),
        violencePredictorScore = ViolencePredictorScore(
          ovpStaticWeightedScore = "0",
          ovpDynamicWeightedScore = "0",
          ovpTotalWeightedScore = "0",
          ovpRisk = OvpScoreLevel.LOW.toString(),
          oneYear = "0",
          twoYears = "0",
        ),
      ),
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
      riskInCommunity = RiskScore(
        veryHigh = null,
        high = listOf(
          "Children",
          "Public",
          "Known adult",
        ),
        medium = listOf("Staff"),
        low = null,
      ),
      riskInCustody = RiskScore(
        veryHigh = listOf(
          "Staff",
          "Prisoners",
        ),
        high = listOf("Known adult"),
        medium = null,
        low = listOf(
          "Children",
          "Public",
        ),
      ),
      assessedOn = "2022-05-19T08:26:31",
      overallRiskLevel = "HIGH",
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
          latestCompleteDate = "2022-10-07T14:20:27",
        ),
      ),
    )

    // when
    val actual = arnApiClient.getRiskManagementPlan(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves risks with full text`() {
    // given
    risksWithFullTextResponse(crn)

    // and
    val expected = RiskResponse(
      riskToSelf = RiskToSelfResponse(
        suicide = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of suicide concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of suicide concerns due to ...",
        ),
        selfHarm = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of self harm concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of self harm concerns due to ...",
        ),
        custody = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of custody concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of custody concerns due to ...",
        ),
        hostelSetting = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of hostel setting concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of hostel setting concerns due to ...",
        ),
        vulnerability = RiskVulnerabilityTypeResponse(
          risk = "Yes",
          previous = "Yes",
          previousConcernsText = "Previous risk of vulnerability concerns due to ...",
          current = "Yes",
          currentConcernsText = "Risk of vulnerability concerns due to ...",
        ),
      ),
      otherRisks = OtherRisksResponse(
        escapeOrAbscond = "YES",
        controlIssuesDisruptiveBehaviour = "YES",
        breachOfTrust = "YES",
        riskToOtherPrisoners = "YES",
      ),
      summary = RiskSummaryRiskResponse(
        whoIsAtRisk = "X, Y and Z are at risk",
        natureOfRisk = "The nature of the risk is X",
        riskImminence = "the risk is imminent and more probably in X situation",
        riskIncreaseFactors = "If offender in situation X the risk can be higher",
        riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
        riskInCommunity = RiskScore(
          veryHigh = null,
          high = listOf(
            "Children",
            "Public",
            "Known adult",
          ),
          medium = listOf("Staff"),
          low = listOf("Prisoners"),
        ),
        riskInCustody = RiskScore(
          veryHigh = listOf(
            "Staff",
            "Prisoners",
          ),
          high = listOf("Known adult"),
          medium = null,
          low = listOf(
            "Children",
            "Public",
          ),
        ),
        overallRiskLevel = "HIGH",
      ),
      assessedOn = "2022-11-23T00:01:50",
    )

    // when
    val actual = arnApiClient.getRisksWithFullText(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }
}
