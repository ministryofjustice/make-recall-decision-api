package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
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
internal class ConvictionServiceTest : ServiceTestBase() {

  @Test
  fun `given an active conviction and licence conditions with documents then return these details in the response`() {
    runTest {
      given(communityApiClient.getActiveConvictions(anyString(), anyBoolean()))
        .willReturn(Mono.fromCallable { listOf(custodialConvictionResponse()) })
      given(communityApiClient.getLicenceConditionsByConvictionId(anyString(), anyLong()))
        .willReturn(Mono.fromCallable { licenceConditions })
      given(communityApiClient.getGroupedDocuments(anyString()))
        .willReturn(Mono.fromCallable { groupedDocumentsResponse() })

      val response = convictionService.buildConvictionResponse(crn, true)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getLicenceConditionsByConvictionId(crn, 2500614567)
      then(communityApiClient).should().getGroupedDocuments(crn)
      then(communityApiClient).shouldHaveNoMoreInteractions()

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          expectedOffenceWithLicenceConditionsResponse(licenceConditions, true),
        )
      )
    }
  }

  @Test
  fun `given an active conviction and licence conditions with no documents required then return these details in the response`() {
    runTest {
      given(communityApiClient.getActiveConvictions(anyString(), anyBoolean()))
        .willReturn(Mono.fromCallable { listOf(custodialConvictionResponse()) })
      given(communityApiClient.getLicenceConditionsByConvictionId(anyString(), anyLong()))
        .willReturn(Mono.fromCallable { licenceConditions })

      val response = convictionService.buildConvictionResponse(crn, false)

      then(communityApiClient).should().getActiveConvictions(crn)
      then(communityApiClient).should().getLicenceConditionsByConvictionId(crn, 2500614567)
      then(communityApiClient).shouldHaveNoMoreInteractions()

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          expectedOffenceWithLicenceConditionsResponse(licenceConditions, false),
        )
      )
    }
  }

  private fun expectedOffenceWithLicenceConditionsResponse(licenceConditions: LicenceConditions?, showDocuments: Boolean): List<ConvictionResponse> {
    val documents = if (showDocuments) listOf(
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
    ) else null

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
        licenceDocuments = documents
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
}
