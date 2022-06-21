package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirst
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
class PersonDetailsService(
  private val communityApiClient: CommunityApiClient
) {
  suspend fun getPersonDetails(crn: String): PersonDetailsResponse {
    val userAccessResponse = communityApiClient.getUserAccess(crn).awaitFirst()
    return if (true == userAccessResponse?.userExcluded || true == userAccessResponse?.userRestricted) {
      PersonDetailsResponse(userAccessResponse = userAccessResponse)
    } else {
      val offenderDetails = getPersonalDetailsOverview(crn)
      val activeOffenderManager = offenderDetails.offenderManagers?.first { it.active ?: false }
      val activeAddress = offenderDetails.contactDetails?.addresses
        ?.firstOrNull { it.status?.description?.lowercase().equals("main") }
      val addressNumber = activeAddress?.addressNumber ?: ""
      val streetName = activeAddress?.streetName ?: ""
      val buildingName = activeAddress?.buildingName ?: ""
      val firstName = offenderDetails.firstName ?: ""
      val surname = offenderDetails.surname ?: ""
      val trustOfficerForenames = activeOffenderManager?.trustOfficer?.forenames ?: ""
      val trustOfficerSurname = activeOffenderManager?.trustOfficer?.surname ?: ""

      return PersonDetailsResponse(
        personalDetailsOverview = PersonDetails(
          name = formatTwoWordField(firstName, surname),
          dateOfBirth = offenderDetails.dateOfBirth,
          age = age(offenderDetails),
          gender = offenderDetails.gender ?: "",
          crn = crn
        ),
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
        )
      )
    }
  }

  suspend fun buildPersonalDetailsOverviewResponse(crn: String): PersonDetails {
    val offenderDetails = getPersonalDetailsOverview(crn)
    return PersonDetails(
      name = "${offenderDetails.firstName} ${offenderDetails.surname}",
      dateOfBirth = offenderDetails.dateOfBirth,
      age = age(offenderDetails),
      gender = offenderDetails.gender,
      crn = crn
    )
  }

  private fun formatTwoWordField(part1: String, part2: String): String {
    val formattedField = if (part1.isEmpty()) {
      part2
    } else "$part1 $part2"
    return formattedField
  }

  private suspend fun getPersonalDetailsOverview(crn: String): AllOffenderDetailsResponse {
    return communityApiClient.getAllOffenderDetails(crn).awaitFirst()
  }

  private fun age(offenderDetails: AllOffenderDetailsResponse) = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years
}
