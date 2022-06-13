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
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.KeyDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Registration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.RegistrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Type
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class CaseSummaryOverviewServiceTest : ServiceTestBase() {

  private lateinit var caseSummaryOverviewService: CaseSummaryOverviewService

  @BeforeEach
  fun setup() {
    caseSummaryOverviewService = CaseSummaryOverviewService(communityApiClient)
  }

  @Test
  fun `retrieves case summary when no offences available`() {
    runBlockingTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { emptyList<Conviction>() })
      given(communityApiClient.getRegistrations(anyString()))
        .willReturn(Mono.empty())

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val offencesShouldBeEmpty = response.offences
      val riskFlagsShouldBeEmpty = response.risk!!.flags

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse()))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(offencesShouldBeEmpty).isEmpty()
      assertThat(riskFlagsShouldBeEmpty).isEmpty()
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `retrieves case summary when offences available`() {
    runBlockingTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { listOf(convictionResponse) })
      given(communityApiClient.getRegistrations(anyString()))
        .willReturn(Mono.fromCallable { registrations })

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val offences = response.offences
      val riskFlags = response.risk!!.flags

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse()))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(offences?.size).isEqualTo(1)
      assertThat(offences!![0].mainOffence).isTrue
      assertThat(offences[0].description).isEqualTo("Robbery (other than armed robbery)")
      assertThat(riskFlags!!.size).isEqualTo(1)
      assertThat(riskFlags[0]).isEqualTo("Victim contact")
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
            allOffenderDetailsResponse().copy(
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
      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(
          Mono.fromCallable {
            listOf(
              convictionResponse.copy(
                offences = listOf(
                  Offence(
                    mainOffence = true,
                    detail = OffenceDetail(
                      mainCategoryDescription = null, subCategoryDescription = null,
                      description = null,
                      code = null
                    )
                  )
                )
              )
            )
          }
        )
      given(communityApiClient.getRegistrations(anyString()))
        .willReturn(
          Mono.fromCallable {
            registrations.copy(
              registrations = listOf(
                Registration(
                  active = true,
                  type = Type(code = null, description = null)
                ),
              )
            )
          }
        )

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val offences = response.offences
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years
      val riskFlags = response.risk!!.flags

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("")
      assertThat(offences?.size).isEqualTo(1)
      assertThat(offences!![0].mainOffence).isTrue
      assertThat(offences[0].description).isEqualTo("")
      assertThat(riskFlags!!.size).isEqualTo(1)
      assertThat(riskFlags[0]).isEqualTo("")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  fun age(offenderDetails: AllOffenderDetailsResponse) = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years

  private val convictionResponse = Conviction(
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
          description = "Robbery (other than armed robbery)",
          code = "ABC123"
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
    custody = Custody(
      status = CustodyStatus(code = "ABC123", description = "custody status"),
      keyDates = KeyDates(
        licenceExpiryDate = LocalDate.parse("2022-05-10"),
        postSentenceSupervisionEndDate = LocalDate.parse("2022-05-11"),
      )
    )
  )

  private val registrations = RegistrationsResponse(
    registrations = listOf(
      Registration(
        active = true,
        type = Type(code = "ABC123", description = "Victim contact")
      ),
      Registration(
        active = false,
        type = Type(code = "ABC124", description = "Mental health issues")
      )
    )
  )
}
