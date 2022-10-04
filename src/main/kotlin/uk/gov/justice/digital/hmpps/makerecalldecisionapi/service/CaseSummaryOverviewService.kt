package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Risk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse

@Service
internal class CaseSummaryOverviewService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val riskService: RiskService,
  private val personDetailsService: PersonDetailsService,
  private val userAccessValidator: UserAccessValidator,
  private val convictionService: ConvictionService,
  private val recommendationService: RecommendationService
) {
  suspend fun getOverview(crn: String): CaseSummaryOverviewResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      CaseSummaryOverviewResponse(userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)

      val registrations = getValueAndHandleWrappedException(communityApiClient.getRegistrations(crn))?.registrations
      val activeRegistrations = registrations?.filter { it.active ?: false }
      val riskFlags = activeRegistrations?.map { it.type?.description ?: "" } ?: emptyList()

      val recommendationDetails = recommendationService.getDraftRecommendationForCrn(crn)

      val riskManagementPlan = riskService.getLatestRiskManagementPlan(crn)
      val assessmentInfo = riskService.fetchAsessmentInfo(crn)

      val releaseSummary = getReleaseSummary(crn)

      CaseSummaryOverviewResponse(
        personalDetailsOverview = personalDetailsOverview,
        convictions = convictionService.buildConvictionResponseForOverview(crn),
        releaseSummary = releaseSummary,
        risk = Risk(flags = riskFlags, riskManagementPlan = riskManagementPlan),
        activeRecommendation = recommendationDetails,
        assessmentInfo = assessmentInfo
      )
    }
  }

  private suspend fun getReleaseSummary(crn: String): ReleaseSummaryResponse? {
    return getValueAndHandleWrappedException(communityApiClient.getReleaseSummary(crn))
  }
}
