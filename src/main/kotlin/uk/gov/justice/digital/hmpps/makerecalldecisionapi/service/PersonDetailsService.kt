package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CurrentAddress
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ProbationTeam
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import java.time.LocalDate

@Service
internal class PersonDetailsService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val userAccessValidator: UserAccessValidator,
  @Lazy private val recommendationService: RecommendationService
) {
  fun getPersonDetails(crn: String): PersonDetailsResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      PersonDetailsResponse(userAccessResponse = userAccessResponse)
    } else {
      val offenderDetails = getPersonalDetailsOverview(crn)
      val activeOffenderManager = offenderDetails.offenderManagers?.first { it.active ?: false }
      val activeAddress = offenderDetails.contactDetails?.addresses
        ?.firstOrNull { it.status?.description?.lowercase().equals("main") }
      val addressNumber = activeAddress?.addressNumber ?: ""
      val streetName = activeAddress?.streetName ?: ""
      val buildingName = activeAddress?.buildingName ?: ""
      val trustOfficerForenames = activeOffenderManager?.trustOfficer?.forenames ?: ""
      val trustOfficerSurname = activeOffenderManager?.trustOfficer?.surname ?: ""
      val recommendationDetails = recommendationService.getDraftRecommendationForCrn(crn)

      return PersonDetailsResponse(
        personalDetailsOverview = buildPersonalDetails(crn, offenderDetails),
        currentAddress = CurrentAddress(
          line1 = formatTwoWordField(buildingName, formatTwoWordField(addressNumber, streetName)),
          line2 = activeAddress?.district ?: "",
          town = activeAddress?.town ?: "",
          postcode = activeAddress?.postcode ?: ""
        ),
        offenderManager = OffenderManager(
          name = formatTwoWordField(trustOfficerForenames, trustOfficerSurname),
          phoneNumber = activeOffenderManager?.team?.telephone ?: "",
          email = activeOffenderManager?.team?.emailAddress ?: "",
          probationTeam = ProbationTeam(
            code = activeOffenderManager?.team?.code ?: "",
            label = activeOffenderManager?.team?.description ?: ""
          )
        ),
        activeRecommendation = recommendationDetails
      )
    }
  }

  fun buildPersonalDetailsOverviewResponse(crn: String): PersonDetails {
    val offenderDetails = getPersonalDetailsOverview(crn)
    return buildPersonalDetails(crn, offenderDetails)
  }

  fun buildPersonalDetails(crn: String, offenderDetails: AllOffenderDetailsResponse): PersonDetails {
    val firstName = offenderDetails.firstName ?: ""
    val surname = offenderDetails.surname ?: ""

    return PersonDetails(
      name = formatTwoWordField(firstName, surname),
      firstName = firstName,
      surname = surname,
      dateOfBirth = offenderDetails.dateOfBirth,
      age = age(offenderDetails),
      gender = offenderDetails.gender ?: "",
      crn = crn
    )
  }

  private fun formatTwoWordField(part1: String, part2: String): String {
    val formattedField = if (part1.isEmpty()) {
      part2
    } else "$part1 $part2"
    return formattedField
  }

  private fun getPersonalDetailsOverview(crn: String): AllOffenderDetailsResponse {
    return getValueAndHandleWrappedException(communityApiClient.getAllOffenderDetails(crn))!!
  }

  private fun age(offenderDetails: AllOffenderDetailsResponse) = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years
}
