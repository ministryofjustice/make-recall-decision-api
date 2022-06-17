package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.mockito.Mock
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LastRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Reason
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import java.time.LocalDate

abstract class ServiceTestBase {

  @Mock
  protected lateinit var communityApiClient: CommunityApiClient

  protected lateinit var personDetailsService: PersonDetailsService

  protected val crn = "12345"

  protected fun allReleaseSummariesResponse(): ReleaseSummaryResponse {

    return ReleaseSummaryResponse(
      lastRelease = LastRelease(date = LocalDate.parse("2017-09-15"), notes = "I am a note", reason = Reason(code = "reasonCode", description = "reasonDescription")),
      lastRecall = LastRecall(date = LocalDate.parse("2020-10-15"), notes = "I am a second note", reason = Reason(code = "another reason code", description = "another reason description"))
    )
  }

  protected fun allOffenderDetailsResponse(): AllOffenderDetailsResponse {
    return AllOffenderDetailsResponse(
      dateOfBirth = LocalDate.parse("1982-10-24"),
      firstName = "John",
      surname = "Smith",
      gender = "Male",
      contactDetails = ContactDetails(
        addresses = listOf(
          Address(
            town = "Compton",
            county = "LA",
            buildingName = "HMPPS Digital Studio",
            district = "South Central",
            status = AddressStatus(code = "ABC123", description = "Not Main"),
            postcode = "90210",
            addressNumber = "339",
            streetName = "Sesame Street"
          ),
          Address(
            postcode = "S3 7BS",
            district = "Sheffield City Centre",
            addressNumber = "32",
            buildingName = "HMPPS Digital Studio",
            town = "Sheffield",
            county = "South Yorkshire", status = AddressStatus(code = "ABC123", description = "Main"),
            streetName = "Jump Street"
          )
        )
      ),
      offenderManagers = listOf(
        OffenderManager(
          active = true,
          trustOfficer = TrustOfficer(forenames = "Sheila Linda", surname = "Hancock"),
          staff = Staff(forenames = "Sheila Linda", surname = "Hancock"),
          providerEmployee = ProviderEmployee(forenames = "Sheila Linda", surname = "Hancock"),
          team = Team(
            telephone = "09056714321",
            emailAddress = "first.last@digital.justice.gov.uk",
            code = "C01T04",
            description = "OMU A"
          )
        ),
        OffenderManager(
          active = false,
          trustOfficer = TrustOfficer(forenames = "Dua", surname = "Lipa"),
          staff = Staff(forenames = "Sheila Linda", surname = "Hancock"),
          providerEmployee = ProviderEmployee(forenames = "Sheila Linda", surname = "Hancock"),
          team = Team(
            telephone = "123",
            emailAddress = "dua.lipa@digital.justice.gov.uk",
            code = "C01T04",
            description = "OMU A"
          )
        )
      )
    )
  }

  protected fun userAccessResponse(excluded: Boolean, restricted: Boolean) = UserAccessResponse(
    userRestricted = restricted,
    userExcluded = excluded,
    exclusionMessage = "I am an exclusion message",
    restrictionMessage = "I am a restriction message"
  )
}
