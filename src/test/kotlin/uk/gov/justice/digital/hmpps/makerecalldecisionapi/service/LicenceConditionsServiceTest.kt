package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceCondition
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeMainCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeSubCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceWithLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.TrustOfficer
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class LicenceConditionsServiceTest {

  private lateinit var licenceConditionsService: LicenceConditionsService

  private lateinit var personDetailsService: PersonDetailsService

  @Mock
  private lateinit var communityApiClient: CommunityApiClient

  val crn = "12345"

  @BeforeEach
  fun setup() {
    personDetailsService = PersonDetailsService(communityApiClient)
    licenceConditionsService = LicenceConditionsService(communityApiClient, personDetailsService)

    given(communityApiClient.getAllOffenderDetails(anyString()))
      .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
  }

  @Test
  fun `given an active conviction and an active licence condition then return these details in the response`() {
    runBlockingTest {

      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { listOf(convictionResponse) })
      given(communityApiClient.getLicenceConditionsByConvictionId(anyString(), anyLong()))
        .willReturn(Mono.fromCallable { licenceConditions })

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getLicenceConditionsByConvictionId(crn, 2500000001)
      then(communityApiClient).should().getAllOffenderDetails(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            expectedPersonDetailsResponse(),
            expectedOffenceWithLicenceConditionsResponse()
          )
        )
      )
    }
  }

  @Test
  fun `given no active licence conditions then still retrieve conviction details`() {
    runBlockingTest {

      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { listOf(convictionResponse) })
      given(communityApiClient.getLicenceConditionsByConvictionId(anyString(), anyLong()))
        .willReturn(Mono.empty())

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getLicenceConditionsByConvictionId(crn, 2500000001)
      then(communityApiClient).should().getAllOffenderDetails(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            expectedPersonDetailsResponse(),
            expectedOffenceWithEmptyLicenceConditionsResponse()
          )
        )
      )
    }
  }

  @Test
  fun `given no offender details then still retrieve personal details`() {
    runBlockingTest {

      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { emptyList() })

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).shouldHaveNoMoreInteractions()

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            expectedPersonDetailsResponse(),
            emptyList()
          )
        )
      )
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

  private fun expectedOffenceWithLicenceConditionsResponse(): List<OffenceWithLicenceConditions> {
    return listOf(
      OffenceWithLicenceConditions(
        convictionId = 2500000001,
        licenceConditions = licenceConditions.licenceConditions
      )
    )
  }

  private fun expectedOffenceWithEmptyLicenceConditionsResponse(): List<OffenceWithLicenceConditions> {
    return listOf(
      OffenceWithLicenceConditions(
        convictionId = 2500000001,
        licenceConditions = null
      )
    )
  }

  private val convictionResponse = ConvictionResponse(
    convictionDate = LocalDate.parse("2021-06-10"),
    sentence = Sentence(
      startDate = LocalDate.parse("2022-04-26"),
      terminationDate = LocalDate.parse("2022-04-26"),
      expectedSentenceEndDate = LocalDate.parse("2022-04-26"),
      description = "string", originalLength = 0,
      originalLengthUnits = "string",
      sentenceType = SentenceType(code = "ABC123")
    ),
    active = true,
    offences = listOf(
      Offence(
        mainOffence = true,
        detail = OffenceDetail(
          mainCategoryDescription = "string", subCategoryDescription = "string",
          description = "Robbery (other than armed robbery)"
        )
      )
    ),
    convictionId = 2500000001,
    orderManagers =
    listOf(
      OrderManager(
        dateStartOfAllocation = LocalDateTime.parse("2022-04-26T20:39:47.778"),
        name = "string",
        staffCode = "STFFCDEU",
        gradeCode = "string"
      )
    ),
    custody = Custody(status = CustodyStatus(code = "ABC123"))
  )

  val licenceConditions = LicenceConditions(
    licenceConditions = listOf(
      LicenceCondition(
        startDate = LocalDate.parse("2022-05-18"),
        createdDateTime = LocalDateTime.parse("2022-05-18T19:33:56"),
        active = true,
        licenceConditionTypeMainCat = LicenceConditionTypeMainCat(
          code = "NLC8",
          description = "Freedom of movement"
        ),
        licenceConditionTypeSubCat = LicenceConditionTypeSubCat(
          code = "NSTT8",
          description = "To only attend places of worship which have been previously agreed with your supervising officer."
        )
      )
    )
  )

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
