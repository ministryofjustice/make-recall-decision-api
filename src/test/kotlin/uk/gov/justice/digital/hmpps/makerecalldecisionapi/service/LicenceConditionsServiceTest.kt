package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionSearch
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceCondition
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeMainCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeSubCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditions
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class LicenceConditionsServiceTest : ServiceTestBase() {

  private lateinit var licenceConditionsService: LicenceConditionsService

  @BeforeEach
  fun setup() {
    personDetailsService = PersonDetailsService(communityApiClient, userAccessValidator, recommendationService)
    licenceConditionsService = LicenceConditionsService(communityApiClient, personDetailsService, userAccessValidator, convictionService, createAndVaryALicenceService, recommendationService)

    given(communityApiClient.getUserAccess(anyString()))
      .willReturn(Mono.fromCallable { userAccessResponse(false, false, false) })
  }

  @Test
  fun `given an active conviction and licence conditions then return these details in the response`() {
    runTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getActiveConvictions(anyString(), anyBoolean()))
        .willReturn(Mono.fromCallable { listOf(custodialConvictionResponse()) })
      given(communityApiClient.getLicenceConditionsByConvictionId(anyString(), anyLong()))
        .willReturn(Mono.fromCallable { licenceConditions })
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getLicenceConditionsByConvictionId(crn, 2500614567)
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).should().getGroupedDocuments(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            null,
            expectedPersonDetailsResponse(),
            expectedOffenceWithLicenceConditionsResponse(licenceConditions),
            allReleaseSummariesResponse()
          )
        )
      )
    }
  }

  @Test
  fun `given an active non custodial conviction then set custodial flag to false`() {
    runTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getActiveConvictions(anyString(), anyBoolean()))
        .willReturn(Mono.fromCallable { listOf(nonCustodialConvictionResponse()) })
      given(communityApiClient.getLicenceConditionsByConvictionId(anyString(), anyLong()))
        .willReturn(Mono.fromCallable { licenceConditions })
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceConditionsService.getLicenceConditions(crn)

      Assertions.assertThat(response.convictions!![0].isCustodial).isFalse
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

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getUserAccess(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            userAccessResponse(true, false, false).copy(restrictionMessage = null, userNotFoundMessage = null), null, null, null
          )
        )
      )
    }
  }

  @Test
  fun `given user is not found for user then return user access response details`() {
    runTest {

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          404, "Forbidden", null, null, null
        )
      )

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getUserAccess(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            userAccessResponse(false, false, true).copy(restrictionMessage = null, exclusionMessage = null), null, null, null
          )
        )
      )
    }
  }

  @Test
  fun `given no active licence conditions then still retrieve conviction details`() {
    runTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getActiveConvictions(anyString(), anyBoolean()))
        .willReturn(Mono.fromCallable { listOf(custodialConvictionResponse()) })
      given(communityApiClient.getLicenceConditionsByConvictionId(anyString(), anyLong()))
        .willReturn(Mono.empty())
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getLicenceConditionsByConvictionId(crn, 2500614567)
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).should().getGroupedDocuments(crn)
      then(communityApiClient).should().getReleaseSummary(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            null,
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
    runTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(communityApiClient.getActiveConvictions(anyString(), anyBoolean()))
        .willReturn(Mono.fromCallable { emptyList() })
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })
      given(communityApiClient.getReleaseSummary(anyString()))
        .willReturn(Mono.fromCallable { allReleaseSummariesResponse() })

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).should().getReleaseSummary(crn)
      then(communityApiClient).should().getGroupedDocuments(crn)
      then(communityApiClient).shouldHaveNoMoreInteractions()

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            null,
            expectedPersonDetailsResponse(),
            emptyList(),
            allReleaseSummariesResponse()
          )
        )
      )
    }
  }

  @Test
  fun `given licence conditions from CVL then return these details in the response`() {
    runTest {
      val licenceId = 444333
      val nomsId = "A1234CR"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(cvlApiClient.getLicenceMatch(crn, LicenceConditionSearch(nomsId = listOf(nomsId))))
        .willReturn(Mono.fromCallable { licenceMatchedResponse(licenceId, crn) })
      given(cvlApiClient.getLicenceById(crn, licenceId))
        .willReturn(Mono.fromCallable { licenceByIdResponse() })

      val response = licenceConditionsService.getLicenceConditionsCvl(crn)

      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(cvlApiClient).should().getLicenceMatch(crn, LicenceConditionSearch(nomsId = listOf(nomsId)))
      then(cvlApiClient).should().getLicenceById(crn, licenceId)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsCvlResponse(
            null,
            expectedPersonDetailsResponse(),
            expectedCvlLicenceConditionsResponse()
          )
        )
      )
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
      middleNames = "",
      croNumber = "123456/04A",
      mostRecentPrisonerNumber = "G12345",
      nomsNumber = "A1234CR",
      pncNumber = "2004/0712343H"
    )
  }

  private fun expectedOffenceWithLicenceConditionsResponse(licenceConditions: LicenceConditions?): List<ConvictionResponse> {
    return listOf(
      ConvictionResponse(
        convictionId = 2500614567,
        active = true,
        offences = listOf(
          uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence(
            mainOffence = true,
            description = "Robbery (other than armed robbery)",
            code = "ABC123",
            offenceDate = LocalDate.parse("2022-08-26")
          ),
          uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence(
            mainOffence = false,
            description = "Arson",
            code = "ZYX789",
            offenceDate = LocalDate.parse("2022-08-26")
          )
        ),
        sentenceDescription = "CJA - Extended Sentence",
        sentenceOriginalLength = 6,
        sentenceOriginalLengthUnits = "Days",
        sentenceSecondLength = 10,
        sentenceSecondLengthUnits = "Months",
        sentenceStartDate = LocalDate.parse("2022-04-26"),
        sentenceExpiryDate = LocalDate.parse("2022-06-10"),
        licenceExpiryDate = LocalDate.parse("2022-05-10"),
        postSentenceSupervisionEndDate = LocalDate.parse("2022-05-11"),
        statusCode = "ABC123",
        statusDescription = "custody status",
        isCustodial = true,
        licenceConditions = licenceConditions?.licenceConditions,
        licenceDocuments = listOf(
          CaseDocument(
            id = "374136ce-f863-48d8-96dc-7581636e461e",
            documentName = "GKlicencejune2022.pdf",
            author = "Tom Thumb",
            type = CaseDocumentType(code = "CONVICTION_DOCUMENT", description = "Sentence related"),
            extendedDescription = null,
            lastModifiedAt = "2022-06-07T17:00:29.493",
            createdAt = "2022-06-07T17:00:29",
            parentPrimaryKeyId = 2500614567L
          ),
          CaseDocument(
            id = "374136ce-f863-48d8-96dc-7581636e123e",
            documentName = "TDlicencejuly2022.pdf",
            author = "Wendy Rose",
            type = CaseDocumentType(code = "CONVICTION_DOCUMENT", description = "Sentence related"),
            extendedDescription = null,
            lastModifiedAt = "2022-07-08T10:00:29.493",
            createdAt = "2022-06-08T10:00:29",
            parentPrimaryKeyId = 2500614567L
          ),
        )
      )
    )
  }

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

  private fun expectedCvlLicenceConditionsResponse(): List<LicenceConditionResponse> {

    return listOf(
      LicenceConditionResponse(
        conditionalReleaseDate = LocalDate.parse("2022-06-10"),
        actualReleaseDate = LocalDate.parse("2022-06-11"),
        sentenceStartDate = LocalDate.parse("2022-06-12"),
        sentenceEndDate = LocalDate.parse("2022-06-13"),
        licenceStartDate = LocalDate.parse("2022-06-14"),
        licenceExpiryDate = LocalDate.parse("2022-06-15"),
        topupSupervisionStartDate = LocalDate.parse("2022-06-16"),
        topupSupervisionExpiryDate = LocalDate.parse("2022-06-17"),
        standardLicenceConditions = listOf(LicenceConditionDetail(text = "This is a standard licence condition")),
        standardPssConditions = listOf(LicenceConditionDetail(text = "This is a standard PSS licence condition")),
        additionalLicenceConditions = listOf(
          LicenceConditionDetail(
            text = "This is an additional licence condition",
            expandedText = "Expanded additional licence condition"
          )
        ),
        additionalPssConditions = listOf(
          LicenceConditionDetail(
            text = "This is an additional PSS licence condition",
            expandedText = "Expanded additional PSS licence condition"
          )
        ),
        bespokeConditions = listOf(LicenceConditionDetail(text = "This is a bespoke condition"))
      )
    )
  }
}
