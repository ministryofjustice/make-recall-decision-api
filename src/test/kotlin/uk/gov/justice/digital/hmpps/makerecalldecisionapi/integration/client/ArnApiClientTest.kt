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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ContingencyPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.CurrentScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.HistoricalScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCustody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.SexualPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDateTime

@ActiveProfiles("test")
class ArnApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var arnApiClient: ArnApiClient

  @Test
  fun `retrieves historical scores`() {
    // given
    val crn = "X123456"
    historicalRiskScoresResponse(crn)

    // and
    val expected = listOf(
      HistoricalScoreResponse(
        rsrPercentageScore = "18",
        rsrScoreLevel = "HIGH",
        ospcPercentageScore = "6.2",
        ospcScoreLevel = "LOW",
        ospiPercentageScore = "8.1",
        ospiScoreLevel = "MEDIUM",
        calculatedDate = "2018-09-12T12:00:00.000"
      )
    )

    // when
    val actual = arnApiClient.getHistoricalScores(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves contingency plan`() {
    // given
    val crn = "X123456"
    contingencyPlanSimpleResponse(crn)

    // and
    val expected = ContingencyPlanResponse(
      assessments = listOf(
        Assessment(
          dateCompleted = "2020-07-03T11:42:01",
          assessmentStatus = "COMPLETE",
          keyConsiderationsCurrentSituation = "key considerations for current situation",
          furtherConsiderationsCurrentSituation = "further considerations for current situation",
          supervision = null,
          monitoringAndControl = "monitoring and control",
          interventionsAndTreatment = "interventions and treatment",
          victimSafetyPlanning = "victim safety planning",
          contingencyPlans = null,
          offenceDetails = emptyList(),
          offence = null,
          laterCompleteAssessmentExists = null,
          laterPartCompSignedAssessmentExists = null,
          laterPartCompUnsignedAssessmentExists = null,
          laterSignLockAssessmentExists = null,
          laterWIPAssessmentExists = null,
          superStatus = null
        )
      )
    )

    // when
    val actual = arnApiClient.getContingencyPlan(crn).block()

    // then
    assertThat(actual, equalTo(expected))
  }

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
  fun `retrieves current scores`() {
    // given
    val crn = "X123456"
    currentRiskScoresResponse(crn)

    // and
    val expected = listOf<CurrentScoreResponse>(
      CurrentScoreResponse(
        completedDate = "2022-04-16T11:40:54.243",
        generalPredictorScore = GeneralPredictorScore(ogpStaticWeightedScore = "0", ogpDynamicWeightedScore = "0", ogpTotalWeightedScore = "0", ogpRisk = "HIGH"),
        riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "0", scoreLevel = "HIGH"),
        sexualPredictorScore = SexualPredictorScore(ospIndecentPercentageScore = "0", ospContactPercentageScore = "0", ospIndecentScoreLevel = "HIGH", ospContactScoreLevel = "HIGH")
      ),
      CurrentScoreResponse(
        completedDate = "2022-06-16T11:40:54.243",
        generalPredictorScore = GeneralPredictorScore(ogpStaticWeightedScore = "0", ogpDynamicWeightedScore = "0", ogpTotalWeightedScore = "12", ogpRisk = "LOW"),
        riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "23", scoreLevel = "HIGH"),
        sexualPredictorScore = SexualPredictorScore(ospIndecentPercentageScore = "5", ospContactPercentageScore = "3.45", ospIndecentScoreLevel = "MEDIUM", ospContactScoreLevel = "LOW")
      )
    )

    // when
    val actual = arnApiClient.getCurrentScores(crn).block()

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
}
