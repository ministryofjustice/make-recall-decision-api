package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.toAddresses
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.joinToString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ProbationTeam
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toOverview

@Service
internal class PersonDetailsService(
  private val deliusClient: DeliusClient,
  private val userAccessValidator: UserAccessValidator,
  @Lazy private val recommendationService: RecommendationService?
) {
  fun getPersonDetails(crn: String): PersonDetailsResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      PersonDetailsResponse(userAccessResponse = userAccessResponse)
    } else {
      val deliusDetails = deliusClient.getPersonalDetails(crn)
      val manager = deliusDetails.communityManager
      val recommendationDetails = recommendationService?.getRecommendationsInProgressForCrn(crn)

      return PersonDetailsResponse(
        personalDetailsOverview = deliusDetails.personalDetails.toOverview(crn),
        addresses = deliusDetails.mainAddress.toAddresses(),
        offenderManager = OffenderManager(
          name = joinToString(manager?.name?.forename, manager?.name?.middleName, manager?.name?.surname),
          phoneNumber = manager?.team?.telephone ?: "",
          email = manager?.team?.email ?: "",
          probationAreaDescription = manager?.provider?.name,
          probationTeam = ProbationTeam(
            code = manager?.team?.code ?: "",
            label = manager?.team?.name ?: "",
            localDeliveryUnitDescription = manager?.team?.localAdminUnit
          )
        ),
        activeRecommendation = recommendationDetails
      )
    }
  }

  fun buildPersonalDetailsOverviewResponse(crn: String) = deliusClient.getPersonalDetails(crn).personalDetails.toOverview(crn)
}
