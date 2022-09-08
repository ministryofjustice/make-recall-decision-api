package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse

@Service
internal class LicenceConditionsService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService,
  private val userAccessValidator: UserAccessValidator,
  private val convictionService: ConvictionService,
  private val recommendationService: RecommendationService
) {
  suspend fun getLicenceConditions(crn: String): LicenceConditionsResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      LicenceConditionsResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val convictions = convictionService.buildConvictionResponse(crn, true)
      val releaseSummary = getReleaseSummary(crn)
      val recommendationDetails = recommendationService.getDraftRecommendationForCrn(crn)

      LicenceConditionsResponse(
        personalDetailsOverview = personalDetailsOverview,
        convictions = convictions,
        releaseSummary = releaseSummary,
        activeRecommendation = recommendationDetails,
      )
    }
  }

  private suspend fun getReleaseSummary(crn: String): ReleaseSummaryResponse? {
    return getValueAndHandleWrappedException(communityApiClient.getReleaseSummary(crn))
  }
}
