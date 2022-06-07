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
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.KeyDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceCondition
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeMainCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeSubCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SentenceType
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class LicenceConditionsServiceTest : ServiceTestBase() {

  private lateinit var licenceConditionsService: LicenceConditionsService

  @BeforeEach
  fun setup() {
    personDetailsService = PersonDetailsService(communityApiClient)
    licenceConditionsService = LicenceConditionsService(communityApiClient, personDetailsService)

    given(communityApiClient.getAllOffenderDetails(anyString()))
      .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
  }

  @Test
  fun `given an active conviction and licence conditions then return these details in the response`() {
    runBlockingTest {

      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { listOf(convictionResponse) })
      given(communityApiClient.getLicenceConditionsByConvictionId(anyString(), anyLong()))
        .willReturn(Mono.fromCallable { licenceConditions })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getLicenceConditionsByConvictionId(crn, 2500000001)
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            expectedPersonDetailsResponse(),
            expectedOffenceWithLicenceConditionsResponse(licenceConditions),
            allReleaseSummariesResponse()
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
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getLicenceConditionsByConvictionId(crn, 2500000001)
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            expectedPersonDetailsResponse(),
            expectedOffenceWithLicenceConditionsResponse(null),
            allReleaseSummariesResponse()
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
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).should().getReleaseSummary(crn)
      then(communityApiClient).shouldHaveNoMoreInteractions()

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            expectedPersonDetailsResponse(),
            emptyList(),
            allReleaseSummariesResponse()
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

  private fun expectedOffenceWithLicenceConditionsResponse(licenceConditions: LicenceConditions?): List<ConvictionResponse> {
    return listOf(
      ConvictionResponse(
        convictionId = 2500000001,
        active = true,
        offences = listOf(
          uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence(
            mainOffence = true,
            description = "Robbery (other than armed robbery)",
            code = "ABC123"
          ),
          uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence(
            mainOffence = false,
            description = "Arson",
            code = "ZYX789"
          )
        ),
        sentenceDescription = "Sentence description",
        sentenceOriginalLength = 2,
        sentenceOriginalLengthUnits = "years",
        sentenceStartDate = LocalDate.parse("2022-04-26"),
        licenceExpiryDate = LocalDate.parse("2022-05-10"),
        postSentenceSupervisionEndDate = LocalDate.parse("2022-05-11"),
        statusCode = "ABC123",
        statusDescription = "custody status",
        licenceConditions = licenceConditions?.licenceConditions
      )
    )
  }

  private val convictionResponse = Conviction(
    convictionDate = LocalDate.parse("2021-06-10"),
    sentence = Sentence(
      startDate = LocalDate.parse("2022-04-26"),
      terminationDate = LocalDate.parse("2022-04-26"),
      expectedSentenceEndDate = LocalDate.parse("2022-04-26"),
      description = "Sentence description", originalLength = 2,
      originalLengthUnits = "years",
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
      ),
      Offence(
        mainOffence = false,
        detail = OffenceDetail(
          mainCategoryDescription = "string", subCategoryDescription = "string",
          description = "Arson",
          code = "ZYX789"
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

  private val licenceConditions = LicenceConditions(
    licenceConditions = listOf(
      LicenceCondition(
        startDate = LocalDate.parse("2022-05-18"),
        createdDateTime = LocalDateTime.parse("2022-05-18T19:33:56"),
        active = true,
        terminationDate = LocalDate.parse("2022-05-22"),
        licenceConditionNotes = "Licence condition notes",
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
}
