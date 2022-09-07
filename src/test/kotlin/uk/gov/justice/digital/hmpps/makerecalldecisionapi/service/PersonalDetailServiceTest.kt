package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PersonalDetailServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    personDetailsService = PersonDetailsService(communityApiClient, userAccessValidator, recommendationService)
  }

  @Test
  fun `throws exception when no person details available`() {
    val nonExistentCrn = "this person doesn't exist"
    given(communityApiClient.getUserAccess(anyString()))
      .willReturn(Mono.fromCallable { userAccessResponse(false, false) })
    given(communityApiClient.getAllOffenderDetails(anyString()))
      .willThrow(PersonNotFoundException("No details available for crn: $nonExistentCrn"))

    assertThatThrownBy {
      runTest {
        personDetailsService.getPersonDetails(nonExistentCrn)
      }
    }.isInstanceOf(PersonNotFoundException::class.java)
      .hasMessage("No details available for crn: $nonExistentCrn")

    then(communityApiClient).should().getAllOffenderDetails(nonExistentCrn)
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          403, "Forbidden", null, excludedResponse().toByteArray(), null
        )
      )

      val response = personDetailsService.getPersonDetails(crn)

      then(communityApiClient).should().getUserAccess(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          PersonDetailsResponse(
            userAccessResponse(true, false).copy(restrictionMessage = null), null, null, null
          )
        )
      )
    }
  }

  @Test
  fun `retrieves person details when no registration available`() {
    runTest {
      val crn = "12345"
      given(communityApiClient.getUserAccess(anyString()))
        .willReturn(Mono.fromCallable { userAccessResponse(false, false) })
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse().copy(middleNames = listOf("Homer Bart")) })

      val response = personDetailsService.getPersonDetails(crn)

      val personalDetails = response.personalDetailsOverview!!
      val currentAddress = response.currentAddress!!
      val offenderManager = response.offenderManager!!
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years

      assertThat(personalDetails, equalTo(expectedPersonDetailsResponse()))
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(currentAddress.line1).isEqualTo("HMPPS Digital Studio 32 Jump Street")
      assertThat(currentAddress.line2).isEqualTo("Sheffield City Centre")
      assertThat(currentAddress.town).isEqualTo("Sheffield")
      assertThat(currentAddress.postcode).isEqualTo("S3 7BS")
      assertThat(offenderManager.name).isEqualTo("Sheila Linda Hancock")
      assertThat(offenderManager.email).isEqualTo("first.last@digital.justice.gov.uk")
      assertThat(offenderManager.phoneNumber).isEqualTo("09056714321")
      assertThat(offenderManager.probationTeam?.code).isEqualTo("C01T04")
      assertThat(offenderManager.probationTeam?.label).isEqualTo("OMU A")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `retrieves person details when no addresses available`() {
    runTest {
      val crn = "12345"
      given(communityApiClient.getUserAccess(anyString()))
        .willReturn(Mono.fromCallable { userAccessResponse(false, false) })
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(
          Mono.fromCallable {
            allOffenderDetailsResponse().copy(
              middleNames = listOf("Homer Bart"),
              contactDetails = ContactDetails(
                addresses = emptyList()
              )
            )
          }
        )

      val response = personDetailsService.getPersonDetails(crn)

      val personalDetails = response.personalDetailsOverview!!
      val currentAddress = response.currentAddress!!
      val offenderManager = response.offenderManager!!
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years

      assertThat(personalDetails, equalTo(expectedPersonDetailsResponse()))
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(currentAddress.line1).isEqualTo("")
      assertThat(currentAddress.line2).isEqualTo("")
      assertThat(currentAddress.town).isEqualTo("")
      assertThat(currentAddress.postcode).isEqualTo("")
      assertThat(offenderManager.name).isEqualTo("Sheila Linda Hancock")
      assertThat(offenderManager.email).isEqualTo("first.last@digital.justice.gov.uk")
      assertThat(offenderManager.phoneNumber).isEqualTo("09056714321")
      assertThat(offenderManager.probationTeam?.code).isEqualTo("C01T04")
      assertThat(offenderManager.probationTeam?.label).isEqualTo("OMU A")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `retrieves person details when only previous address available`() {
    runTest {
      val crn = "12345"
      given(communityApiClient.getUserAccess(anyString()))
        .willReturn(Mono.fromCallable { userAccessResponse(false, false) })
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(
          Mono.fromCallable {
            allOffenderDetailsResponse().copy(
              middleNames = listOf("Homer Bart"),
              contactDetails = ContactDetails(
                addresses = listOf(
                  Address(
                    status = AddressStatus(code = "ABC123", description = "Previous"),
                  )
                )
              )
            )
          }
        )

      val response = personDetailsService.getPersonDetails(crn)

      val personalDetails = response.personalDetailsOverview!!
      val currentAddress = response.currentAddress!!
      val offenderManager = response.offenderManager!!
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails, equalTo(expectedPersonDetailsResponse()))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(currentAddress.line1).isEqualTo("")
      assertThat(currentAddress.line2).isEqualTo("")
      assertThat(currentAddress.town).isEqualTo("")
      assertThat(currentAddress.postcode).isEqualTo("")
      assertThat(offenderManager.name).isEqualTo("Sheila Linda Hancock")
      assertThat(offenderManager.email).isEqualTo("first.last@digital.justice.gov.uk")
      assertThat(offenderManager.phoneNumber).isEqualTo("09056714321")
      assertThat(offenderManager.probationTeam?.code).isEqualTo("C01T04")
      assertThat(offenderManager.probationTeam?.label).isEqualTo("OMU A")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `retrieves person details when registration available`() {
    runTest {
      val crn = "12345"
      given(communityApiClient.getUserAccess(anyString()))
        .willReturn(Mono.fromCallable { userAccessResponse(false, false) })
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse().copy(middleNames = listOf("Homer Bart")) })

      val response = personDetailsService.getPersonDetails(crn)

      val personalDetails = response.personalDetailsOverview!!
      val currentAddress = response.currentAddress!!
      val offenderManager = response.offenderManager!!
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years

      assertThat(personalDetails, equalTo(expectedPersonDetailsResponse()))
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.croNumber).isEqualTo("123456/04A")
      assertThat(personalDetails.mostRecentPrisonerNumber).isEqualTo("G12345")
      assertThat(personalDetails.nomsNumber).isEqualTo("A1234CR")
      assertThat(personalDetails.pncNumber).isEqualTo("2004/0712343H")
      assertThat(currentAddress.line1).isEqualTo("HMPPS Digital Studio 32 Jump Street")
      assertThat(currentAddress.line2).isEqualTo("Sheffield City Centre")
      assertThat(currentAddress.town).isEqualTo("Sheffield")
      assertThat(currentAddress.postcode).isEqualTo("S3 7BS")
      assertThat(offenderManager.name).isEqualTo("Sheila Linda Hancock")
      assertThat(offenderManager.email).isEqualTo("first.last@digital.justice.gov.uk")
      assertThat(offenderManager.phoneNumber).isEqualTo("09056714321")
      assertThat(offenderManager.probationTeam?.code).isEqualTo("C01T04")
      assertThat(offenderManager.probationTeam?.label).isEqualTo("OMU A")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `retrieves person details when optional fields are missing`() {
    runTest {
      val crn = "12345"
      given(communityApiClient.getUserAccess(anyString()))
        .willReturn(Mono.fromCallable { userAccessResponse(false, false) })
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(
          Mono.fromCallable {
            allOffenderDetailsResponse()
              .copy(
                gender = null,
                firstName = null,
                surname = null,
                contactDetails = ContactDetails(
                  addresses = listOf(
                    Address(
                      postcode = null,
                      district = null,
                      addressNumber = null,
                      buildingName = null,
                      town = null,
                      county = null, status = AddressStatus(code = "ABC123", description = "Main"),
                      streetName = null
                    )
                  )
                ),
                offenderManagers = listOf(
                  OffenderManager(
                    active = true,
                    trustOfficer = TrustOfficer(forenames = null, surname = null),
                    staff = Staff(forenames = null, surname = null),
                    providerEmployee = ProviderEmployee(forenames = null, surname = null),
                    team = Team(
                      telephone = null,
                      emailAddress = null,
                      code = null,
                      description = null
                    )
                  )
                )
              )
          }
        )

      val response = personDetailsService.getPersonDetails(crn)

      val personalDetails = response.personalDetailsOverview!!
      val currentAddress = response.currentAddress!!
      val offenderManager = response.offenderManager!!
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("")
      assertThat(personalDetails.gender).isEqualTo("")
      assertThat(currentAddress.line1).isEqualTo("")
      assertThat(currentAddress.line2).isEqualTo("")
      assertThat(currentAddress.town).isEqualTo("")
      assertThat(currentAddress.postcode).isEqualTo("")
      assertThat(offenderManager.name).isEqualTo("")
      assertThat(offenderManager.email).isEqualTo("")
      assertThat(offenderManager.phoneNumber).isEqualTo("")
      assertThat(offenderManager.probationTeam?.code).isEqualTo("")
      assertThat(offenderManager.probationTeam?.label).isEqualTo("")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `retrieve summary of person details`() {
    runTest {

      val crn = "12345"

      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse().copy(middleNames = listOf("Homer Bart")) })

      val response = personDetailsService.buildPersonalDetailsOverviewResponse(crn)

      then(communityApiClient).should().getAllOffenderDetails(crn)

      assertThat(response, equalTo(expectedPersonDetailsResponse()))
    }
  }

  private fun expectedPersonDetailsResponse(): PersonDetails {
    val dateOfBirth = LocalDate.parse("1982-10-24")

    return PersonDetails(
      name = "John Smith",
      firstName = "John",
      surname = "Smith",
      dateOfBirth = dateOfBirth,
      age = dateOfBirth?.until(LocalDate.now())?.years,
      gender = "Male",
      crn = "12345",
      ethnicity = "Ainu",
      middleNames = "Homer Bart",
      croNumber = "123456/04A",
      mostRecentPrisonerNumber = "G12345",
      nomsNumber = "A1234CR",
      pncNumber = "2004/0712343H"
    )
  }
}
