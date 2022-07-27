package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CaseSummaryOverviewResponse
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
internal class CaseSummaryOverviewServiceTest : ServiceTestBase() {

  private lateinit var caseSummaryOverviewService: CaseSummaryOverviewService

  @BeforeEach
  fun setup() {
    caseSummaryOverviewService = CaseSummaryOverviewService(communityApiClient, userAccessValidator, recommendationService)

    given(communityApiClient.getUserAccess(anyString()))
      .willReturn(Mono.fromCallable { userAccessResponse(false, false) })
  }

  @Test
  fun `retrieves case summary when convictions and offences available`() {
    runTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { listOf(convictionResponse) })
      given(communityApiClient.getRegistrations(anyString()))
        .willReturn(Mono.fromCallable { registrations })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val convictions = response.convictions
      val riskFlags = response.risk!!.flags

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse()))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(convictions?.size).isEqualTo(1)
      assertThat(convictions!![0].offences?.size).isEqualTo(1)
      assertThat(convictions[0].active).isTrue
      assertThat(convictions[0].offences!![0].mainOffence).isTrue
      assertThat(convictions[0].offences!![0].description).isEqualTo("Robbery (other than armed robbery)")
      assertThat(convictions[0].sentenceDescription).isEqualTo("Sentence description")
      assertThat(convictions[0].sentenceOriginalLength).isEqualTo(6)
      assertThat(convictions[0].sentenceOriginalLengthUnits).isEqualTo("Days")
      assertThat(convictions[0].sentenceExpiryDate).isEqualTo("2022-06-10")
      assertThat(convictions[0].licenceExpiryDate).isEqualTo("2022-05-10")
      assertThat(riskFlags!!.size).isEqualTo(1)
      assertThat(response.releaseSummary?.lastRelease?.date).isEqualTo("2017-09-15")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `retrieves case summary when no convictions available`() {
    runTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { emptyList() })
      given(communityApiClient.getRegistrations(anyString()))
        .willReturn(Mono.empty())
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = caseSummaryOverviewService.getOverview(crn)

      val personalDetails = response.personalDetailsOverview!!
      val convictionsShouldBeEmpty = response.convictions
      val riskFlagsShouldBeEmpty = response.risk!!.flags

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse()))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(convictionsShouldBeEmpty).isEmpty()
      assertThat(riskFlagsShouldBeEmpty).isEmpty()
      assertThat(response.releaseSummary?.lastRelease?.date).isEqualTo("2017-09-15")
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          403, "Forbidden", null, excludedResponse().toByteArray(), null
        )
      )

      val response = caseSummaryOverviewService.getOverview(crn)

      then(communityApiClient).should().getUserAccess(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          CaseSummaryOverviewResponse(
            userAccessResponse(true, false).copy(restrictionMessage = null), null, null, null
          )
        )
      )
    }
  }

  @Test
  fun `retrieves case summary with optional fields missing`() {
    runTest {
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
      val convictions = response.convictions
      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years
      val riskFlags = response.risk!!.flags

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("")
      assertThat(convictions!![0].offences?.size).isEqualTo(1)
      assertThat(convictions[0].offences!![0].mainOffence).isTrue
      assertThat(convictions[0].offences!![0].description).isEqualTo("")
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
      description = "Sentence description", originalLength = 6,
      originalLengthUnits = "Days",
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
        sentenceExpiryDate = LocalDate.parse("2022-06-10"),
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
