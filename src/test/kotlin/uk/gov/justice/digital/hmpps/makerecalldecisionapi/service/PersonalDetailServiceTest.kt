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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PersonalDetailServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    personDetailsService = PersonDetailsService(deliusClient, userAccessValidator, recommendationService)
  }

  @Test
  fun `throws exception when no person details available`() {
    val nonExistentCrn = "this person doesn't exist"
    given(deliusClient.getPersonalDetails(anyString())).willThrow(PersonNotFoundException("No details available for crn: $nonExistentCrn"))

    assertThatThrownBy {
      runTest {
        personDetailsService.getPersonDetails(nonExistentCrn)
      }
    }.isInstanceOf(PersonNotFoundException::class.java).hasMessage("No details available for crn: $nonExistentCrn")

    then(deliusClient).should().getPersonalDetails(nonExistentCrn)
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {
      given(deliusClient.getUserAccess(username, crn)).willReturn(excludedAccess())

      val response = personDetailsService.getPersonDetails(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          PersonDetailsResponse(
            userAccessResponse(true, false, false).copy(restrictionMessage = null),
            null,
            null,
            null,
          ),
        ),
      )
    }
  }

  @Test
  fun `given user not found for user then return user access response details`() {
    runTest {
      given(deliusClient.getUserAccess(username, crn)).willThrow(PersonNotFoundException("Not found"))

      val response = personDetailsService.getPersonDetails(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          PersonDetailsResponse(
            userAccessResponse(false, false, true).copy(restrictionMessage = null, exclusionMessage = null),
            null,
            null,
            null,
          ),
        ),
      )
    }
  }

  @Test
  fun `retrieves person details when no registration available`() {
    runTest {
      val crn = "12345"
      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(userAccessResponse(false, false, false))
      given(deliusClient.getPersonalDetails(anyString())).willReturn(deliusPersonalDetailsResponse())

      val response = personDetailsService.getPersonDetails(crn)

      val personalDetails = response.personalDetailsOverview!!
      val currentAddresses = response.addresses!!
      val offenderManager = response.offenderManager!!
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years

      assertThat(personalDetails, equalTo(expectedPersonDetailsResponse()))
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.fullName).isEqualTo("Joe Michael Bloggs")
      assertThat(personalDetails.name).isEqualTo("Joe Bloggs")
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.primaryLanguage).isEqualTo("English")
      assertThat(currentAddresses[0].line1).isEqualTo("HMPPS Digital Studio 32 Jump Street")
      assertThat(currentAddresses[0].line2).isEqualTo("Sheffield City Centre")
      assertThat(currentAddresses[0].town).isEqualTo("Sheffield")
      assertThat(currentAddresses[0].postcode).isEqualTo("S12 345")
      assertThat(offenderManager.name).isEqualTo("Jane Linda Bloggs")
      assertThat(offenderManager.email).isEqualTo("first.last@digital.justice.gov.uk")
      assertThat(offenderManager.phoneNumber).isEqualTo("09056714321")
      assertThat(offenderManager.probationTeam?.code).isEqualTo("C01T04")
      assertThat(offenderManager.probationTeam?.label).isEqualTo("OMU A")
      then(deliusClient).should().getPersonalDetails(crn)
    }
  }

  @Test
  fun `retrieves person details when no addresses available`() {
    runTest {
      val crn = "12345"
      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(userAccessResponse(false, false, false))
      given(deliusClient.getPersonalDetails(anyString())).willReturn(deliusPersonalDetailsResponse(address = null))

      val response = personDetailsService.getPersonDetails(crn)

      val personalDetails = response.personalDetailsOverview!!
      val currentAddresses = response.addresses!!
      val offenderManager = response.offenderManager!!
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years

      assertThat(personalDetails, equalTo(expectedPersonDetailsResponse()))
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("Joe Bloggs")
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.primaryLanguage).isEqualTo("English")
      assertThat(currentAddresses).isEmpty()
      assertThat(offenderManager.name).isEqualTo("Jane Linda Bloggs")
      assertThat(offenderManager.email).isEqualTo("first.last@digital.justice.gov.uk")
      assertThat(offenderManager.phoneNumber).isEqualTo("09056714321")
      assertThat(offenderManager.probationTeam?.code).isEqualTo("C01T04")
      assertThat(offenderManager.probationTeam?.label).isEqualTo("OMU A")
      then(deliusClient).should().getPersonalDetails(crn)
    }
  }

  @Test
  fun `returns noFixedAbode as true when noFixedAbode postcode present`() {
    runTest {
      // given
      val crn = "12345"
      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(userAccessResponse(false, false, false))
      given(deliusClient.getPersonalDetails(anyString())).willReturn(
        deliusPersonalDetailsResponse(
          address = Address(
            postcode = "Nf1 1nf",
          ),
        ),
      )

      // when
      val response = personDetailsService.getPersonDetails(crn)

      // then
      val currentAddresses = response.addresses!!
      assertThat(currentAddresses[0].postcode).isEqualTo("Nf1 1nf")
      assertThat(currentAddresses[0].noFixedAbode).isEqualTo(true)
      then(deliusClient).should().getPersonalDetails(crn)
    }
  }

  @Test
  fun `retrieves person details when optional fields are missing`() {
    runTest {
      val crn = "12345"
      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(userAccessResponse(false, false, false))
      given(deliusClient.getPersonalDetails(anyString())).willReturn(
        deliusPersonalDetailsResponse(
          middleName = null,
          ethnicity = null,
          primaryLanguage = null,
          manager = null,
          address = Address(
            postcode = null,
            district = null,
            addressNumber = null,
            buildingName = null,
            town = null,
            county = null,
            streetName = null,
            noFixedAbode = null,
          ),
        ),
      )

      val response = personDetailsService.getPersonDetails(crn)

      val personalDetails = response.personalDetailsOverview!!
      val currentAddresses = response.addresses!!
      val offenderManager = response.offenderManager!!
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("Joe Bloggs")
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.ethnicity).isEqualTo("")
      assertThat(personalDetails.primaryLanguage).isEqualTo("")
      assertThat(currentAddresses[0].line1).isEqualTo("")
      assertThat(currentAddresses[0].line2).isEqualTo("")
      assertThat(currentAddresses[0].town).isEqualTo("")
      assertThat(currentAddresses[0].postcode).isEqualTo("")
      assertThat(offenderManager.name).isEqualTo("")
      assertThat(offenderManager.email).isEqualTo("")
      assertThat(offenderManager.phoneNumber).isEqualTo("")
      assertThat(offenderManager.probationTeam?.code).isEqualTo("")
      assertThat(offenderManager.probationTeam?.label).isEqualTo("")
      then(deliusClient).should().getPersonalDetails(crn)
    }
  }

  @Test
  fun `retrieve summary of person details`() {
    runTest {
      val crn = "12345"

      given(deliusClient.getPersonalDetails(anyString())).willReturn(deliusPersonalDetailsResponse())

      val response = personDetailsService.buildPersonalDetailsOverviewResponse(crn)

      then(deliusClient).should().getPersonalDetails(crn)

      assertThat(response, equalTo(expectedPersonDetailsResponse()))
    }
  }
}
