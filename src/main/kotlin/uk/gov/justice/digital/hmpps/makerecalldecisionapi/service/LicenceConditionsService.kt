package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse

@Service
internal class LicenceConditionsService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService,
  private val userAccessValidator: UserAccessValidator,
  private val convictionService: ConvictionService,
  private val createAndVaryALicenceService: CreateAndVaryALicenceService,
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

  suspend fun getLicenceConditionsCvl(crn: String): LicenceConditionsCvlResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      LicenceConditionsCvlResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val licenceConditions = personalDetailsOverview.nomsNumber.let { createAndVaryALicenceService.buildLicenceConditions(crn, it!!) }
      val recommendationDetails = recommendationService.getDraftRecommendationForCrn(crn)

      LicenceConditionsCvlResponse(
        personalDetailsOverview = personalDetailsOverview,
        licenceConditions = licenceConditions,
        activeRecommendation = recommendationDetails
      )
    }
  }

  private suspend fun getReleaseSummary(crn: String): ReleaseSummaryResponse? {
    return getValueAndHandleWrappedException(communityApiClient.getReleaseSummary(crn))
  }
}
