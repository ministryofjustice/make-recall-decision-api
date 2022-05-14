package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.TrustOfficer
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class CaseSummaryOverviewServiceTest {

  private lateinit var caseSummaryOverviewService: CaseSummaryOverviewService

  @Mock
  private lateinit var communityApiClient: CommunityApiClient

  @BeforeEach
  fun setup() {
    caseSummaryOverviewService = CaseSummaryOverviewService(communityApiClient)
  }

  @Test
  fun `retrieves case summary when no offences available`() {
    runBlockingTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse })
      given(communityApiClient.getConvictions(anyString()))
        .willReturn(Mono.fromCallable { emptyList<ConvictionResponse>() })

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val offencesShouldBeEmpty = response.offences
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(offencesShouldBeEmpty).isEmpty()
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `retrieves case summary when offences available`() {
    runBlockingTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse })
      given(communityApiClient.getConvictions(anyString()))
        .willReturn(Mono.fromCallable { listOf(convictionResponse) })

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val offences = response.offences
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(offences?.size).isEqualTo(1)
      assertThat(offences!![0].mainOffence).isTrue
      assertThat(offences[0].description).isEqualTo("Robbery (other than armed robbery)")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `retrieves case summary with optional fields missing`() {
    runBlockingTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(
          Mono.fromCallable {
            allOffenderDetailsResponse.copy(
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
                    county = null, status = AddressStatus(code = "ABC123", description = "Main")
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
      given(communityApiClient.getConvictions(anyString()))
        .willReturn(
          Mono.fromCallable {
            listOf(
              convictionResponse.copy(
                offences = listOf(
                  Offence(
                    mainOffence = true,
                    detail = OffenceDetail(
                      mainCategoryDescription = null, subCategoryDescription = null,
                      description = null
                    )
                  )
                )
              )
            )
          }
        )

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val offences = response.offences
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("")
      assertThat(offences?.size).isEqualTo(1)
      assertThat(offences!![0].mainOffence).isTrue
      assertThat(offences[0].description).isEqualTo("")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  fun age(offenderDetails: AllOffenderDetailsResponse) = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years

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

  private val allOffenderDetailsResponse = AllOffenderDetailsResponse(
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
