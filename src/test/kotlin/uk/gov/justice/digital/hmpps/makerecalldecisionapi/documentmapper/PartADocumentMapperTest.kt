package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditionOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CvlLicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceTypeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionSection
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PractitionerForPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RoshData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RoshDataScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SentenceGroup
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.StandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UnderIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhoCompletedPartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.YesNoNotApplicableOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.toPersonOnProbationDto
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RegionService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationMetaData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.WHITE_SPACE
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExperimentalCoroutinesApi
@ExtendWith(MockitoExtension::class)
class PartADocumentMapperTest {

  private lateinit var partADocumentMapper: PartADocumentMapper

  @Mock
  private lateinit var regionService: RegionService

  @BeforeEach
  fun setup() {
    partADocumentMapper = PartADocumentMapper(regionService)
  }

  @ParameterizedTest(name = "given custody status {0} in recommendation data should map to the part A text {1}")
  @CsvSource("YES_POLICE,Police Custody", "YES_PRISON,Prison Custody", "NO,No")
  fun `given custody status in recommendation data then should map to the part A text`(
    custodyValue: CustodyStatusValue,
    partADisplayText: String,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        custodyStatus = CustodyStatus(selected = custodyValue, details = "Details", allOptions = null),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.custodyStatus?.value).isEqualTo(partADisplayText)
      assertThat(result.custodyStatus?.details).isEqualTo("Details")
    }
  }

  @ParameterizedTest(name = "given recall type {0}, recall details {1}, fixed term additional licence conditions selected {2} with value {3}, indeterminate sentence {4}, extended sentence {5}, sentence group {6} should map in the part A with the recall value {7} and details {8} with additional licence conditions value {9}")
  @CsvSource(
    "STANDARD,Standard details,false,,false,false,,Standard,Standard details,N/A (standard recall)",
    "STANDARD,Standard details,false,,,,ADULT_SDS,Standard,Standard details,N/A (standard recall)",
    "FIXED_TERM,Fixed details,true,Fixed term additional licence conditions,false,false,,Fixed,Fixed details,Fixed term additional licence conditions",
    "FIXED_TERM,Fixed details,true,Fixed term additional licence conditions,,,ADULT_SDS,Fixed,Fixed details,Fixed term additional licence conditions",
    "FIXED_TERM,Fixed details,false,Fixed term additional licence conditions,false,false,,Fixed,Fixed details,''",
    "FIXED_TERM,Fixed details,false,Fixed term additional licence conditions,,,ADULT_SDS,Fixed,Fixed details,''",
    "STANDARD,Standard details,false,,true,true,,N/A (not a determinate recall),N/A (not a determinate recall),N/A (not a determinate recall)",
    "STANDARD,Standard details,false,,,,INDETERMINATE,N/A (not a determinate recall),N/A (not a determinate recall),N/A (not a determinate recall)",
    "STANDARD,Standard details,false,,true,false,,N/A (not a determinate recall),N/A (not a determinate recall),N/A (not a determinate recall)",
    "STANDARD,Standard details,false,,,,INDETERMINATE,N/A (not a determinate recall),N/A (not a determinate recall),N/A (not a determinate recall)",
    "STANDARD,Standard details,false,,false,true,,N/A (extended sentence recall),N/A (extended sentence recall),N/A (extended sentence recall)",
    "STANDARD,Standard details,false,,,,EXTENDED,N/A (extended sentence recall),N/A (extended sentence recall),N/A (extended sentence recall)",
  )
  fun `given recall type and whether indeterminate sentence in recommendation data then should map to the part A text`(
    recallValue: RecallTypeValue,
    recallTypeDetails: String?,
    fixedTermAdditionalLicenceConditionsSelected: Boolean,
    fixedTermAdditionalLicenceConditionsValue: String?,
    isIndeterminateSentence: Boolean?,
    isExtendedSentence: Boolean?,
    sentenceGroup: SentenceGroup?,
    partARecallTypeDisplayValue: String?,
    partARecallTypeDisplayDetails: String?,
    partAFixedTermLicenceConditionsDisplayValue: String?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        recallType = RecallType(
          selected = RecallTypeSelectedValue(value = recallValue, details = recallTypeDetails),
          allOptions = null,
        ),
        isIndeterminateSentence = isIndeterminateSentence,
        isExtendedSentence = isExtendedSentence,
        sentenceGroup = sentenceGroup,
        fixedTermAdditionalLicenceConditions = SelectedWithDetails(
          fixedTermAdditionalLicenceConditionsSelected,
          fixedTermAdditionalLicenceConditionsValue,
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.recallType?.value).isEqualTo(partARecallTypeDisplayValue)
      assertThat(result.recallType?.details).isEqualTo(partARecallTypeDisplayDetails)
      assertThat(result.fixedTermAdditionalLicenceConditions).isEqualTo(partAFixedTermLicenceConditionsDisplayValue)
    }
  }

  @ParameterizedTest(name = "given is emergency recall field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("true,Yes", "false,No", "null,''", nullValues = ["null"])
  fun `given is emergency recall data then should map to the part A text`(
    isThisAnEmergencyRecall: Boolean?,
    partADisplayText: String?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        isThisAnEmergencyRecall = isThisAnEmergencyRecall,
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.isThisAnEmergencyRecall).isEqualTo(partADisplayText)
    }
  }

  @ParameterizedTest
  @EnumSource(SentenceGroup::class)
  fun `set isServingYouthSentence appropriately based on sentenceGroup value`(
    sentenceGroup: SentenceGroup?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        sentenceGroup = sentenceGroup,
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.isServingYouthSentence).isEqualTo(if (sentenceGroup == SentenceGroup.YOUTH_SDS) "Yes" else EMPTY_STRING)
    }
  }

  @ParameterizedTest(name = "given extended sentence value (isExtendedSentence={0}, sentenceGroup={1}), recommendation data should map to the part A text {2}")
  @CsvSource(
    "true, null, Yes",
    ", EXTENDED, Yes",
    "false, null, No",
    ", ADULT_SDS, No",
    "null, null,''",
    "null, null,''",
    nullValues = ["null"],
  )
  fun `given is extended sentence data then should map to the part A text`(
    isExtendedSentence: Boolean?,
    sentenceGroup: SentenceGroup?,
    partADisplayText: String?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        isExtendedSentence = isExtendedSentence,
        sentenceGroup = sentenceGroup,
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.isExtendedSentence).isEqualTo(partADisplayText)
    }
  }

  @ParameterizedTest(name = "given is victim contact scheme field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("YES,Yes", "NO,No", "NOT_APPLICABLE,N/A")
  fun `given victims in contact scheme data then should map to the part A text`(
    victimsInContactScheme: YesNoNotApplicableOptions,
    partADisplayText: String?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        hasVictimsInContactScheme = VictimsInContactScheme(selected = victimsInContactScheme),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.hasVictimsInContactScheme).isEqualTo(partADisplayText)
    }
  }

  @Test
  fun `given date vlo informed then should map to readable date in part A text`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        dateVloInformed = LocalDate.parse("2022-09-01"),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.dateVloInformed).isEqualTo("1 September 2022")
    }
  }

  @ParameterizedTest(name = "extended sentence (isExtendedSentence={0}, sentenceGroup={1}) considered in part A text")
  @CsvSource("true,", ", EXTENDED")
  fun `given extended sentence used in part A text`(isExtendedSentence: Boolean?, sentenceGroup: SentenceGroup?) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        dateVloInformed = LocalDate.parse("2022-09-01"),
        isExtendedSentence = isExtendedSentence,
        sentenceGroup = sentenceGroup,
        isUnder18 = false,
        recallType = RecallType(selected = RecallTypeSelectedValue(RecallTypeValue.STANDARD)),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)
      assertThat(result.isUnder18).isEqualTo("N/A - indeterminate or extended sentence")
      assertThat(result.isMappaAboveLevel1).isEqualTo("N/A - indeterminate or extended sentence")
      assertThat(result.isSentence12MonthsOrOver).isEqualTo("N/A - indeterminate or extended sentence")
    }
  }

  @ParameterizedTest(name = "indeterminate sentence (isIndeterminateSentence={0}, sentenceGroup={1}) considered in part A text")
  @CsvSource("true,", ", INDETERMINATE")
  fun `given indeterminate sentence used in part A text`(
    isIndeterminateSentence: Boolean?,
    sentenceGroup: SentenceGroup?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        dateVloInformed = LocalDate.parse("2022-09-01"),
        isIndeterminateSentence = isIndeterminateSentence,
        sentenceGroup = sentenceGroup,
        isUnder18 = false,
        recallType = RecallType(selected = RecallTypeSelectedValue(RecallTypeValue.FIXED_TERM)),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)
      assertThat(result.isUnder18).isEqualTo("N/A - indeterminate or extended sentence")
      assertThat(result.isMappaAboveLevel1).isEqualTo("N/A - indeterminate or extended sentence")
      assertThat(result.isSentence12MonthsOrOver).isEqualTo("N/A - indeterminate or extended sentence")
      assertThat(result.isChargedWithSeriousOffence).isEqualTo("N/A - indeterminate or extended sentence")
    }
  }

  @ParameterizedTest(name = "non-indeterminate, non-extended sentence (isIndeterminateSentence={0}, isExtendedSentence={1}, sentenceGroup={2}) and standard recall considered in part A text")
  @CsvSource("false,false,", ",, ADULT_SDS", ",, YOUTH_SDS")
  fun `given not indeterminate and not extended sentence and standard recall used in part A text`(
    isIndeterminateSentence: Boolean?,
    isExtendedSentence: Boolean?,
    sentenceGroup: SentenceGroup?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        dateVloInformed = LocalDate.parse("2022-09-01"),
        isIndeterminateSentence = isIndeterminateSentence,
        isExtendedSentence = isExtendedSentence,
        sentenceGroup = sentenceGroup,
        isMappaLevelAbove1 = false,
        isUnder18 = false,
        isSentence12MonthsOrOver = false,
        isChargedWithOffence = false,
        isServingTerroristOrNationalSecurityOffence = false,
        isAtRiskOfInvolvedInForeignPowerThreat = false,
        wasReferredToParoleBoard244ZB = false,
        wasRepatriatedForMurder = false,
        isServingSOPCSentence = false,
        isServingDCRSentence = false,
        isYouthSentenceOver12Months = false,
        isYouthChargedWithSeriousOffence = false,
        recallType = RecallType(selected = RecallTypeSelectedValue(RecallTypeValue.STANDARD)),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)
      assertThat(result.isUnder18).isEqualTo("No")
      assertThat(result.isMappaAboveLevel1).isEqualTo("No")
      assertThat(result.isSentence12MonthsOrOver).isEqualTo("No")
      assertThat(result.isChargedWithOffence).isEqualTo("No")
      assertThat(result.isServingTerroristOrNationalSecurityOffence).isEqualTo("No")
      assertThat(result.isAtRiskOfInvolvedInForeignPowerThreat).isEqualTo("No")
      assertThat(result.wasReferredToParoleBoard244ZB).isEqualTo("No")
      assertThat(result.wasRepatriatedForMurder).isEqualTo("No")
      assertThat(result.isServingSOPCSentence).isEqualTo("No")
      assertThat(result.isServingDCRSentence).isEqualTo("No")
      assertThat(result.isYouthSentenceOver12Months).isEqualTo("No")
      assertThat(result.isYouthChargedWithSeriousOffence).isEqualTo("No")
    }
  }

  @ParameterizedTest(name = "non-indeterminate, non-extended sentence (isIndeterminateSentence={0}, isExtendedSentence={1}, sentenceGroup={2}) and fixed-term recall considered in part A text")
  @CsvSource("false,false,", ",, ADULT_SDS", ",, YOUTH_SDS")
  fun `given fixed term recall used in part A text`(
    isIndeterminateSentence: Boolean?,
    isExtendedSentence: Boolean?,
    sentenceGroup: SentenceGroup?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        dateVloInformed = LocalDate.parse("2022-09-01"),
        isIndeterminateSentence = isIndeterminateSentence,
        isExtendedSentence = isExtendedSentence,
        sentenceGroup = sentenceGroup,
        isUnder18 = true,
        isMappaLevelAbove1 = true,
        isSentence12MonthsOrOver = false,
        isChargedWithOffence = false,
        isServingTerroristOrNationalSecurityOffence = false,
        isAtRiskOfInvolvedInForeignPowerThreat = false,
        wasReferredToParoleBoard244ZB = false,
        wasRepatriatedForMurder = false,
        isServingSOPCSentence = false,
        isServingDCRSentence = false,
        isYouthSentenceOver12Months = false,
        isYouthChargedWithSeriousOffence = false,
        recallType = RecallType(selected = RecallTypeSelectedValue(RecallTypeValue.FIXED_TERM)),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)
      assertThat(result.isUnder18).isEqualTo("Yes")
      assertThat(result.isMappaAboveLevel1).isEqualTo("Yes")
      assertThat(result.isSentence12MonthsOrOver).isEqualTo("No")
      assertThat(result.isChargedWithOffence).isEqualTo("No")
      assertThat(result.isServingTerroristOrNationalSecurityOffence).isEqualTo("No")
      assertThat(result.isAtRiskOfInvolvedInForeignPowerThreat).isEqualTo("No")
      assertThat(result.wasReferredToParoleBoard244ZB).isEqualTo("No")
      assertThat(result.wasRepatriatedForMurder).isEqualTo("No")
      assertThat(result.isServingSOPCSentence).isEqualTo("No")
      assertThat(result.isServingDCRSentence).isEqualTo("No")
      assertThat(result.isYouthSentenceOver12Months).isEqualTo("No")
      assertThat(result.isYouthChargedWithSeriousOffence).isEqualTo("No")
    }
  }

  @ParameterizedTest(name = "isMappaLevel2or3AsYouthSdsUnder12Months set to {3} when sentence group is {0} and isMappaLevel2or3 is {1} and isYouthSentenceOver12Months is {2}")
  @CsvSource(
    "YOUTH_SDS, true, false, Yes",
    "YOUTH_SDS, true, true,",
    "YOUTH_SDS, true, ,",
    "YOUTH_SDS, false, false, No",
    "YOUTH_SDS, false, true,",
    "YOUTH_SDS, false, ,",
    "ADULT_SDS, true, false,",
    "ADULT_SDS, true, true,",
    "ADULT_SDS, true, ,",
    "ADULT_SDS, false, false,",
    "ADULT_SDS, false, true,",
    "ADULT_SDS, false, ,",
    "INDETERMINATE, true, false,N/A - indeterminate or extended sentence",
    "INDETERMINATE, true, true,N/A - indeterminate or extended sentence",
    "INDETERMINATE, true, ,N/A - indeterminate or extended sentence",
    "INDETERMINATE, false, false,N/A - indeterminate or extended sentence",
    "INDETERMINATE, false, true,N/A - indeterminate or extended sentence",
    "INDETERMINATE, false, ,N/A - indeterminate or extended sentence",
    "EXTENDED, true, false,N/A - indeterminate or extended sentence",
    "EXTENDED, true, true,N/A - indeterminate or extended sentence",
    "EXTENDED, true, ,N/A - indeterminate or extended sentence",
    "EXTENDED, false, false,N/A - indeterminate or extended sentence",
    "EXTENDED, false, true,N/A - indeterminate or extended sentence",
    "EXTENDED, false, ,N/A - indeterminate or extended sentence",
  )
  fun `isMappaLevel2or3AsYouthSdsUnder12Months set correctly`(
    sentenceGroup: SentenceGroup?,
    isMappaLevel2or3: Boolean?,
    isYouthSentenceOver12Months: Boolean?,
    expectedIsMappaLevel2or3AsYouthSdsUnder12Months: String?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        isMappaLevel2Or3 = isMappaLevel2or3,
        sentenceGroup = sentenceGroup,
        isYouthSentenceOver12Months = isYouthSentenceOver12Months,
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.isMappaLevel2or3AsYouthSdsUnder12Months).isEqualTo(
        expectedIsMappaLevel2or3AsYouthSdsUnder12Months,
      )
    }
  }

  @ParameterizedTest(name = "isMappaLevel2or3AsAdultSds set to {2} when sentence group is {0} and isMappaLevel2or3 is {1}")
  @CsvSource(
    "ADULT_SDS, true, Yes",
    "ADULT_SDS, false, No",
    "YOUTH_SDS, true,",
    "YOUTH_SDS, false,",
    "INDETERMINATE, true,N/A - indeterminate or extended sentence",
    "INDETERMINATE, false,N/A - indeterminate or extended sentence",
    "EXTENDED, true,N/A - indeterminate or extended sentence",
    "EXTENDED, false,N/A - indeterminate or extended sentence",
  )
  fun `isMappaLevel2or3AsAdultSds set correctly`(
    sentenceGroup: SentenceGroup?,
    isMappaLevel2or3: Boolean?,
    expectedIsMappaLevel2or3AsAdultSds: String?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        isMappaLevel2Or3 = isMappaLevel2or3,
        sentenceGroup = sentenceGroup,
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.isMappaLevel2or3AsAdultSds).isEqualTo(expectedIsMappaLevel2or3AsAdultSds)
    }
  }

  @ParameterizedTest(name = "isMappaCategory4AsAdultSds set to {2} when sentence group is {0} and isMappaCategory4 is {1}")
  @CsvSource(
    "ADULT_SDS, true, Yes",
    "ADULT_SDS, false, No",
    "YOUTH_SDS, true,",
    "YOUTH_SDS, false,",
    "INDETERMINATE, true,N/A - indeterminate or extended sentence",
    "INDETERMINATE, false,N/A - indeterminate or extended sentence",
    "EXTENDED, true,N/A - indeterminate or extended sentence",
    "EXTENDED, false,N/A - indeterminate or extended sentence",
  )
  fun `isMappaCategory4AsAdultSds set correctly`(
    sentenceGroup: SentenceGroup?,
    isMappaCategory4: Boolean?,
    expectedIsMappaCategory4AsAdultSds: String?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        isMappaCategory4 = isMappaCategory4,
        sentenceGroup = sentenceGroup,
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.isMappaCategory4AsAdultSds).isEqualTo(expectedIsMappaCategory4AsAdultSds)
    }
  }

  @Test
  fun `given null date vlo informed then should map to empty string in part A text`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        dateVloInformed = null,
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.dateVloInformed).isEqualTo("")
    }
  }

  @ParameterizedTest(name = "given is indeterminate sentence type field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("LIFE,Yes - Lifer", "IPP,Yes - IPP", "DPP,Yes - DPP", "DHMP,Yes - DHMP", "NO,No")
  fun `given indeterminate sentence type data then should map to the part A text`(
    indeterminateSentenceType: IndeterminateSentenceTypeOptions,
    partADisplayText: String?,
  ) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        indeterminateSentenceType = IndeterminateSentenceType(selected = indeterminateSentenceType),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.indeterminateSentenceType).isEqualTo(partADisplayText)
    }
  }

  @Test
  fun `given has arrest issues data then should map in part A text`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        hasArrestIssues = SelectedWithDetails(selected = true, details = "Arrest details"),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.hasArrestIssues?.value).isEqualTo("Yes")
      assertThat(result.hasArrestIssues?.details).isEqualTo("Arrest details")
    }
  }

  @Test
  fun `given has no arrest issues data then should map in part A text`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        hasArrestIssues = SelectedWithDetails(selected = null),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.hasArrestIssues?.value).isEqualTo("")
    }
  }

  @Test
  fun `given has contraband risk data then should map in part A text`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        hasContrabandRisk = SelectedWithDetails(selected = true, details = "Contraband risk details"),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.hasContrabandRisk?.value).isEqualTo("Yes")
      assertThat(result.hasContrabandRisk?.details).isEqualTo("Contraband risk details")
    }
  }

  @Test
  fun `given deprecated selected alternative licence conditions then build up the text for alternative licences in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        licenceConditionsBreached = LicenceConditionsBreached(
          additionalLicenceConditions = AdditionalLicenceConditions(
            selected = listOf("NST14"),
            allOptions = listOf(
              AdditionalLicenceConditionOption(
                subCatCode = "NST14",
                mainCatCode = "NLC5",
                title = "I am a title",
                details = "details1",
                note = "note1",
              ),
            ),
          ),
          standardLicenceConditions = null,
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      val expectedResult = StringBuilder().append("I am a title").append(System.lineSeparator()).append("details1")
        .append(System.lineSeparator()).append("Note: note1")
      assertThat(result.additionalConditionsBreached).isEqualTo(expectedResult.toString())
    }
  }

  @Test
  fun `given alternative licence conditions then build up the text for alternative licences in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        licenceConditionsBreached = LicenceConditionsBreached(
          additionalLicenceConditions = AdditionalLicenceConditions(
            selectedOptions = listOf(SelectedOption(mainCatCode = "NLC5", subCatCode = "NST14")),
            allOptions = listOf(
              AdditionalLicenceConditionOption(
                subCatCode = "NST14",
                mainCatCode = "NLC5",
                title = "I am a title",
                details = "details1",
                note = "note1",
              ),
            ),
          ),
          standardLicenceConditions = null,
        ),
        additionalLicenceConditionsText = "Not to drink any alcohol",
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      val expectedResult = StringBuilder()
        .append("I am a title")
        .append(System.lineSeparator())
        .append("details1")
        .append(System.lineSeparator())
        .append("Note: note1")
        .append(System.lineSeparator())
        .append(System.lineSeparator())
        .append("Not to drink any alcohol")
      assertThat(result.additionalConditionsBreached).isEqualTo(expectedResult.toString())
    }
  }

  @Test
  fun `given alternative licence conditions from cvl then build up the text for alternative licences in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        licenceConditionsBreached = null,
        cvlLicenceConditionsBreached = CvlLicenceConditionsBreached(
          standardLicenceConditions = null,
          additionalLicenceConditions = LicenceConditionSection(
            selected = listOf("1", "2"),
            allOptions = listOf(
              LicenceConditionOption("1", "one"),
              LicenceConditionOption("3", "three"),
              LicenceConditionOption("2", "two"),
            ),
          ),
          bespokeLicenceConditions = LicenceConditionSection(
            selected = listOf("5", "6"),
            allOptions = listOf(LicenceConditionOption("5", "five"), LicenceConditionOption("6", "six")),
          ),
        ),
        additionalLicenceConditionsText = "Not to drink any alcohol",
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.additionalConditionsBreached).isEqualTo("one\n\ntwo\n\nfive\n\nsix\n\nNot to drink any alcohol")
    }
  }

  @Test
  fun `given multiple alternative licence conditions then build up the text with line breaks for alternative licences in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        licenceConditionsBreached = LicenceConditionsBreached(
          additionalLicenceConditions = AdditionalLicenceConditions(
            selectedOptions = listOf(
              SelectedOption(mainCatCode = "NLC5", subCatCode = "NST14"),
              SelectedOption(mainCatCode = "ABC", subCatCode = "XYZ"),
            ),
            allOptions = listOf(
              AdditionalLicenceConditionOption(
                subCatCode = "NST14",
                mainCatCode = "NLC5",
                title = "I am a title",
                details = "details1",
                note = "note1",
              ),
              AdditionalLicenceConditionOption(
                subCatCode = "XYZ",
                mainCatCode = "ABC",
                title = "I am another title",
                details = "details2",
                note = "note2",
              ),
              AdditionalLicenceConditionOption(
                subCatCode = "XYZ",
                mainCatCode = "DIFFERENT",
                title = "I am different title",
                details = "details3",
                note = "note3",
              ),
            ),
          ),
          standardLicenceConditions = null,
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

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
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        licenceConditionsBreached = LicenceConditionsBreached(
          additionalLicenceConditions = AdditionalLicenceConditions(
            selectedOptions = listOf(
              SelectedOption(mainCatCode = "NLC5", subCatCode = "NST14"),
              SelectedOption(mainCatCode = "ABC", subCatCode = "XYZ"),
            ),
            allOptions = listOf(
              AdditionalLicenceConditionOption(
                subCatCode = "NST14",
                mainCatCode = "NLC5",
                title = "I am a title",
                details = "details1",
              ),
              AdditionalLicenceConditionOption(
                subCatCode = "XYZ",
                mainCatCode = "ABC",
                title = "I am another title",
                details = "details2",
              ),
            ),
          ),
          standardLicenceConditions = null,
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      val expectedResult = StringBuilder().append("I am a title").append(System.lineSeparator()).append("details1")
        .append(System.lineSeparator()).append(System.lineSeparator())
        .append("I am another title").append(System.lineSeparator()).append("details2")

      assertThat(result.additionalConditionsBreached).isEqualTo(expectedResult.toString())
    }
  }

  @Test
  fun `given has is under integrated offender management then should map in part A text`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        underIntegratedOffenderManagement = UnderIntegratedOffenderManagement(
          selected = "YES",
          allOptions = listOf(
            TextValueOption(value = "YES", text = "Yes"),
            TextValueOption(value = "NO", text = "No"),
            TextValueOption(value = "NOT_APPLICABLE", text = "N/A"),
          ),
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.isUnderIntegratedOffenderManagement).isEqualTo("YES")
    }
  }

  @Test
  fun `given no alternative licence conditions then return empty text for alternative licences in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        licenceConditionsBreached = LicenceConditionsBreached(
          additionalLicenceConditions = null,
          standardLicenceConditions = null,
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.additionalConditionsBreached).isEqualTo(EMPTY_STRING)
    }
  }

  @Test
  fun `given selected standard licence conditions then return this for standard licences in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        licenceConditionsBreached = LicenceConditionsBreached(
          additionalLicenceConditions = null,
          standardLicenceConditions = StandardLicenceConditions(
            selected = listOf(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name),
          ),
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.selectedStandardConditionsBreached?.get(0)).isEqualTo(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name)
    }
  }

  @Test
  fun `given selected standard licence conditions from cvl then return this for standard licences in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        licenceConditionsBreached = null,
        cvlLicenceConditionsBreached = CvlLicenceConditionsBreached(
          standardLicenceConditions = LicenceConditionSection(
            selected = listOf(
              SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.cvlCode,
              SelectedStandardLicenceConditions.ADDRESS_APPROVED.cvlCode,
            ),
          ),
          additionalLicenceConditions = null,
          bespokeLicenceConditions = null,
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.selectedStandardConditionsBreached?.get(0)).isEqualTo(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name)
      assertThat(result.selectedStandardConditionsBreached?.get(1)).isEqualTo(SelectedStandardLicenceConditions.ADDRESS_APPROVED.name)
    }
  }

  @Test
  fun `given conviction details then convert dates and sentence lengths in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        convictionDetail = ConvictionDetail(
          indexOffenceDescription = "Armed robbery",
          dateOfOriginalOffence = LocalDate.parse("2022-09-01"),
          dateOfSentence = LocalDate.parse("2022-09-04"),
          lengthOfSentence = 6,
          lengthOfSentenceUnits = "days",
          licenceExpiryDate = LocalDate.parse("2022-09-05"),
          sentenceExpiryDate = LocalDate.parse("2022-09-06"),
          sentenceSecondLength = 20,
          sentenceSecondLengthUnits = "days",
          custodialTerm = "6 days",
          extendedTerm = "20 days",
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.indexOffenceDescription).isEqualTo("Armed robbery")
      assertThat(result.dateOfOriginalOffence).isEqualTo("01/09/2022")
      assertThat(result.dateOfSentence).isEqualTo("04/09/2022")
      assertThat(result.lengthOfSentence).isEqualTo("6 days")
      assertThat(result.licenceExpiryDate).isEqualTo("05/09/2022")
      assertThat(result.sentenceExpiryDate).isEqualTo("06/09/2022")

      assertThat(result.custodialTerm).isEqualTo("6 days")
      assertThat(result.extendedTerm).isEqualTo("20 days")
    }
  }

  @Test
  fun `given conviction details with empty dates then set empty dates in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        convictionDetail = ConvictionDetail(
          indexOffenceDescription = "Armed robbery",
          dateOfOriginalOffence = null,
          dateOfSentence = null,
          lengthOfSentence = null,
          lengthOfSentenceUnits = null,
          licenceExpiryDate = null,
          sentenceExpiryDate = null,
          sentenceSecondLength = null,
          sentenceSecondLengthUnits = null,
          custodialTerm = WHITE_SPACE,
          extendedTerm = WHITE_SPACE,
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

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
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        convictionDetail = null,
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

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
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        personOnProbation = PersonOnProbation(
          addresses = listOf(
            Address(
              line1 = "Line 1 address",
              line2 = "Line 2 address",
              town = "Address town",
              postcode = "TS1 1ST",
              noFixedAbode = false,
            ),
          ),
        ).toPersonOnProbationDto(),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.lastRecordedAddress).isEqualTo("Line 1 address, Line 2 address, Address town, TS1 1ST")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given address is no fixed abode then clear address in the part A and set no fixed abode field`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        personOnProbation = PersonOnProbation(
          addresses = listOf(
            Address(
              line1 = "Line 1 address",
              line2 = "Line 2 address",
              town = "Address town",
              postcode = "TS1 1ST",
              noFixedAbode = true,
            ),
          ),
        ).toPersonOnProbationDto(),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.lastRecordedAddress).isEqualTo("")
      assertThat(result.noFixedAbode).isEqualTo("No fixed abode")
    }
  }

  @Test
  fun `given multiple main address details then set address details in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        personOnProbation = PersonOnProbation(
          addresses = listOf(
            Address(
              line1 = "Line 1 address",
              line2 = "Line 2 address",
              town = "Address town",
              postcode = "TS1 1ST",
              noFixedAbode = false,
            ),
            Address(
              line1 = "Line 1 second address",
              line2 = "Line 2 second address",
              town = "Address second town",
              postcode = "TS1 2ST",
              noFixedAbode = false,
            ),
          ),
        ).toPersonOnProbationDto(),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.lastRecordedAddress).isEqualTo("Line 1 address, Line 2 address, Address town, TS1 1ST\nLine 1 second address, Line 2 second address, Address second town, TS1 2ST")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given multiple main address details with contradicting no fixed abode values then clear the address details in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        personOnProbation = PersonOnProbation(
          addresses = listOf(
            Address(
              line1 = "Line 1 address",
              line2 = "Line 2 address",
              town = "Address town",
              postcode = "TS1 1ST",
              noFixedAbode = true,
            ),
            Address(
              line1 = "Line 1 second address",
              line2 = "Line 2 second address",
              town = "Address second town",
              postcode = "TS1 2ST",
              noFixedAbode = false,
            ),
          ),
        ).toPersonOnProbationDto(),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.lastRecordedAddress).isEqualTo("")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given empty address details then clear the address details in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        personOnProbation = PersonOnProbation().toPersonOnProbationDto(),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.lastRecordedAddress).isEqualTo("")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given main address with all empty lines then clear the address details in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        personOnProbation = PersonOnProbation(
          addresses = listOf(
            Address(
              line1 = "",
              line2 = "",
              town = "",
              postcode = "",
              noFixedAbode = false,
            ),
          ),
        ).toPersonOnProbationDto(),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.lastRecordedAddress).isEqualTo("")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given main address with some empty lines then correctly format the address in the part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        personOnProbation = PersonOnProbation(
          addresses = listOf(
            Address(
              line1 = "Line 1 address",
              line2 = "",
              town = "Address town",
              postcode = "TS1 1ST",
              noFixedAbode = false,
            ),
          ),
        ).toPersonOnProbationDto(),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.lastRecordedAddress).isEqualTo("Line 1 address, Address town, TS1 1ST")
      assertThat(result.noFixedAbode).isEqualTo("")
    }
  }

  @Test
  fun `given other possible address field is populated then format it for the Part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        mainAddressWherePersonCanBeFound = SelectedWithDetails(
          selected = false,
          details = "123 Oak Avenue, Birmingham, B23 1AV",
        ),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.otherPossibleAddresses).isEqualTo("Police can find this person at: 123 Oak Avenue, Birmingham, B23 1AV")
    }
  }

  @ParameterizedTest(name = "given empty address value is {0} then leave this field empty in the Part A")
  @CsvSource("''", "null", nullValues = ["null"])
  fun `given empty other possible address field then leave this field empty in the Part A`(addressValue: String?) {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        mainAddressWherePersonCanBeFound = SelectedWithDetails(selected = true, details = addressValue),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.otherPossibleAddresses).isEqualTo(null)
    }
  }

  @Test
  fun `given offence analysis details then show in the Part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        indexOffenceDetails = "I am the index offence details",
        offenceAnalysis = "I am the offence analysis details",
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.offenceAnalysis).isEqualTo("I am the offence analysis details")
    }
  }

  @Test
  fun `given previous release details with multiple release dates then format it for the Part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        previousReleases = PreviousReleases(
          lastReleaseDate = LocalDate.parse("2022-09-05"),
          lastReleasingPrisonOrCustodialEstablishment = "HMP Holloway",
          previousReleaseDates = listOf(LocalDate.parse("2022-05-09"), LocalDate.parse("2020-02-01")),
        ),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.lastReleasingPrison).isEqualTo("HMP Holloway")
      assertThat(result.datesOfLastReleases).isEqualTo("09/05/2022, 01/02/2020")
    }
  }

  @Test
  fun `given previous release details with one last release date then format it for the Part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        previousReleases = PreviousReleases(
          lastReleaseDate = LocalDate.parse("2022-09-05"),
          lastReleasingPrisonOrCustodialEstablishment = "HMP Holloway",
          previousReleaseDates = null,
        ),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.lastReleasingPrison).isEqualTo("HMP Holloway")
      assertThat(result.lastReleaseDate).isEqualTo("05/09/2022")
    }
  }

  @Test
  fun `given previous recall details with multiple recall dates then format it for the Part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        previousRecalls = PreviousRecalls(
          lastRecallDate = LocalDate.parse("2022-09-05"),
          hasBeenRecalledPreviously = true,
          previousRecallDates = listOf(LocalDate.parse("2020-02-01"), LocalDate.parse("2018-06-21")),
        ),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.datesOfLastRecalls).isEqualTo("05/09/2022, 01/02/2020, 21/06/2018")
    }
  }

  @Test
  fun `given previous recall details with one last recall date then format it for the Part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        previousRecalls = PreviousRecalls(
          lastRecallDate = LocalDate.parse("2022-09-05"),
          hasBeenRecalledPreviously = true,
          previousRecallDates = null,
        ),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.datesOfLastRecalls).isEqualTo("05/09/2022")
    }
  }

  @Test
  fun `given rosh data then format it for the Part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        currentRoshForPartA = RoshData(
          riskToChildren = RoshDataScore.VERY_HIGH,
          riskToPublic = RoshDataScore.HIGH,
          riskToKnownAdult = RoshDataScore.MEDIUM,
          riskToStaff = RoshDataScore.LOW,
          riskToPrisoners = RoshDataScore.NOT_APPLICABLE,
        ),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.riskToChildren).isEqualTo("Very High")
      assertThat(result.riskToPublic).isEqualTo("High")
      assertThat(result.riskToKnownAdult).isEqualTo("Medium")
      assertThat(result.riskToStaff).isEqualTo("Low")
      assertThat(result.riskToPrisoners).isEqualTo("N/A")
    }
  }

  @Test
  fun `given indeterminate or extended sentence then populate fields for question 24 in Part A with selected values`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        indeterminateOrExtendedSentenceDetails = IndeterminateOrExtendedSentenceDetails(
          selected = listOf(
            ValueWithDetails(
              value = "BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE",
              details = "Some behaviour similar to index offence",
            ),
            ValueWithDetails(
              value = "BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE",
              details = "Behaviour leading to sexual or violent behaviour",
            ),
            ValueWithDetails(
              value = "BEHAVIOUR_LIKELY_TO_RESULT_SEXUAL_OR_VIOLENT_OFFENCE",
              details = "Behaviour likely to result in sexual or violent behaviour",
            ),
            ValueWithDetails(
              value = "OUT_OF_TOUCH",
              details = "Out of touch",
            ),
          ),
          allOptions = listOf(
            TextValueOption(
              text = "Some behaviour similar to index offence",
              value = "BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE",
            ),
            TextValueOption(
              text = "Behaviour leading to sexual or violent behaviour",
              value = "BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE",
            ),
            TextValueOption(
              text = "Behaviour likely to result in sexual or violent behaviour",
              value = "BEHAVIOUR_LIKELY_TO_RESULT_SEXUAL_OR_VIOLENT_OFFENCE",
            ),
            TextValueOption(
              text = "Out of touch",
              value = "OUT_OF_TOUCH",
            ),
          ),
        ),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.behaviourSimilarToIndexOffencePresent).isEqualTo("Yes")
      assertThat(result.behaviourSimilarToIndexOffence).isEqualTo("Some behaviour similar to index offence")
      assertThat(result.behaviourLeadingToSexualOrViolentOffencePresent).isEqualTo("Yes")
      assertThat(result.behaviourLeadingToSexualOrViolentOffence).isEqualTo("Behaviour leading to sexual or violent behaviour")
      assertThat(result.behaviourLikelyToResultSexualOrViolentOffencePresent).isEqualTo("Yes")
      assertThat(result.behaviourLikelyToResultSexualOrViolentOffence).isEqualTo("Behaviour likely to result in sexual or violent behaviour")
      assertThat(result.outOfTouchPresent).isEqualTo("Yes")
      assertThat(result.outOfTouch).isEqualTo("Out of touch")
    }
  }

  @Test
  fun `given indeterminate or extended sentence then populate fields for question 24 in Part A with No for values not selected`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        indeterminateOrExtendedSentenceDetails = IndeterminateOrExtendedSentenceDetails(
          selected = null,
          allOptions = listOf(
            TextValueOption(
              text = "Some behaviour similar to index offence",
              value = "BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE",
            ),
            TextValueOption(
              text = "Behaviour leading to sexual or violent behaviour",
              value = "BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE",
            ),
            TextValueOption(
              text = "Behaviour likely to result in sexual or violent behaviour",
              value = "BEHAVIOUR_LIKELY_TO_RESULT_SEXUAL_OR_VIOLENT_OFFENCE",
            ),
            TextValueOption(
              text = "Out of touch",
              value = "OUT_OF_TOUCH",
            ),
          ),
        ),
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.behaviourSimilarToIndexOffencePresent).isEqualTo("No")
      assertThat(result.behaviourSimilarToIndexOffence).isEqualTo("")
      assertThat(result.behaviourLeadingToSexualOrViolentOffencePresent).isEqualTo("No")
      assertThat(result.behaviourLeadingToSexualOrViolentOffence).isEqualTo("")
      assertThat(result.behaviourLikelyToResultSexualOrViolentOffencePresent).isEqualTo("No")
      assertThat(result.behaviourLikelyToResultSexualOrViolentOffence).isEqualTo("")
      assertThat(result.outOfTouchPresent).isEqualTo("No")
      assertThat(result.outOfTouch).isEqualTo("")
    }
  }

  @Test
  fun `given non indeterminate or extended sentence then do not populate fields for question 24 in Part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        indeterminateOrExtendedSentenceDetails = null,
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.behaviourSimilarToIndexOffencePresent).isEqualTo("")
      assertThat(result.behaviourSimilarToIndexOffence).isEqualTo("")
      assertThat(result.behaviourLeadingToSexualOrViolentOffencePresent).isEqualTo("")
      assertThat(result.behaviourLeadingToSexualOrViolentOffence).isEqualTo("")
      assertThat(result.behaviourLikelyToResultSexualOrViolentOffencePresent).isEqualTo("")
      assertThat(result.behaviourLikelyToResultSexualOrViolentOffence).isEqualTo("")
      assertThat(result.outOfTouchPresent).isEqualTo("")
      assertThat(result.outOfTouch).isEqualTo("")
    }
  }

  @Test
  fun `Q25 mappings include who completed Part A details`() {
    runTest {
      val featureFlags = FeatureFlags()
      given(regionService.getRegionName(null))
        .willReturn("")
      given(regionService.getRegionName("RegionCode"))
        .willReturn("Region Name")
      val recommendation = RecommendationResponse(
        region = "Incorrect region",
        localDeliveryUnit = "Incorrect LDU",
        whoCompletedPartA = WhoCompletedPartA(
          name = "Joe Bloggs",
          telephone = "0123456789",
          email = "jb@example.com",
          region = "RegionCode",
          localDeliveryUnit = "Delivery Unit 1",
        ),
      )
      val metadata = RecommendationMetaData(
        userNamePartACompletedBy = "Incorrect Practitioner",
        userEmailPartACompletedBy = "incorrect@example.com",
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata, featureFlags)

      assertThat(result.completedBy.name).isEqualTo("Joe Bloggs")
      assertThat(result.completedBy.telephone).isEqualTo("0123456789")
      assertThat(result.completedBy.email).isEqualTo("jb@example.com")
      assertThat(result.completedBy.region).isEqualTo("Region Name")
      assertThat(result.completedBy.localDeliveryUnit).isEqualTo("Delivery Unit 1")
    }
  }

  @Test
  fun `given person who completed is not supervising practitioner then Q26 mappings contain supervising practitioner details`() {
    runTest {
      val featureFlags = FeatureFlags()
      given(regionService.getRegionName("RegionCode"))
        .willReturn("Region Name")
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        whoCompletedPartA = WhoCompletedPartA(
          isPersonProbationPractitionerForOffender = false,
        ),
        practitionerForPartA = PractitionerForPartA(
          name = "Joe Bloggs",
          telephone = "0123456789",
          email = "jb@example.com",
          region = "RegionCode",
          localDeliveryUnit = "Delivery Unit 2",
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata, featureFlags)

      assertThat(result.supervisingPractitioner.name).isEqualTo("Joe Bloggs")
      assertThat(result.supervisingPractitioner.telephone).isEqualTo("0123456789")
      assertThat(result.supervisingPractitioner.email).isEqualTo("jb@example.com")
      assertThat(result.supervisingPractitioner.region).isEqualTo("Region Name")
      assertThat(result.supervisingPractitioner.localDeliveryUnit).isEqualTo("Delivery Unit 2")
    }
  }

  @Test
  fun `given person who completed is the supervising practitioner then Q26 mappings are empty`() {
    runTest {
      val featureFlags = FeatureFlags()
      given(regionService.getRegionName(null))
        .willReturn("")
      val recommendation = RecommendationResponse(
        whoCompletedPartA = WhoCompletedPartA(
          isPersonProbationPractitionerForOffender = true,
        ),
        practitionerForPartA = PractitionerForPartA(
          name = "Joe Bloggs",
          telephone = "0123456789",
          email = "jb@example.com",
          region = "RegionCode",
          localDeliveryUnit = "Delivery Unit 2",
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata, featureFlags)

      assertThat(result.supervisingPractitioner.name).isEqualTo(EMPTY_STRING)
      assertThat(result.supervisingPractitioner.telephone).isEqualTo(EMPTY_STRING)
      assertThat(result.supervisingPractitioner.email).isEqualTo(EMPTY_STRING)
      assertThat(result.supervisingPractitioner.region).isEqualTo(EMPTY_STRING)
      assertThat(result.supervisingPractitioner.localDeliveryUnit).isEqualTo(EMPTY_STRING)
    }
  }

  @ParameterizedTest(name = "probation practitioner details are included when practitioner isPersonProbationPractitionerForOffender is {0}")
  @ValueSource(booleans = [true, false])
  fun `probation practitioner details are always included`(isPersonProbationPractitionerForOffender: Boolean) {
    runTest {
      val featureFlags = FeatureFlags()
      given(regionService.getRegionName("RegionCode"))
        .willReturn("Region Name")
      if (!isPersonProbationPractitionerForOffender) {
        given(regionService.getRegionName(null))
          .willReturn("")
      }
      val recommendation = RecommendationResponse(
        whoCompletedPartA = WhoCompletedPartA(
          name = "Joe Bloggs",
          telephone = "0123456789",
          email = "jb@example.com",
          region = "RegionCode",
          localDeliveryUnit = "Delivery Unit 1",
          isPersonProbationPractitionerForOffender = isPersonProbationPractitionerForOffender,
        ),
        practitionerForPartA = PractitionerForPartA(
          name = "Jane Vloggs",
          telephone = "9876543210",
          email = "jv@example.com",
        ),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata, featureFlags)

      assertThat(result.probationPractitionerDetails.name)
        .isEqualTo(if (isPersonProbationPractitionerForOffender) recommendation.whoCompletedPartA?.name else recommendation.practitionerForPartA?.name)
      assertThat(result.probationPractitionerDetails.telephone)
        .isEqualTo(if (isPersonProbationPractitionerForOffender) recommendation.whoCompletedPartA?.telephone else recommendation.practitionerForPartA?.telephone)
      assertThat(result.probationPractitionerDetails.email)
        .isEqualTo(if (isPersonProbationPractitionerForOffender) recommendation.whoCompletedPartA?.email else recommendation.practitionerForPartA?.email)
    }
  }

  @Test
  fun `given ppcs query emails and supervising practitioner completed the part A then show emails in Q25`() {
    runTest {
      val emails = listOf("test1@example.com", "test2@example.com")
      given(regionService.getRegionName(any()))
        .willReturn("")
      val featureFlags = FeatureFlags()
      val recommendation = RecommendationResponse(
        whoCompletedPartA = WhoCompletedPartA(
          isPersonProbationPractitionerForOffender = true,
        ),
        ppcsQueryEmails = emails,
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata, featureFlags)

      assertThat(result.completedBy.ppcsQueryEmails).hasSameElementsAs(emails)
      assertThat(result.supervisingPractitioner.ppcsQueryEmails).isEmpty()
    }
  }

  @Test
  fun `given ppcs query emails and supervising practitioner did not complete the part A then show emails in Q26`() {
    runTest {
      val emails = listOf("test1@example.com", "test2@example.com")
      val featureFlags = FeatureFlags()
      given(regionService.getRegionName(any()))
        .willReturn("")
      val recommendation = RecommendationResponse(
        whoCompletedPartA = WhoCompletedPartA(
          isPersonProbationPractitionerForOffender = false,
        ),
        ppcsQueryEmails = emails,
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata, featureFlags)

      assertThat(result.completedBy.ppcsQueryEmails).isEmpty()
      assertThat(result.supervisingPractitioner.ppcsQueryEmails).hasSameElementsAs(emails)
    }
  }

  @Test
  fun `given revocation recipients then show in the Part A`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("")
      val emails = listOf("test1@example.com", "test2@example.com")
      val recommendation = RecommendationResponse(
        revocationOrderRecipients = emails,
      )
      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.revocationOrderRecipients).hasSameElementsAs(emails)
    }
  }

  @Test
  fun `metadata mapped`() {
    runTest {
      given(regionService.getRegionName(null))
        .willReturn("Joe Bloggs")
      val recommendation = RecommendationResponse(
        id = 1,
        crn = "ABC123",
        dateVloInformed = LocalDate.parse("2022-09-01"),
      )

      val result = partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metadata)

      assertThat(result.counterSignSpoEmail).isEqualTo("john-the-spo@bla.com")
      assertThat(result.countersignAcoEmail).isEqualTo("jane-the-aco@bla.com")
      assertThat(result.countersignSpoName).isEqualTo("Spo Name")
      assertThat(result.countersignAcoName).isEqualTo("Aco Name")
      assertThat(result.countersignAcoDate).isNotBlank
      assertThat(result.countersignAcoTime).isNotBlank
      assertThat(result.countersignSpoDate).isNotBlank
      assertThat(result.countersignSpoTime).isNotBlank
    }
  }

  private val metadata = RecommendationMetaData(
    acoCounterSignEmail = "jane-the-aco@bla.com",
    spoCounterSignEmail = "john-the-spo@bla.com",
    countersignSpoName = "Spo Name",
    countersignAcoName = "Aco Name",
    userNamePartACompletedBy = "Joe Bloggs",
    userEmailPartACompletedBy = "Joe.Bloggs@test.com",
    countersignAcoDateTime = DateTimeHelper.dateTimeWithDaylightSavingFromString(
      LocalDateTime.now(ZoneId.of("UTC")).toString(),
    ),
    countersignSpoDateTime = DateTimeHelper.dateTimeWithDaylightSavingFromString(
      LocalDateTime.now(ZoneId.of("UTC")).toString(),
    ),
    userPartACompletedByDateTime = LocalDateTime.now(ZoneId.of("Europe/London")),
  )
}
