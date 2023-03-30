package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.PersonalDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.formatFullName
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonalDetailsOverview
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ProbationTeam
import java.time.LocalDate

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
      val personalDetails = deliusClient.getPersonalDetails(crn)
      val manager = personalDetails.communityManager
      val recommendationDetails = recommendationService?.getRecommendationsInProgressForCrn(crn)

      return PersonDetailsResponse(
        personalDetailsOverview = personalDetails.toOverview(crn),
        addresses = listOfNotNull(
          personalDetails.mainAddress?.let {
            Address(
              line1 = join(it.buildingName, it.addressNumber, it.streetName),
              line2 = it.district ?: "",
              town = it.town ?: "",
              postcode = it.postcode ?: "",
              noFixedAbode = isNoFixedAbode(it)
            )
          }
        ),
        offenderManager = OffenderManager(
          name = join(manager?.name?.forename, manager?.name?.middleName, manager?.name?.surname),
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

  fun buildPersonalDetailsOverviewResponse(crn: String) = deliusClient.getPersonalDetails(crn).toOverview(crn)

  fun PersonalDetails.toOverview(crn: String) = with(personalDetails) {
    PersonalDetailsOverview(
      crn = crn,
      fullName = formatFullName(name.forename, name.middleName, name.surname),
      name = join(name.forename, name.surname),
      firstName = name.forename,
      middleNames = name.middleName,
      surname = name.surname,
      dateOfBirth = dateOfBirth,
      age = dateOfBirth.until(LocalDate.now())?.years,
      gender = gender,
      ethnicity = ethnicity ?: "",
      primaryLanguage = primaryLanguage ?: "",
      croNumber = identifiers.croNumber ?: "",
      pncNumber = identifiers.pncNumber ?: "",
      nomsNumber = identifiers.nomsNumber ?: "",
      mostRecentPrisonerNumber = identifiers.bookingNumber ?: ""
    )
  }

  private fun isNoFixedAbode(it: PersonalDetails.Address): Boolean {
    val postcodeUppercaseNoWhiteSpace = it.postcode?.filter { !it.isWhitespace() }?.uppercase()
    return postcodeUppercaseNoWhiteSpace == "NF11NF" || it.noFixedAbode == true
  }

  private fun join(vararg parts: String?) = parts.filter { !it.isNullOrBlank() }.joinToString(" ")
}
