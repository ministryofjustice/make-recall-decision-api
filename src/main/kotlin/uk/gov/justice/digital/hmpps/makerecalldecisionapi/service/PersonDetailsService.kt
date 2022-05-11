package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.CurrentAddress
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ProbationTeam
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Risk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import java.time.LocalDate

@Service
class PersonDetailsService(
  private val communityApiClient: CommunityApiClient
) {
  suspend fun getPersonDetails(crn: String): PersonDetailsResponse {
    val offenderDetails = communityApiClient.getAllOffenderDetails(crn).awaitFirstOrNull()
      ?: throw PersonNotFoundException("No details available for crn: $crn")

    val age = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years

    val activeOffenderManager = offenderDetails.offenderManagers?.first { it.active ?: false }
    val activeAddress = offenderDetails.contactDetails?.addresses
      ?.first { it.status?.description?.lowercase().equals("main") }
    val addressNumber = activeAddress?.addressNumber
    val buildingName = activeAddress?.buildingName

    val trustOfficerForenames = activeOffenderManager?.trustOfficer?.forenames
    val trustOfficerSurname = activeOffenderManager?.trustOfficer?.surname

    val registrations = communityApiClient.getRegistrations(crn).awaitFirstOrNull()?.registrations
    val activeRegistrations = registrations?.filter { it.active ?: false }
    val riskFlags = activeRegistrations?.map { it.type?.description }

    return PersonDetailsResponse(
      personDetails = PersonDetails(
        name = "${offenderDetails.firstName} ${offenderDetails.surname}",
        dateOfBirth = offenderDetails.dateOfBirth,
        age = age,
        gender = offenderDetails.gender,
        crn = crn
      ),
      currentAddress = CurrentAddress(
        line1 = "$addressNumber $buildingName",
        line2 = offenderDetails.contactDetails?.addresses?.get(0)?.district,
        town = offenderDetails.contactDetails?.addresses?.get(0)?.town,
        postcode = offenderDetails.contactDetails?.addresses?.get(0)?.postcode
      ),
      offenderManager = OffenderManager(
        name = "$trustOfficerForenames $trustOfficerSurname",
        phoneNumber = activeOffenderManager?.team?.telephone,
        email = activeOffenderManager?.team?.emailAddress,
        probationTeam = ProbationTeam(
          code = activeOffenderManager?.team?.code,
          label = activeOffenderManager?.team?.description
        )
      ),
      risk = Risk(flags = riskFlags)
    )
  }
}
