package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.LicenceHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactOutcome
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Content
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.EnforcementAction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.EnforcementActionType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.LastRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.LastRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.TrustOfficer
import java.time.LocalDate
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class LicenceHistoryServiceTest {

  private lateinit var licenceHistoryService: LicenceHistoryService

  private lateinit var personDetailsService: PersonDetailsService

  @Mock
  private lateinit var communityApiClient: CommunityApiClient

  val crn = "12345"

  @BeforeEach
  fun setup() {
    personDetailsService = PersonDetailsService(communityApiClient)
    licenceHistoryService = LicenceHistoryService(communityApiClient, personDetailsService)

    given(communityApiClient.getAllOffenderDetails(anyString()))
      .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
  }

  @Test
  fun `given a contact summary and release summary then return these details in the response`() {
    runBlockingTest {

      given(communityApiClient.getContactSummary(anyString(), true))
        .willReturn(Mono.fromCallable { allContactSummariesResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceHistoryService.getLicenceHistory(crn, true)

      then(communityApiClient).should().getContactSummary(crn, true)
      then(communityApiClient).should().getReleaseSummary(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)

      assertThat(response, equalTo(LicenceHistoryResponse(expectedPersonDetailsResponse(), expectedContactSummaryResponse(), allReleaseSummariesResponse())))
    }
  }

  @Test
  fun `given no release summary details then still retrieve contact summary details`() {
    runBlockingTest {

      given(communityApiClient.getContactSummary(anyString(), true))
        .willReturn(Mono.fromCallable { allContactSummariesResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.empty())

      val response = licenceHistoryService.getLicenceHistory(crn, true)

      then(communityApiClient).should().getContactSummary(crn, true)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(response, equalTo(LicenceHistoryResponse(expectedPersonDetailsResponse(), expectedContactSummaryResponse(), null)))
    }
  }

  @Test
  fun `given no contact summary details then still retrieve release summary details`() {
    runBlockingTest {

      given(communityApiClient.getContactSummary(anyString(), true))
        .willReturn(Mono.empty())
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceHistoryService.getLicenceHistory(crn, true)

      then(communityApiClient).should().getContactSummary(crn, true)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(response, equalTo(LicenceHistoryResponse(expectedPersonDetailsResponse(), emptyList(), allReleaseSummariesResponse())))
    }
  }

  @Test
  fun `given no contact summary details and no release summary details then still return an empty response`() {
    runBlockingTest {

      given(communityApiClient.getContactSummary(anyString(), true))
        .willReturn(Mono.empty())
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.empty())

      val response = licenceHistoryService.getLicenceHistory(crn, true)

      then(communityApiClient).should().getContactSummary(crn, true)
      then(communityApiClient).should().getReleaseSummary(crn)

      assertThat(response, equalTo(LicenceHistoryResponse(expectedPersonDetailsResponse(), emptyList(), null)))
    }
  }

  private fun expectedPersonDetailsResponse(): PersonDetails {
    val dateOfBirth = LocalDate.parse("1982-10-24")

    return PersonDetails(
      name = "John Smith",
      dateOfBirth = dateOfBirth,
      age = dateOfBirth?.until(LocalDate.now())?.years,
      gender = "Male",
      crn = "12345"
    )
  }

  private fun expectedContactSummaryResponse(): List<ContactSummaryResponse> {
    return listOf(
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-06-03T07:00Z"),
        descriptionType = "Registration Review",
        outcome = null,
        notes = "Comment added by John Smith on 05/05/2022",
        enforcementAction = null
      ),
      ContactSummaryResponse(
        contactStartDate = OffsetDateTime.parse("2022-05-10T10:39Z"),
        descriptionType = "Police Liaison",
        outcome = "Test - Not Clean / Not Acceptable / Unsuitable",
        notes = "This is a test",
        enforcementAction = "Enforcement Letter Requested"
      )
    )
  }

  private fun allContactSummariesResponse(): ContactSummaryResponseCommunity {
    return ContactSummaryResponseCommunity(
      content = listOf(
        Content(
          contactStart = OffsetDateTime.parse("2022-06-03T07:00Z"),
          type = ContactType(description = "Registration Review"),
          outcome = null,
          notes = "Comment added by John Smith on 05/05/2022",
          enforcement = null,
        ),
        Content(
          contactStart = OffsetDateTime.parse("2022-05-10T10:39Z"),
          type = ContactType(description = "Police Liaison"),
          outcome = ContactOutcome(description = "Test - Not Clean / Not Acceptable / Unsuitable"),
          notes = "This is a test",
          enforcement = EnforcementAction(enforcementAction = EnforcementActionType(description = "Enforcement Letter Requested")),
        )
      )
    )
  }

  private fun allReleaseSummariesResponse(): ReleaseSummaryResponse {

    return ReleaseSummaryResponse(
      lastRelease = LastRelease(date = LocalDate.parse("2017-09-15")),
      lastRecall = LastRecall(date = LocalDate.parse("2020-10-15"))
    )
  }

  private fun allOffenderDetailsResponse(): AllOffenderDetailsResponse {
    return AllOffenderDetailsResponse(
      dateOfBirth = LocalDate.parse("1982-10-24"),
      firstName = "John",
      surname = "Smith",
      gender = "Male",
      contactDetails = ContactDetails(
        addresses = listOf(
          Address(
            postcode = "S3 7BS",
            district = "Sheffield City Centre",
            addressNumber = "32",
            buildingName = "HMPPS Digital Studio",
            town = "Sheffield",
            county = "South Yorkshire", status = AddressStatus(code = "ABC123", description = "Main")
          ),
          Address(
            town = "Sheffield",
            county = "South Yorkshire",
            buildingName = "HMPPS Digital Studio",
            district = "Sheffield City Centre",
            status = AddressStatus(code = "ABC123", description = "Not Main"),
            postcode = "S3 7BS",
            addressNumber = "33"
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
}
