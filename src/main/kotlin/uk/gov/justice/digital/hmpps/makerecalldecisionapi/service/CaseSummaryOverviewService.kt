package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Risk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toOverview

@Service
internal class CaseSummaryOverviewService(
  private val deliusClient: DeliusClient,
  private val riskService: RiskService,
  private val userAccessValidator: UserAccessValidator,
  private val recommendationService: RecommendationService,
) {
  suspend fun getOverview(crn: String): CaseSummaryOverviewResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      CaseSummaryOverviewResponse(userAccessResponse)
    } else {
      val deliusOverview = deliusClient.getOverview(crn)
      val recommendationDetails = recommendationService.getRecommendationsInProgressForCrn(crn)
      val riskManagementPlan = riskService.getLatestRiskManagementPlan(crn)
      val assessmentInfo = riskService.fetchAssessmentInfo(crn, deliusOverview.activeConvictions)

      CaseSummaryOverviewResponse(
        personalDetailsOverview = deliusOverview.personalDetails.toOverview(crn),
        activeConvictions = deliusOverview.activeConvictions,
        lastRelease = deliusOverview.lastRelease,
        risk = Risk(
          flags = deliusOverview.registerFlags,
          riskManagementPlan = riskManagementPlan,
          assessmentInfo = assessmentInfo,
        ),
        activeRecommendation = recommendationDetails,
      )
    }
  }
}
