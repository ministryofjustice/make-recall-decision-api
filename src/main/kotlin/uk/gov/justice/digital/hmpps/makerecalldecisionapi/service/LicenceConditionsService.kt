package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toOverview

@Service
internal class LicenceConditionsService(
  private val deliusClient: DeliusClient,
  private val personDetailsService: PersonDetailsService,
  private val userAccessValidator: UserAccessValidator,
  private val createAndVaryALicenceService: CreateAndVaryALicenceService,
  private val recommendationService: RecommendationService,
  private val licenceConditionsCoordinator: LicenceConditionsCoordinator,
) {
  suspend fun getLicenceConditions(crn: String): LicenceConditionsResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      LicenceConditionsResponse(userAccessResponse = userAccessResponse)
    } else {
      val licenceConditions = deliusClient.getLicenceConditions(crn)
      val recommendationDetails = recommendationService.getRecommendationsInProgressForCrn(crn)

      LicenceConditionsResponse(
        personalDetailsOverview = licenceConditions.personalDetails.toOverview(crn),
        activeConvictions = licenceConditions.activeConvictions,
        activeRecommendation = recommendationDetails,
      )
    }
  }

  suspend fun getLicenceConditionsCvl(crn: String): LicenceConditionsCvlResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      LicenceConditionsCvlResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val licenceConditions = personalDetailsOverview.nomsNumber.let { createAndVaryALicenceService.buildLicenceConditions(crn, it!!) }
      val recommendationDetails = recommendationService.getRecommendationsInProgressForCrn(crn)

      LicenceConditionsCvlResponse(
        personalDetailsOverview = personalDetailsOverview,
        licenceConditions = licenceConditions,
        activeRecommendation = recommendationDetails
      )
    }
  }

  suspend fun getLicenceConditionsV2(crn: String): LicenceConditionsResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      LicenceConditionsResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val cvlLicenceConditions =
        personalDetailsOverview.nomsNumber.let { createAndVaryALicenceService.buildLicenceConditions(crn, it!!) }
      val deliusLicenceConditions = deliusClient.getLicenceConditions(crn)
      val recommendationDetails = recommendationService.getRecommendationsInProgressForCrn(crn)
      val selectedLicenceConditions =
        licenceConditionsCoordinator.selectLicenceConditions(deliusLicenceConditions, cvlLicenceConditions)
      LicenceConditionsResponse(
        hasAllConvictionsReleasedOnLicence = selectedLicenceConditions.hasAllConvictionsReleasedOnLicence,
        personalDetailsOverview = deliusLicenceConditions.personalDetails.toOverview(crn),
        activeConvictions = deliusLicenceConditions.activeConvictions,
        activeRecommendation = recommendationDetails,
        cvlLicence = selectedLicenceConditions.cvlLicenceCondition,
      )
    }
  }
}
