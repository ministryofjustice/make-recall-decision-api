package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditionOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.StandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UnderIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.YesNoNotApplicableOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import java.time.LocalDate

@ExperimentalCoroutinesApi
class RecommendationToPartADataMapperTest {

  private lateinit var recommendationToPartADataMapper: RecommendationToPartADataMapper

  @BeforeEach
  fun setup() {
    recommendationToPartADataMapper = RecommendationToPartADataMapper()
  }

  @ParameterizedTest(name = "given custody status {0} in recommendation data should map to the part A text {1}")
  @CsvSource("YES_POLICE,Police Custody", "YES_PRISON,Prison Custody", "NO,No")
  fun `given custody status in recommendation data then should map to the part A text`(custodyValue: CustodyStatusValue, partADisplayText: String) {
    runTest {
      val recommendation = RecommendationEntity(id = 1, data = RecommendationModel(crn = "ABC123", custodyStatus = CustodyStatus(selected = custodyValue, details = "Details", allOptions = null)))

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.custodyStatus?.value).isEqualTo(partADisplayText)
      assertThat(result.custodyStatus?.details).isEqualTo("Details")
    }
  }

  @ParameterizedTest(name = "given recall type {0} in recommendation data should map to the part A text {1}")
  @CsvSource("STANDARD,Standard", "FIXED_TERM,Fixed", "NO_RECALL,")
  fun `given recall type in recommendation data then should map to the part A text`(recallValue: RecallTypeValue, partADisplayText: String?) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", recallType = RecallType(selected = RecallTypeSelectedValue(value = recallValue, details = "My details"), allOptions = null))
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.recallType?.value).isEqualTo(partADisplayText)
      assertThat(result.recallType?.details).isEqualTo("My details")
    }
  }

  @ParameterizedTest(name = "given is emergency recall field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("true,Yes", "false,No", "null,No")
  fun `given is emergency recall data then should map to the part A text`(isThisAnEmergencyRecall: Boolean, partADisplayText: String?) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", isThisAnEmergencyRecall = isThisAnEmergencyRecall)
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.isThisAnEmergencyRecall).isEqualTo(partADisplayText)
    }
  }

  @ParameterizedTest(name = "given is emergency recall field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("YES,Yes", "NO,No", "NOT_APPLICABLE,N/A")
  fun `given victims in contact scheme data then should map to the part A text`(victimsInContactScheme: YesNoNotApplicableOptions, partADisplayText: String?) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", hasVictimsInContactScheme = VictimsInContactScheme(selected = victimsInContactScheme))
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

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

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

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

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.dateVloInformed).isEqualTo("")
    }
  }

  @Test
  fun `given has arrest issues data then should map in part A text`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", hasArrestIssues = SelectedWithDetails(selected = true, details = "Arrest details"))
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

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

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.hasArrestIssues?.value).isEqualTo("")
    }
  }

  @Test
  fun `given has contraband risk data then should map in part A text`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", hasContrabandRisk = SelectedWithDetails(selected = true, details = "Contraband risk details"))
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

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

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      val expectedResult = StringBuilder().append("I am a title").append(System.lineSeparator()).append("details1").append(System.lineSeparator()).append("Note: note1")
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

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      val expectedResult = StringBuilder().append("I am a title").append(System.lineSeparator()).append("details1").append(System.lineSeparator()).append("Note: note1").append(System.lineSeparator())
        .append(System.lineSeparator())
        .append("I am another title").append(System.lineSeparator()).append("details2").append(System.lineSeparator()).append("Note: note2")

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

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      val expectedResult = StringBuilder().append("I am a title").append(System.lineSeparator()).append("details1").append(System.lineSeparator()).append(System.lineSeparator())
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
            allOptions = listOf(TextValueOption(value = "YES", text = "Yes"), TextValueOption(value = "NO", text = "No"), TextValueOption(value = "NOT_APPLICABLE", text = "N/A"))
          )
        )
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

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

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

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

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

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

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

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
        assertThat(result.custodialTerm).isEqualTo("")
        assertThat(result.extendedTerm).isEqualTo("")
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
            lengthOfSentence = 6,
            lengthOfSentenceUnits = "days",
            sentenceDescription = "Extended Determinate Sentence",
            licenceExpiryDate = null,
            sentenceExpiryDate = null,
            sentenceSecondLength = 20,
            sentenceSecondLengthUnits = "days"
          )
        )
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.indexOffenceDescription).isEqualTo("Armed robbery")
      assertThat(result.dateOfOriginalOffence).isEqualTo(EMPTY_STRING)
      assertThat(result.dateOfSentence).isEqualTo(EMPTY_STRING)
      assertThat(result.lengthOfSentence).isEqualTo("6 days")
      assertThat(result.licenceExpiryDate).isEqualTo(EMPTY_STRING)
      assertThat(result.sentenceExpiryDate).isEqualTo(EMPTY_STRING)
      assertThat(result.custodialTerm).isEqualTo("6 days")
      assertThat(result.extendedTerm).isEqualTo("20 days")
    }
  }
}
