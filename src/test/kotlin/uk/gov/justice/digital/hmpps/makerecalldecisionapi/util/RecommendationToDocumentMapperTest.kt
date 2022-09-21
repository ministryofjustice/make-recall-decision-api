package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditionOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceTypeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.StandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UnderIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecallValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.YesNoNotApplicableOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.WHITE_SPACE
import java.time.LocalDate

@ExperimentalCoroutinesApi
class RecommendationToDocumentMapperTest {

  private lateinit var recommendationToDocumentMapper: RecommendationToDocumentMapper

  @BeforeEach
  fun setup() {
    recommendationToDocumentMapper = RecommendationToDocumentMapper()
  }

  @ParameterizedTest(name = "given why considered recall {0} in recommendation data should map to the DNTR text {1}")
  @CsvSource("RISK_INCREASED,Your risk is assessed as increased", "CONTACT_STOPPED,Contact with your probation practitioner has broken down", "RISK_INCREASED_AND_CONTACT_STOPPED,Your risk is assessed as increased and contact with your probation practitioner has broken down")
  fun `given why considered recall in recommendation data then should map to the DNTR text`(
    whyConsideredRecallValue: WhyConsideredRecallValue,
    dntrDisplayText: String
  ) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          whyConsideredRecall = WhyConsideredRecall(
            selected = whyConsideredRecallValue,
            allOptions = listOf(
              TextValueOption(value = "RISK_INCREASED", text = "Your risk is assessed as increased"),
              TextValueOption(value = "CONTACT_STOPPED", text = "Contact with your probation practitioner has broken down"),
              TextValueOption(value = "RISK_INCREASED_AND_CONTACT_STOPPED", text = "Your risk is assessed as increased and contact with your probation practitioner has broken down")
            )
          )
        )
      )
      val result = RecommendationToDocumentMapper.mapRecommendationDataToDntrDocumentData(recommendation)
      assertThat(result.whyConsideredRecall).isEqualTo(dntrDisplayText)
    }
  }

  @ParameterizedTest(name = "given custody status {0} in recommendation data should map to the part A text {1}")
  @CsvSource("YES_POLICE,Police Custody", "YES_PRISON,Prison Custody", "NO,No")
  fun `given custody status in recommendation data then should map to the part A text`(
    custodyValue: CustodyStatusValue,
    partADisplayText: String
  ) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          custodyStatus = CustodyStatus(selected = custodyValue, details = "Details", allOptions = null)
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.custodyStatus?.value).isEqualTo(partADisplayText)
      assertThat(result.custodyStatus?.details).isEqualTo("Details")
    }
  }

  @ParameterizedTest(name = "given recall type {0}, recall details {1}, fixed term additional licence conditions selected {2} with value {3}, indeterminate sentence {4}, extended sentence {5}, should map in the part A with the recall value {6} and details {7} with additional licence conditions value {8}")
  @CsvSource(
    "STANDARD,Standard details,false,,false,false,Standard,Standard details,N/A (standard recall)",
    "FIXED_TERM,Fixed details,true,Fixed term additional licence conditions,false,false,Fixed,Fixed details,Fixed term additional licence conditions",
    "FIXED_TERM,Fixed details,false,Fixed term additional licence conditions,false,false,Fixed,Fixed details,''",
    "STANDARD,Standard details,false,,true,true,N/A (not a determinate recall),N/A (not a determinate recall),N/A (not a determinate recall)",
    "STANDARD,Standard details,false,,true,false,N/A (not a determinate recall),N/A (not a determinate recall),N/A (not a determinate recall)",
    "STANDARD,Standard details,false,,false,true,N/A (extended sentence recall),N/A (extended sentence recall),N/A (extended sentence recall)"
  )
  fun `given recall type and whether indeterminate sentence in recommendation data then should map to the part A text`(
    recallValue: RecallTypeValue,
    recallTypeDetails: String?,
    fixedTermAdditionalLicenceConditionsSelected: Boolean,
    fixedTermAdditionalLicenceConditionsValue: String?,
    isIndeterminateSentence: Boolean,
    isExtendedSentence: Boolean,
    partARecallTypeDisplayValue: String?,
    partARecallTypeDisplayDetails: String?,
    partAFixedTermLicenceConditionsDisplayValue: String?,
  ) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          recallType = RecallType(
            selected = RecallTypeSelectedValue(value = recallValue, details = recallTypeDetails),
            allOptions = null
          ),
          isIndeterminateSentence = isIndeterminateSentence,
          isExtendedSentence = isExtendedSentence,
          fixedTermAdditionalLicenceConditions = SelectedWithDetails(fixedTermAdditionalLicenceConditionsSelected, fixedTermAdditionalLicenceConditionsValue),
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.recallType?.value).isEqualTo(partARecallTypeDisplayValue)
      assertThat(result.recallType?.details).isEqualTo(partARecallTypeDisplayDetails)
      assertThat(result.fixedTermAdditionalLicenceConditions).isEqualTo(partAFixedTermLicenceConditionsDisplayValue)
    }
  }

  @ParameterizedTest(name = "given is emergency recall field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("true,Yes", "false,No", "null,''", nullValues = ["null"])
  fun `given is emergency recall data then should map to the part A text`(
    isThisAnEmergencyRecall: Boolean?,
    partADisplayText: String?
  ) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", isThisAnEmergencyRecall = isThisAnEmergencyRecall)
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.isThisAnEmergencyRecall).isEqualTo(partADisplayText)
    }
  }

  @ParameterizedTest(name = "given is an extended sentence {0} in recommendation data should map to the part A text {1}")
  @CsvSource("true,Yes", "false,No", "null,''", nullValues = ["null"])
  fun `given is extended sentence data then should map to the part A text`(
    isExtendedSentence: Boolean?,
    partADisplayText: String?
  ) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", isExtendedSentence = isExtendedSentence)
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.isExtendedSentence).isEqualTo(partADisplayText)
    }
  }

  @ParameterizedTest(name = "given is victim contact scheme field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("YES,Yes", "NO,No", "NOT_APPLICABLE,N/A")
  fun `given victims in contact scheme data then should map to the part A text`(
    victimsInContactScheme: YesNoNotApplicableOptions,
    partADisplayText: String?
  ) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          hasVictimsInContactScheme = VictimsInContactScheme(selected = victimsInContactScheme)
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.hasVictimsInContactScheme).isEqualTo(partADisplayText)
    }
  }

  @Test
  fun `given date vlo informed then should map to readable date in part A text`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", dateVloInformed = LocalDate.parse("2022-09-01"))
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.dateVloInformed).isEqualTo("1 September 2022")
    }
  }

  @Test
  fun `given null date vlo informed then should map to empty string in part A text`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", dateVloInformed = null)
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.dateVloInformed).isEqualTo("")
    }
  }

  @ParameterizedTest(name = "given is indeterminate sentence type field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("LIFE,Yes - Lifer", "IPP,Yes - IPP", "DPP,Yes - DPP", "NO,No")
  fun `given  indeterminate sentence type data then should map to the part A text`(
    indeterminateSentenceType: IndeterminateSentenceTypeOptions,
    partADisplayText: String?
  ) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          indeterminateSentenceType = IndeterminateSentenceType(selected = indeterminateSentenceType)
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.indeterminateSentenceType).isEqualTo(partADisplayText)
    }
  }

  @Test
  fun `given has arrest issues data then should map in part A text`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          hasArrestIssues = SelectedWithDetails(selected = true, details = "Arrest details")
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.hasArrestIssues?.value).isEqualTo("Yes")
      assertThat(result.hasArrestIssues?.details).isEqualTo("Arrest details")
    }
  }

  @Test
  fun `given has no arrest issues data then should map in part A text`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", hasArrestIssues = SelectedWithDetails(selected = null))
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.hasArrestIssues?.value).isEqualTo("")
    }
  }

  @Test
  fun `given has contraband risk data then should map in part A text`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          hasContrabandRisk = SelectedWithDetails(selected = true, details = "Contraband risk details")
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.hasContrabandRisk?.value).isEqualTo("Yes")
      assertThat(result.hasContrabandRisk?.details).isEqualTo("Contraband risk details")
    }
  }

  @Test
  fun `given alternative licence conditions then build up the text for alternative licences in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          licenceConditionsBreached = LicenceConditionsBreached(
            additionalLicenceConditions = AdditionalLicenceConditions(
              selected = listOf("NST14"),
              allOptions = listOf(
                AdditionalLicenceConditionOption(
                  subCatCode = "NST14",
                  mainCatCode = "NLC5",
                  title = "I am a title", details = "details1", note = "note1"
                )
              )
            ),
            standardLicenceConditions = null
          )
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      val expectedResult = StringBuilder().append("I am a title").append(System.lineSeparator()).append("details1")
        .append(System.lineSeparator()).append("Note: note1")
      assertThat(result.additionalConditionsBreached).isEqualTo(expectedResult.toString())
    }
  }

  @Test
  fun `given multiple alternative licence conditions then build up the text with line breaks for alternative licences in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          licenceConditionsBreached = LicenceConditionsBreached(
            additionalLicenceConditions = AdditionalLicenceConditions(
              selected = listOf("NST14", "XYZ"),
              allOptions = listOf(
                AdditionalLicenceConditionOption(
                  subCatCode = "NST14",
                  mainCatCode = "NLC5",
                  title = "I am a title", details = "details1", note = "note1"
                ),
                AdditionalLicenceConditionOption(
                  subCatCode = "XYZ",
                  mainCatCode = "ABC",
                  title = "I am another title", details = "details2", note = "note2"
                )
              )
            ),
            standardLicenceConditions = null
          )
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      val expectedResult = StringBuilder().append("I am a title").append(System.lineSeparator()).append("details1")
        .append(System.lineSeparator()).append("Note: note1").append(System.lineSeparator())
        .append(System.lineSeparator())
        .append("I am another title").append(System.lineSeparator()).append("details2").append(System.lineSeparator())
        .append("Note: note2")

      assertThat(result.additionalConditionsBreached).isEqualTo(expectedResult.toString())
    }
  }

  @Test
  fun `given multiple alternative licence conditions with no note then build up the text with line breaks and no notes for alternative licences in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          licenceConditionsBreached = LicenceConditionsBreached(
            additionalLicenceConditions = AdditionalLicenceConditions(
              selected = listOf("NST14", "XYZ"),
              allOptions = listOf(
                AdditionalLicenceConditionOption(
                  subCatCode = "NST14",
                  mainCatCode = "NLC5",
                  title = "I am a title", details = "details1"
                ),
                AdditionalLicenceConditionOption(
                  subCatCode = "XYZ",
                  mainCatCode = "ABC",
                  title = "I am another title", details = "details2"
                )
              )
            ),
            standardLicenceConditions = null
          )
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      val expectedResult = StringBuilder().append("I am a title").append(System.lineSeparator()).append("details1")
        .append(System.lineSeparator()).append(System.lineSeparator())
        .append("I am another title").append(System.lineSeparator()).append("details2")

      assertThat(result.additionalConditionsBreached).isEqualTo(expectedResult.toString())
    }
  }

  @Test
  fun `given has is under integrated offender management then should map in part A text`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          underIntegratedOffenderManagement = UnderIntegratedOffenderManagement(
            selected = "YES",
            allOptions = listOf(
              TextValueOption(value = "YES", text = "Yes"),
              TextValueOption(value = "NO", text = "No"),
              TextValueOption(value = "NOT_APPLICABLE", text = "N/A")
            )
          )
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.isUnderIntegratedOffenderManagement).isEqualTo("YES")
    }
  }

  @Test
  fun `given no alternative licence conditions then return empty text for alternative licences in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          licenceConditionsBreached = LicenceConditionsBreached(
            additionalLicenceConditions = null, standardLicenceConditions = null
          )
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.additionalConditionsBreached).isEqualTo(EMPTY_STRING)
    }
  }

  @Test
  fun `given selected standard licence conditions then return this for standard licences in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          licenceConditionsBreached = LicenceConditionsBreached(
            additionalLicenceConditions = null,
            standardLicenceConditions = StandardLicenceConditions(
              selected = listOf(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name)
            )
          )
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.selectedStandardConditionsBreached?.get(0)).isEqualTo(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name)
    }
  }

  @ParameterizedTest()
  @CsvSource("Extended Determinate Sentence", "CJA - Extended Sentence", "Random sentence description")
  fun `given conviction details then convert dates and sentence lengths in the part A`(sentenceDescription: String) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          convictionDetail = ConvictionDetail(
            indexOffenceDescription = "Armed robbery",
            dateOfOriginalOffence = LocalDate.parse("2022-09-01"),
            dateOfSentence = LocalDate.parse("2022-09-04"),
            lengthOfSentence = 6,
            lengthOfSentenceUnits = "days",
            sentenceDescription = sentenceDescription,
            licenceExpiryDate = LocalDate.parse("2022-09-05"),
            sentenceExpiryDate = LocalDate.parse("2022-09-06"),
            sentenceSecondLength = 20,
            sentenceSecondLengthUnits = "days"
          )
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.indexOffenceDescription).isEqualTo("Armed robbery")
      assertThat(result.dateOfOriginalOffence).isEqualTo("01/09/2022")
      assertThat(result.dateOfSentence).isEqualTo("04/09/2022")
      assertThat(result.lengthOfSentence).isEqualTo("6 days")
      assertThat(result.licenceExpiryDate).isEqualTo("05/09/2022")
      assertThat(result.sentenceExpiryDate).isEqualTo("06/09/2022")

      if (sentenceDescription != "Random sentence description") {
        assertThat(result.custodialTerm).isEqualTo("6 days")
        assertThat(result.extendedTerm).isEqualTo("20 days")
      } else {
        assertThat(result.custodialTerm).isNull()
        assertThat(result.extendedTerm).isNull()
      }
    }
  }

  @Test
  fun `given conviction details with empty dates then set empty dates in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          convictionDetail = ConvictionDetail(
            indexOffenceDescription = "Armed robbery",
            dateOfOriginalOffence = null,
            dateOfSentence = null,
            lengthOfSentence = null,
            lengthOfSentenceUnits = null,
            sentenceDescription = "Extended Determinate Sentence",
            licenceExpiryDate = null,
            sentenceExpiryDate = null,
            sentenceSecondLength = null,
            sentenceSecondLengthUnits = null
          )
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.indexOffenceDescription).isEqualTo("Armed robbery")
      assertThat(result.dateOfOriginalOffence).isEqualTo(EMPTY_STRING)
      assertThat(result.dateOfSentence).isEqualTo(EMPTY_STRING)
      assertThat(result.lengthOfSentence).isEqualTo(WHITE_SPACE)
      assertThat(result.licenceExpiryDate).isEqualTo(EMPTY_STRING)
      assertThat(result.sentenceExpiryDate).isEqualTo(EMPTY_STRING)
      assertThat(result.custodialTerm).isEqualTo(WHITE_SPACE)
      assertThat(result.extendedTerm).isEqualTo(WHITE_SPACE)
    }
  }

  @Test
  fun `given empty conviction details then set empty conviction in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          convictionDetail = null
        )
      )

      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.indexOffenceDescription).isNull()
      assertThat(result.dateOfOriginalOffence).isEqualTo(EMPTY_STRING)
      assertThat(result.dateOfSentence).isEqualTo(EMPTY_STRING)
      assertThat(result.lengthOfSentence).isNull()
      assertThat(result.licenceExpiryDate).isEqualTo(EMPTY_STRING)
      assertThat(result.sentenceExpiryDate).isEqualTo(EMPTY_STRING)
      assertThat(result.custodialTerm).isNull()
      assertThat(result.extendedTerm).isNull()
    }
  }

  @Test
  fun `given main address details then set address details in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          personOnProbation = PersonOnProbation(
            addresses = listOf(
              Address(
                line1 = "Line 1 address",
                line2 = "Line 2 address",
                town = "Address town",
                postcode = "TS1 1ST",
                noFixedAbode = false
              )
            )
          )
        )
      )
      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.lastRecordedAddress).isEqualTo("Line 1 address, Line 2 address, Address town, TS1 1ST")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given address is no fixed abode then clear address in the part A and set no fixed abode field`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          personOnProbation = PersonOnProbation(
            addresses = listOf(
              Address(
                line1 = "Line 1 address",
                line2 = "Line 2 address",
                town = "Address town",
                postcode = "TS1 1ST",
                noFixedAbode = true
              )
            )
          )
        )
      )
      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.lastRecordedAddress).isEqualTo("")
      assertThat(result.noFixedAbode).isEqualTo("No fixed abode")
    }
  }

  @Test
  fun `given multiple main address details then set address details in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          personOnProbation = PersonOnProbation(
            addresses = listOf(
              Address(
                line1 = "Line 1 address",
                line2 = "Line 2 address",
                town = "Address town",
                postcode = "TS1 1ST",
                noFixedAbode = false
              ),
              Address(
                line1 = "Line 1 second address",
                line2 = "Line 2 second address",
                town = "Address second town",
                postcode = "TS1 2ST",
                noFixedAbode = false
              )
            )
          )
        )
      )
      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.lastRecordedAddress).isEqualTo("Line 1 address, Line 2 address, Address town, TS1 1ST\nLine 1 second address, Line 2 second address, Address second town, TS1 2ST")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given multiple main address details with contradicting no fixed abode values then clear the address details in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          personOnProbation = PersonOnProbation(
            addresses = listOf(
              Address(
                line1 = "Line 1 address",
                line2 = "Line 2 address",
                town = "Address town",
                postcode = "TS1 1ST",
                noFixedAbode = true
              ),
              Address(
                line1 = "Line 1 second address",
                line2 = "Line 2 second address",
                town = "Address second town",
                postcode = "TS1 2ST",
                noFixedAbode = false
              )
            )
          )
        )
      )
      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.lastRecordedAddress).isEqualTo("")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given empty address details then clear the address details in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          personOnProbation = PersonOnProbation()
        )
      )
      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.lastRecordedAddress).isEqualTo("")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given main address with all empty lines then clear the address details in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          personOnProbation = PersonOnProbation(
            addresses = listOf(
              Address(
                line1 = "",
                line2 = "",
                town = "",
                postcode = "",
                noFixedAbode = false
              )
            )
          )
        )
      )
      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.lastRecordedAddress).isEqualTo("")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given main address with some empty lines then correctly format the address in the part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          personOnProbation = PersonOnProbation(
            addresses = listOf(
              Address(
                line1 = "Line 1 address",
                line2 = "",
                town = "Address town",
                postcode = "TS1 1ST",
                noFixedAbode = false
              )
            )
          )
        )
      )
      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.lastRecordedAddress).isEqualTo("Line 1 address, Address town, TS1 1ST")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given other possible address field is populated then format it for the Part A`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          mainAddressWherePersonCanBeFound = SelectedWithDetails(
            selected = false,
            details = "123 Acacia Avenue, Birmingham, B23 1AV"
          ),
        )
      )
      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.otherPossibleAddresses).isEqualTo("Police can find this person at: 123 Acacia Avenue, Birmingham, B23 1AV")
    }
  }

  @ParameterizedTest(name = "given empty adress value is {0} then leave this field empty in the Part A")
  @CsvSource("''", "null", nullValues = ["null"])
  fun `given empty other possible address field then leave this field empty in the Part A`(addressValue: String?) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = "ABC123",
          mainAddressWherePersonCanBeFound = SelectedWithDetails(selected = true, details = addressValue),
        )
      )
      val result = RecommendationToDocumentMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.otherPossibleAddresses).isEqualTo(null)
    }
  }
}
