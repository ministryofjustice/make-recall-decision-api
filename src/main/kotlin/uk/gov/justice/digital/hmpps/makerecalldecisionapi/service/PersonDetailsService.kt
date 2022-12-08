package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.formatFullName
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ProbationTeam
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.WHITE_SPACE
import java.time.LocalDate

@Service
internal class PersonDetailsService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val userAccessValidator: UserAccessValidator,
  @Lazy private val recommendationService: RecommendationService?
) {
  fun getPersonDetails(crn: String): PersonDetailsResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      PersonDetailsResponse(userAccessResponse = userAccessResponse)
    } else {
      val offenderDetails = getPersonalDetailsOverview(crn)
      val activeOffenderManager = offenderDetails.offenderManagers?.first { it.active ?: false }
      val trustOfficerForenames = activeOffenderManager?.trustOfficer?.forenames ?: ""
      val trustOfficerSurname = activeOffenderManager?.trustOfficer?.surname ?: ""
      val recommendationDetails = recommendationService?.getDraftRecommendationForCrn(crn)

      return PersonDetailsResponse(
        personalDetailsOverview = buildPersonalDetails(crn, offenderDetails),
        addresses = buildAddressDetails(offenderDetails.contactDetails?.addresses?.filter { it.status?.description?.lowercase().equals("main") }),
        offenderManager = OffenderManager(
          name = formatTwoWordField(trustOfficerForenames, trustOfficerSurname),
          phoneNumber = activeOffenderManager?.team?.telephone ?: "",
          email = activeOffenderManager?.team?.emailAddress ?: "",
          probationAreaDescription = activeOffenderManager?.probationArea?.description,
          probationTeam = ProbationTeam(
            code = activeOffenderManager?.team?.code ?: "",
            label = activeOffenderManager?.team?.description ?: "",
            localDeliveryUnitDescription = activeOffenderManager?.team?.localDeliveryUnit?.description
          )
        ),
        activeRecommendation = recommendationDetails
      )
    }
  }

  private fun buildAddressDetails(addresses: List<uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address>?): List<Address>? {
    return addresses?.map {
      val addressNumber = it.addressNumber ?: ""
      val streetName = it.streetName ?: ""
      val buildingName = it.buildingName ?: ""

      Address(
        line1 = formatTwoWordField(buildingName, formatTwoWordField(addressNumber, streetName)),
        line2 = it.district ?: "",
        town = it.town ?: "",
        postcode = it.postcode ?: "",
        noFixedAbode = isNoFixedAbode(it)
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
    val middleNames = offenderDetails.middleNames?.joinToString(WHITE_SPACE) ?: ""

    return PersonDetails(
      fullName = formatFullName(firstName, middleNames, surname),
      name = formatTwoWordField(firstName, surname),
      firstName = firstName,
      surname = surname,
      middleNames = middleNames,
      dateOfBirth = offenderDetails.dateOfBirth,
      age = age(offenderDetails),
      gender = offenderDetails.gender ?: "",
      crn = crn,
      ethnicity = offenderDetails.offenderProfile?.ethnicity ?: "",
      primaryLanguage = offenderDetails.offenderProfile?.offenderLanguages?.primaryLanguage ?: "",
      croNumber = offenderDetails.otherIds?.croNumber ?: "",
      pncNumber = offenderDetails.otherIds?.pncNumber ?: "",
      nomsNumber = offenderDetails.otherIds?.nomsNumber ?: "",
      mostRecentPrisonerNumber = offenderDetails.otherIds?.mostRecentPrisonerNumber ?: ""
    )
  }

  private fun isNoFixedAbode(it: uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address): Boolean {
    val postcodeUppercasedNoWhiteSpace = it.postcode
      ?.filter { !it.isWhitespace() }
      ?.uppercase()
    return postcodeUppercasedNoWhiteSpace == "NF11NF" || it.noFixedAbode == true
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
