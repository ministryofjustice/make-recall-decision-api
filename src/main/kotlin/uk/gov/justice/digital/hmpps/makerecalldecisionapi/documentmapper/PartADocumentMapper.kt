package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_LIKELY_TO_RESULT_SEXUAL_OR_VIOLENT_OFFENCE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.OUT_OF_TOUCH
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionSection
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PractitionerDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SentenceGroup
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RegionService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationMetaData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToDateWithSlashes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToReadableDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.dateTimeWithDaylightSavingFromString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.splitDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NOT_APPLICABLE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.YES
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.calculateIsExtendedSentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.calculateIsIndeterminateSentence
import java.time.LocalDate

@Component
internal class PartADocumentMapper(
  private val regionService: RegionService,
) : RecommendationDataToDocumentMapper() {

  suspend fun mapRecommendationDataToDocumentData(
    recommendation: RecommendationResponse,
    metadata: RecommendationMetaData,
    flags: FeatureFlags = FeatureFlags(),
  ): DocumentData {
    val firstName = recommendation.personOnProbation?.firstName
    val middleNames = recommendation.personOnProbation?.middleNames
    val lastName = recommendation.personOnProbation?.surname
    val (lastRecordedAddress, noFixedAbode) = getAddressDetails(recommendation.personOnProbation?.addresses)
    val (lastDownloadDate, lastDownloadTime) = splitDateTime(metadata.userPartACompletedByDateTime)
    val (behaviourSimilarToIndexOffencePresent, behaviourSimilarToIndexOffence) = getIndeterminateOrExtendedSentenceDetails(
      recommendation.indeterminateOrExtendedSentenceDetails,
      BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE.name,
    )
    val (behaviourLeadingToSexualOrViolentOffencePresent, behaviourLeadingToSexualOrViolentOffence) = getIndeterminateOrExtendedSentenceDetails(
      recommendation.indeterminateOrExtendedSentenceDetails,
      BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE.name,
    )
    val (behaviourLikelyToResultSexualOrViolentOffencePresent, behaviourLikelyToResultSexualOrViolentOffence) = getIndeterminateOrExtendedSentenceDetails(
      recommendation.indeterminateOrExtendedSentenceDetails,
      BEHAVIOUR_LIKELY_TO_RESULT_SEXUAL_OR_VIOLENT_OFFENCE.name,
    )
    val (outOfTouchPresent, outOfTouch) = getIndeterminateOrExtendedSentenceDetails(
      recommendation.indeterminateOrExtendedSentenceDetails,
      OUT_OF_TOUCH.name,
    )
    val (countersignSpoDate, countersignSpoTime) = splitDateTime(metadata.countersignSpoDateTime)
    val (countersignAcoDate, countersignAcoTime) = splitDateTime(metadata.countersignAcoDateTime)

    val decisionDateTimeWithDaylightSaving =
      recommendation.decisionDateTime?.let { dateTimeWithDaylightSavingFromString(utcDateTimeString = it.toString()) }
    val (decisionDate, decisionTime) = splitDateTime(decisionDateTimeWithDaylightSaving)

    val lastRelease = recommendation.previousReleases?.lastReleaseDate
    val previousReleasesList = buildPreviousReleasesList(recommendation.previousReleases)
    val previousRecallsList = buildPreviousRecallsList(recommendation.previousRecalls)

    return DocumentData(
      custodyStatus = ValueWithDetails(
        recommendation.custodyStatus?.selected?.partADisplayValue ?: EMPTY_STRING,
        recommendation.custodyStatus?.details,
      ),
      recallType = findRecallTypeToDisplay(recommendation),
      responseToProbation = recommendation.responseToProbation,
      whatLedToRecall = recommendation.whatLedToRecall,
      isThisAnEmergencyRecall = convertBooleanToYesNo(recommendation.isThisAnEmergencyRecall),
      // we want the youth section to go unanswered for non-youth sentences, rather than explicitly say "No", as it
      // might otherwise confuse readers over whether it's youth or not
      isServingYouthSentence = if (recommendation.sentenceGroup == SentenceGroup.YOUTH_SDS) "Yes" else EMPTY_STRING,

      isUnder18 = generateExclusionCriteriaAnswer(recommendation.isUnder18, recommendation),
      isSentence12MonthsOrOver = generateExclusionCriteriaAnswer(
        recommendation.isSentence12MonthsOrOver,
        recommendation,
      ),
      isMappaAboveLevel1 = generateExclusionCriteriaAnswer(recommendation.isMappaLevelAbove1, recommendation),
      isChargedWithSeriousOffence = generateExclusionCriteriaAnswer(
        recommendation.hasBeenConvictedOfSeriousOffence,
        recommendation,
      ),
      isSentence48MonthsOrOver = generateExclusionCriteriaAnswer(
        recommendation.isSentence48MonthsOrOver,
        recommendation,
      ),
      isMappaCategory4 = generateExclusionCriteriaAnswer(
        recommendation.isMappaCategory4,
        recommendation,
      ),
      isMappaLevel2Or3 = generateExclusionCriteriaAnswer(
        recommendation.isMappaLevel2Or3,
        recommendation,
      ),
      isMappaLevel2or3AsYouthSdsUnder12Months = calculateIsMappaLevel2or3AsYouthSdsUnder12Months(recommendation),
      isMappaLevel2or3AsAdultSds = calculateIsMappaLevel2or3AsAdultSds(recommendation),
      isMappaCategory4AsAdultSds = calculateMappaCategory4AsAdultSds(recommendation),
      isRecalledOnNewChargedOffence = generateExclusionCriteriaAnswer(
        recommendation.isRecalledOnNewChargedOffence,
        recommendation,
      ),
      isServingFTSentenceForTerroristOffence = generateExclusionCriteriaAnswer(
        recommendation.isServingFTSentenceForTerroristOffence,
        recommendation,
      ),
      hasBeenChargedWithTerroristOrStateThreatOffence = generateExclusionCriteriaAnswer(
        recommendation.hasBeenChargedWithTerroristOrStateThreatOffence,
        recommendation,
      ),
      isChargedWithOffence = generateExclusionCriteriaAnswer(
        recommendation.isChargedWithOffence,
        recommendation,
      ),
      isServingTerroristOrNationalSecurityOffence = generateExclusionCriteriaAnswer(
        recommendation.isServingTerroristOrNationalSecurityOffence,
        recommendation,
      ),
      isAtRiskOfInvolvedInForeignPowerThreat = generateExclusionCriteriaAnswer(
        recommendation.isAtRiskOfInvolvedInForeignPowerThreat,
        recommendation,
      ),
      wasReferredToParoleBoard244ZB = generateExclusionCriteriaAnswer(
        recommendation.wasReferredToParoleBoard244ZB,
        recommendation,
      ),
      wasRepatriatedForMurder = generateExclusionCriteriaAnswer(
        recommendation.wasRepatriatedForMurder,
        recommendation,
      ),
      isServingSOPCSentence = generateExclusionCriteriaAnswer(
        recommendation.isServingSOPCSentence,
        recommendation,
      ),
      isServingDCRSentence = generateExclusionCriteriaAnswer(
        recommendation.isServingDCRSentence,
        recommendation,
      ),
      isYouthSentenceOver12Months = generateExclusionCriteriaAnswer(
        recommendation.isYouthSentenceOver12Months,
        recommendation,
      ),
      isYouthChargedWithSeriousOffence = generateExclusionCriteriaAnswer(
        recommendation.isYouthChargedWithSeriousOffence,
        recommendation,
      ),
      isExtendedSentence = convertBooleanToYesNo(recommendation.calculateIsExtendedSentence()),
      hasVictimsInContactScheme = recommendation.hasVictimsInContactScheme?.selected?.partADisplayValue
        ?: EMPTY_STRING,
      indeterminateSentenceType = recommendation.indeterminateSentenceType?.selected?.partADisplayValue
        ?: EMPTY_STRING,
      dateVloInformed = convertLocalDateToReadableDate(recommendation.dateVloInformed),
      selectedAlternatives = recommendation.alternativesToRecallTried?.selected,
      hasArrestIssues = ValueWithDetails(
        convertBooleanToYesNo(recommendation.hasArrestIssues?.selected),
        recommendation.hasArrestIssues?.details,
      ),
      hasContrabandRisk = ValueWithDetails(
        convertBooleanToYesNo(recommendation.hasContrabandRisk?.selected),
        recommendation.hasContrabandRisk?.details,
      ),
      selectedStandardConditionsBreached = buildStandardLicenceCodes(
        recommendation.licenceConditionsBreached?.standardLicenceConditions?.selected,
        recommendation.cvlLicenceConditionsBreached?.standardLicenceConditions?.selected,
      ),
      additionalConditionsBreached = buildAdditionalConditionsBreachedText(
        recommendation.licenceConditionsBreached?.additionalLicenceConditions,
        recommendation.cvlLicenceConditionsBreached?.additionalLicenceConditions,
        recommendation.cvlLicenceConditionsBreached?.bespokeLicenceConditions,
        recommendation.additionalLicenceConditionsText,
      ),
      isUnderIntegratedOffenderManagement = recommendation.underIntegratedOffenderManagement?.selected,
      localPoliceContact = recommendation.localPoliceContact,
      vulnerabilities = recommendation.vulnerabilities,
      gender = recommendation.personOnProbation?.gender,
      dateOfBirth = recommendation.personOnProbation?.dateOfBirth,
      name = formatFullName(firstName, middleNames, lastName),
      ethnicity = recommendation.personOnProbation?.ethnicity,
      croNumber = recommendation.personOnProbation?.croNumber,
      pncNumber = recommendation.personOnProbation?.pncNumber,
      crn = recommendation.crn,
      mostRecentPrisonerNumber = recommendation.personOnProbation?.mostRecentPrisonerNumber,
      nomsNumber = recommendation.personOnProbation?.nomsNumber,
      indexOffenceDescription = recommendation.convictionDetail?.indexOffenceDescription,
      dateOfOriginalOffence = buildFormattedLocalDate(recommendation.convictionDetail?.dateOfOriginalOffence),
      dateOfSentence = buildFormattedLocalDate(recommendation.convictionDetail?.dateOfSentence),
      lengthOfSentence = buildLengthOfSentence(recommendation.convictionDetail),
      licenceExpiryDate = buildFormattedLocalDate(recommendation.convictionDetail?.licenceExpiryDate),
      sentenceExpiryDate = buildFormattedLocalDate(recommendation.convictionDetail?.sentenceExpiryDate),
      custodialTerm = recommendation.convictionDetail?.custodialTerm,
      extendedTerm = recommendation.convictionDetail?.extendedTerm,
      mappa = recommendation.personOnProbation?.mappa,
      lastRecordedAddress = lastRecordedAddress,
      noFixedAbode = noFixedAbode,
      completedBy = determineCompletedBy(recommendation, metadata, flags),
      // we have separate supervisingPractitioner and probationPractitionerDetails fields because Part As pre-FTR56
      // include the details only if the person completing the Part A isn't the practitioner (the Part A had separate
      // sections and explicitly ask for the second one only to filled in if relevant), whereas the FTR56 version always
      // wants the practitioner's details, for which we need the logic in determineProbationPractitionerDetails. We
      // can't change determineSupervisingPractitioner, as we still need to support downloads of older Part As and we
      // don't want them to fill in the practitioner section if they are the same as the whoFilledPartA one
      supervisingPractitioner = determineSupervisingPractitioner(recommendation, flags),
      probationPractitionerDetails = determineProbationPractitionerDetails(recommendation),
      revocationOrderRecipients = recommendation.revocationOrderRecipients ?: emptyList(),
      dateOfDecision = decisionDate,
      timeOfDecision = decisionTime,
      offenceAnalysis = recommendation.offenceAnalysis,
      fixedTermAdditionalLicenceConditions = additionalLicenceConditionsTextToDisplay(recommendation),
      behaviourSimilarToIndexOffence = behaviourSimilarToIndexOffence,
      behaviourSimilarToIndexOffencePresent = behaviourSimilarToIndexOffencePresent,
      behaviourLeadingToSexualOrViolentOffence = behaviourLeadingToSexualOrViolentOffence,
      behaviourLeadingToSexualOrViolentOffencePresent = behaviourLeadingToSexualOrViolentOffencePresent,
      behaviourLikelyToResultSexualOrViolentOffence = behaviourLikelyToResultSexualOrViolentOffence,
      behaviourLikelyToResultSexualOrViolentOffencePresent = behaviourLikelyToResultSexualOrViolentOffencePresent,
      outOfTouch = outOfTouch,
      outOfTouchPresent = outOfTouchPresent,
      otherPossibleAddresses =
      formatAddressWherePersonCanBeFound(recommendation.mainAddressWherePersonCanBeFound?.details),
      primaryLanguage = recommendation.personOnProbation?.primaryLanguage,
      lastReleasingPrison = recommendation.previousReleases?.lastReleasingPrisonOrCustodialEstablishment,
      lastReleaseDate = buildFormattedLocalDate(lastRelease),
      datesOfLastReleases = formatMultipleDates(previousReleasesList),
      datesOfLastRecalls = formatMultipleDates(previousRecallsList),
      riskToChildren = recommendation.currentRoshForPartA?.riskToChildren?.partADisplayValue,
      riskToPublic = recommendation.currentRoshForPartA?.riskToPublic?.partADisplayValue,
      riskToKnownAdult = recommendation.currentRoshForPartA?.riskToKnownAdult?.partADisplayValue,
      riskToStaff = recommendation.currentRoshForPartA?.riskToStaff?.partADisplayValue,
      riskToPrisoners = recommendation.currentRoshForPartA?.riskToPrisoners?.partADisplayValue,

      releaseUnderECSL = recommendation.releaseUnderECSL,
      dateOfRelease = buildFormattedLocalDate(recommendation.dateOfRelease),
      conditionalReleaseDate = buildFormattedLocalDate(recommendation.conditionalReleaseDate),

      countersignAcoEmail = metadata.acoCounterSignEmail,
      counterSignSpoEmail = metadata.spoCounterSignEmail,
      countersignSpoName = metadata.countersignSpoName,
      countersignAcoName = metadata.countersignAcoName,
      countersignSpoDate = countersignSpoDate,
      countersignSpoTime = countersignSpoTime,
      countersignAcoDate = countersignAcoDate,
      countersignAcoTime = countersignAcoTime,
      countersignSpoTelephone = recommendation.countersignSpoTelephone,
      countersignSpoExposition = recommendation.countersignSpoExposition,
      countersignAcoTelephone = recommendation.countersignAcoTelephone,
      countersignAcoExposition = recommendation.countersignAcoExposition,
    )
  }

  private suspend fun determineCompletedBy(
    recommendation: RecommendationResponse,
    metadata: RecommendationMetaData,
    flags: FeatureFlags,
  ): PractitionerDetails {
    with(recommendation.whoCompletedPartA) {
      return PractitionerDetails(
        name = this?.name ?: "",
        telephone = this?.telephone ?: "",
        email = this?.email ?: "",
        region = regionService.getRegionName(this?.region),
        localDeliveryUnit = this?.localDeliveryUnit ?: "",
        ppcsQueryEmails = if (recommendation.whoCompletedPartA?.isPersonProbationPractitionerForOffender == true) {
          recommendation.ppcsQueryEmails ?: emptyList()
        } else {
          emptyList()
        },
      )
    }
  }

  private suspend fun determineSupervisingPractitioner(
    recommendation: RecommendationResponse,
    flags: FeatureFlags,
  ): PractitionerDetails = if (recommendation.whoCompletedPartA?.isPersonProbationPractitionerForOffender != true) {
    with(recommendation.practitionerForPartA) {
      PractitionerDetails(
        name = this?.name ?: "",
        telephone = this?.telephone ?: "",
        email = this?.email ?: "",
        region = regionService.getRegionName(this?.region),
        localDeliveryUnit = this?.localDeliveryUnit ?: "",
        ppcsQueryEmails = recommendation.ppcsQueryEmails ?: emptyList(),
      )
    }
  } else {
    PractitionerDetails()
  }

  private suspend fun determineProbationPractitionerDetails(recommendation: RecommendationResponse): PractitionerDetails = if (recommendation.whoCompletedPartA?.isPersonProbationPractitionerForOffender != true) {
    with(recommendation.practitionerForPartA) {
      PractitionerDetails(
        name = this?.name ?: "",
        telephone = this?.telephone ?: "",
        email = this?.email ?: "",
      )
    }
  } else {
    with(recommendation.whoCompletedPartA) {
      PractitionerDetails(
        name = this.name ?: "",
        telephone = this.telephone ?: "",
        email = this.email ?: "",
      )
    }
  }

  private fun buildPreviousReleasesList(previousReleases: PreviousReleases?): List<LocalDate> = previousReleases?.previousReleaseDates ?: emptyList()

  private fun buildPreviousRecallsList(previousRecalls: PreviousRecalls?): List<LocalDate> {
    var dates: List<LocalDate> = previousRecalls?.previousRecallDates ?: emptyList()
    if (previousRecalls?.lastRecallDate != null) {
      dates = listOf(previousRecalls.lastRecallDate) + dates
    }
    return dates
  }

  private fun findRecallTypeToDisplay(recommendation: RecommendationResponse): ValueWithDetails {
    val isIndeterminateSentence = recommendation.calculateIsIndeterminateSentence()
    val isExtendedSentence = recommendation.calculateIsExtendedSentence()
    if (isIndeterminateSentence == true || isExtendedSentence == true) {
      val textToDisplay = buildNotApplicableMessage(isIndeterminateSentence, isExtendedSentence, null)
      return ValueWithDetails(textToDisplay, textToDisplay)
    } else {
      val partAValue = when (recommendation.recallType?.selected?.value) {
        RecallTypeValue.STANDARD -> RecallTypeValue.STANDARD.displayValue
        RecallTypeValue.FIXED_TERM -> RecallTypeValue.FIXED_TERM.displayValue
        else -> null
      }
      return ValueWithDetails(partAValue, recommendation.recallType?.selected?.details)
    }
  }

  private fun additionalLicenceConditionsTextToDisplay(recommendation: RecommendationResponse): String? {
    val isStandardRecall: Boolean = recommendation.recallType?.selected?.value == RecallTypeValue.STANDARD
    val isIndeterminateSentence = recommendation.calculateIsIndeterminateSentence()
    val isExtendedSentence = recommendation.calculateIsExtendedSentence()
    return buildNotApplicableMessage(
      isIndeterminateSentence,
      isExtendedSentence,
      isStandardRecall,
    )
      ?: if (recommendation.fixedTermAdditionalLicenceConditions?.selected == true) recommendation.fixedTermAdditionalLicenceConditions.details else EMPTY_STRING
  }

  private fun buildNotApplicableMessage(
    isIndeterminateSentence: Boolean?,
    isExtendedSentence: Boolean?,
    isStandardRecall: Boolean?,
  ): String? = if (isIndeterminateSentence == true) {
    "$NOT_APPLICABLE (not a determinate recall)"
  } else if (isExtendedSentence == true) {
    "$NOT_APPLICABLE (extended sentence recall)"
  } else if (isStandardRecall == true) {
    "$NOT_APPLICABLE (standard recall)"
  } else {
    null
  }

  private fun convertBooleanToYesNo(value: Boolean?): String = if (value == true) {
    YES
  } else if (value == false) {
    NO
  } else {
    EMPTY_STRING
  }

  private fun generateExclusionCriteriaAnswer(
    value: Boolean?,
    recommendation: RecommendationResponse,
  ): String {
    val isIndeterminateSentence = recommendation.calculateIsIndeterminateSentence()
    val isExtendedSentence = recommendation.calculateIsExtendedSentence()

    return when {
      (isIndeterminateSentence ?: false) || (isExtendedSentence ?: false) -> "N/A - indeterminate or extended sentence"
      else -> if (value == true) {
        YES
      } else if (value == false) {
        NO
      } else {
        EMPTY_STRING
      }
    }
  }

  private fun buildStandardLicenceCodes(standardCodes: List<String>?, cvlStandardCodes: List<String>?): List<String>? {
    if (standardCodes !== null) {
      return standardCodes
    }
    if (cvlStandardCodes !== null) {
      return cvlStandardCodes
        .mapNotNull { code -> SelectedStandardLicenceConditions.entries.find { it.isCodeForCvl(code) } }
        .map { it.name }
    }
    return null
  }

  private fun buildAdditionalConditionsBreachedText(
    additionalLicenceConditions: AdditionalLicenceConditions?,
    cvlAdditionalLicenceConditions: LicenceConditionSection?,
    cvlBespokeLicenceConditions: LicenceConditionSection?,
    additionalText: String?,
  ): String {
    val result = StringBuilder()

    if (additionalLicenceConditions != null) {
      val selectedOptions = if (additionalLicenceConditions.selectedOptions != null) {
        additionalLicenceConditions.allOptions?.filter { sel ->
          additionalLicenceConditions.selectedOptions.any {
            it.subCatCode == sel.subCatCode && it.mainCatCode == sel.mainCatCode
          }
        }
      } else {
        // Left this line in to make the code backwards compatible after the issue described in MRD-1056 was fixed
        additionalLicenceConditions.allOptions
          ?.filter { additionalLicenceConditions.selected?.contains(it.subCatCode) == true }
      }

      selectedOptions?.foldIndexed(result) { index, builder, licenceCondition ->

        builder.append(licenceCondition.title)
        builder.append(System.lineSeparator())
        builder.append(licenceCondition.details)

        if (licenceCondition.note != null) {
          builder.append(System.lineSeparator())
          builder.append("Note: " + licenceCondition.note)
        }

        builder.append(System.lineSeparator())
        builder.append(System.lineSeparator())
        builder
      }
    } else {
      cvlAdditionalLicenceConditions?.selected
        ?.map { code -> cvlAdditionalLicenceConditions.allOptions?.find { it.code == code }?.text }
        ?.fold(result) { buffer, text ->
          buffer.append(text)
          result.append(System.lineSeparator())
          result.append(System.lineSeparator())
          buffer
        }

      cvlBespokeLicenceConditions?.selected
        ?.map { code -> cvlBespokeLicenceConditions.allOptions?.find { it.code == code }?.text }
        ?.fold(result) { buffer, text ->
          buffer.append(text)
          result.append(System.lineSeparator())
          result.append(System.lineSeparator())
          buffer
        }
    }

    if (additionalText != null) {
      result.append(additionalText)
      result.append(System.lineSeparator())
      result.append(System.lineSeparator())
    }

    return result.toString().trim()
  }

  private fun buildFormattedLocalDate(dateToConvert: LocalDate?): String = if (null != dateToConvert) {
    convertLocalDateToDateWithSlashes(dateToConvert)
  } else {
    EMPTY_STRING
  }

  private fun getAddressDetails(addresses: List<Address>?): Pair<String, String> {
    val mainAddresses = addresses?.filter { !it.noFixedAbode }
    val noFixedAbodeAddresses = addresses?.filter { it.noFixedAbode }

    return if (mainAddresses?.isNotEmpty() == true && noFixedAbodeAddresses?.isEmpty() == true) {
      val addressConcat = mainAddresses.map {
        it.separatorFormattedAddress(", ")
      }
      Pair(addressConcat.joinToString("\n") { it }, "")
    } else if (mainAddresses?.isEmpty() == true && noFixedAbodeAddresses?.isNotEmpty() == true) {
      Pair(
        "",
        "No fixed abode",
      )
    } else {
      Pair("", "")
    }
  }

  private fun buildLengthOfSentence(convictionDetail: ConvictionDetail?): String? = convictionDetail?.let {
    val lengthOfSentence = it.lengthOfSentence?.toString() ?: EMPTY_STRING
    val lengthOfSentenceUnits = it.lengthOfSentenceUnits ?: EMPTY_STRING

    "$lengthOfSentence $lengthOfSentenceUnits"
  }

  private fun getIndeterminateOrExtendedSentenceDetails(
    indeterminateOrExtendedSentenceDetails: IndeterminateOrExtendedSentenceDetails?,
    field: String,
  ): Pair<String, String?> = if (indeterminateOrExtendedSentenceDetails != null) {
    if (indeterminateOrExtendedSentenceDetails.selected?.any { it.value == field } == true) {
      Pair(YES, indeterminateOrExtendedSentenceDetails.selected.filter { it.value == field }[0].details)
    } else {
      Pair(NO, EMPTY_STRING)
    }
  } else {
    Pair(EMPTY_STRING, EMPTY_STRING)
  }

  private fun formatAddressWherePersonCanBeFound(details: String?): String? = if (details?.isBlank() == false) "Police can find this person at: $details" else null

  private fun formatMultipleDates(dates: List<LocalDate>?): String {
    if (dates != null) {
      return dates.joinToString(", ") { buildFormattedLocalDate(it) }
    }
    return EMPTY_STRING
  }

  /**
   * We want to leave the value empty for this field unless the person is serving a youth sentence of 12 months or
   * under, in which case we want to display the Mappa level 2 or 3 value. We explicitly don't want to return "No" if
   * either of the first two criteria (youth & sentence length) aren't met, as otherwise the reader of the Part A may
   * incorrectly infer that the two criteria are met but the offender isn't MAPPA level 2 or 3, when in reality the
   * criteria just aren't met and the section of the part A where this value is set is irrelevant.
   */
  private fun calculateIsMappaLevel2or3AsYouthSdsUnder12Months(recommendation: RecommendationResponse): String? = if (recommendation.sentenceGroup == SentenceGroup.YOUTH_SDS &&
    !(recommendation.isYouthSentenceOver12Months ?: true) // if null, we want to return null
  ) {
    convertBooleanToYesNo(recommendation.isMappaLevel2Or3)
  } else if (recommendation.sentenceGroup === SentenceGroup.INDETERMINATE || recommendation.sentenceGroup === SentenceGroup.EXTENDED) {
    generateExclusionCriteriaAnswer(false, recommendation)
  } else {
    null
  }

  /**
   * Similar logic to calculateIsMappaLevel2or3AsYouthSdsUnder12Months - we only want to return a value for this field
   * if the offender is serving an adult sentence, in which case we want to display the Mappa level 2 or 3 value. If the
   * offender is serving a different type of sentence, we want to leave the field empty to avoid readers of the Part A
   * incorrectly inferring that the offender is serving an adult sentence and isn't MAPPA level 2 or 3, when in reality
   * they're serving a different type of sentence and the section of the part A where this value is set is irrelevant.
   */
  private fun calculateIsMappaLevel2or3AsAdultSds(recommendation: RecommendationResponse): String? = if (recommendation.sentenceGroup == SentenceGroup.ADULT_SDS) {
    convertBooleanToYesNo(recommendation.isMappaLevel2Or3)
  } else if (recommendation.sentenceGroup === SentenceGroup.INDETERMINATE || recommendation.sentenceGroup === SentenceGroup.EXTENDED) {
    generateExclusionCriteriaAnswer(false, recommendation)
  } else {
    null
  }

  /**
   * Similar logic to calculateIsMappaLevel2or3AsYouthSdsUnder12Months - we only want to return a value for this field
   * if the offender is serving an adult sentence, in which case we want to display the Mappa category 4 value. If the
   * offender is serving a different type of sentence, we want to leave the field empty to avoid readers of the Part A
   * incorrectly inferring that the offender is serving an adult sentence and isn't MAPPA category 4, when in reality
   * they're serving a different type of sentence and the section of the part A where this value is set is irrelevant.
   */
  private fun calculateMappaCategory4AsAdultSds(recommendation: RecommendationResponse): String? = if (recommendation.sentenceGroup == SentenceGroup.ADULT_SDS) {
    convertBooleanToYesNo(recommendation.isMappaCategory4)
  } else if (recommendation.sentenceGroup === SentenceGroup.INDETERMINATE || recommendation.sentenceGroup === SentenceGroup.EXTENDED) {
    generateExclusionCriteriaAnswer(false, recommendation)
  } else {
    null
  }
}
