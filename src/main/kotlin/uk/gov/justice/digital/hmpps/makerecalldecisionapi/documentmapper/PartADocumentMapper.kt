package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.OUT_OF_TOUCH
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToDateWithSlashes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToReadableDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.splitDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NOT_APPLICABLE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.YES
import java.time.LocalDate

@Component
class PartADocumentMapper : RecommendationDataToDocumentMapper() {

  fun mapRecommendationDataToDocumentData(recommendation: RecommendationResponse): DocumentData {
    val firstName = recommendation.personOnProbation?.firstName
    val middleNames = recommendation.personOnProbation?.middleNames
    val lastName = recommendation.personOnProbation?.surname
    val (lastRecordedAddress, noFixedAbode) = getAddressDetails(recommendation.personOnProbation?.addresses)
    val (lastDownloadDate, lastDownloadTime) = splitDateTime(recommendation.lastPartADownloadDateTime)
    val (behaviourSimilarToIndexOffencePresent, behaviourSimilarToIndexOffence) = getIndeterminateOrExtendedSentenceDetails(recommendation.indeterminateOrExtendedSentenceDetails, BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE.name)
    val (behaviourLeadingToSexualOrViolentOffencePresent, behaviourLeadingToSexualOrViolentOffence) = getIndeterminateOrExtendedSentenceDetails(recommendation.indeterminateOrExtendedSentenceDetails, BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE.name)
    val (outOfTouchPresent, outOfTouch) = getIndeterminateOrExtendedSentenceDetails(recommendation.indeterminateOrExtendedSentenceDetails, OUT_OF_TOUCH.name)
    val (countersignSpoDate, countersignSpoTime) = splitDateTime(recommendation.countersignSpoDateTime)
    val (countersignAcoDate, countersignAcoTime) = splitDateTime(recommendation.countersignAcoDateTime)
    val lastRelease = recommendation.previousReleases?.lastReleaseDate
    val previousReleasesList = buildPreviousReleasesList(recommendation.previousReleases)
    val previousRecallsList = buildPreviousRecallsList(recommendation.previousRecalls)

    return DocumentData(
      custodyStatus = ValueWithDetails(
        recommendation.custodyStatus?.selected?.partADisplayValue ?: EMPTY_STRING,
        recommendation.custodyStatus?.details
      ),
      recallType = findRecallTypeToDisplay(
        recommendation.recallType,
        recommendation.isIndeterminateSentence,
        recommendation.isExtendedSentence
      ),
      responseToProbation = recommendation.responseToProbation,
      whatLedToRecall = recommendation.whatLedToRecall,
      isThisAnEmergencyRecall = convertBooleanToYesNo(recommendation.isThisAnEmergencyRecall),
      isExtendedSentence = convertBooleanToYesNo(recommendation.isExtendedSentence),
      hasVictimsInContactScheme = recommendation.hasVictimsInContactScheme?.selected?.partADisplayValue
        ?: EMPTY_STRING,
      indeterminateSentenceType = recommendation.indeterminateSentenceType?.selected?.partADisplayValue
        ?: EMPTY_STRING,
      dateVloInformed = convertLocalDateToReadableDate(recommendation.dateVloInformed),
      selectedAlternatives = recommendation.alternativesToRecallTried?.selected,
      hasArrestIssues = ValueWithDetails(
        convertBooleanToYesNo(recommendation.hasArrestIssues?.selected),
        recommendation.hasArrestIssues?.details
      ),
      hasContrabandRisk = ValueWithDetails(
        convertBooleanToYesNo(recommendation.hasContrabandRisk?.selected),
        recommendation.hasContrabandRisk?.details
      ),
      selectedStandardConditionsBreached = recommendation.licenceConditionsBreached?.standardLicenceConditions?.selected,
      additionalConditionsBreached = buildAlternativeConditionsBreachedText(recommendation.licenceConditionsBreached?.additionalLicenceConditions),
      isUnderIntegratedOffenderManagement = recommendation.underIntegratedOffenderManagement?.selected,
      localPoliceContact = recommendation.localPoliceContact,
      vulnerabilities = recommendation.vulnerabilities,
      gender = recommendation.personOnProbation?.gender,
      dateOfBirth = recommendation.personOnProbation?.dateOfBirth,
      name = formatFullName(firstName, middleNames, lastName),
      ethnicity = recommendation.personOnProbation?.ethnicity,
      croNumber = recommendation.personOnProbation?.croNumber,
      pncNumber = recommendation.personOnProbation?.pncNumber,
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
      probationPracticionerName = recommendation.userNamePartACompletedBy,
      probationPracticionerEmail = recommendation.userEmailPartACompletedBy,
      region = recommendation.region,
      localDeliveryUnit = recommendation.localDeliveryUnit,
      dateOfDecision = lastDownloadDate,
      timeOfDecision = lastDownloadTime,
      offenceAnalysis = recommendation.offenceAnalysis,
      fixedTermAdditionalLicenceConditions = additionalLicenceConditionsTextToDisplay(recommendation),
      behaviourSimilarToIndexOffence = behaviourSimilarToIndexOffence,
      behaviourSimilarToIndexOffencePresent = behaviourSimilarToIndexOffencePresent,
      behaviourLeadingToSexualOrViolentOffence = behaviourLeadingToSexualOrViolentOffence,
      behaviourLeadingToSexualOrViolentOffencePresent = behaviourLeadingToSexualOrViolentOffencePresent,
      outOfTouch = outOfTouch,
      outOfTouchPresent = outOfTouchPresent,
      otherPossibleAddresses = formatAddressWherePersonCanBeFound(recommendation.mainAddressWherePersonCanBeFound?.details),
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
      countersignAcoEmail = recommendation.countersignAcoEmail,
      counterSignSpoEmail = recommendation.countersignSpoEmail,
      countersignSpoName = recommendation.countersignSpoName,
      countersignSpoTelephone = recommendation.countersignSpoTelephone,
      countersignSpoDate = countersignSpoDate,
      countersignSpoTime = countersignSpoTime,
      countersignSpoExposition = recommendation.countersignSpoExposition,
      countersignAcoName = recommendation.countersignAcoName,
      countersignAcoTelephone = recommendation.countersignAcoTelephone,
      countersignAcoDate = countersignAcoDate,
      countersignAcoTime = countersignAcoTime,
      countersignAcoExposition = recommendation.countersignAcoExposition
    )
  }

  private fun buildPreviousReleasesList(previousReleases: PreviousReleases?): List<LocalDate>? {
    return previousReleases?.previousReleaseDates ?: emptyList()
  }

  private fun buildPreviousRecallsList(previousRecalls: PreviousRecalls?): List<LocalDate>? {
    var dates: List<LocalDate> = previousRecalls?.previousRecallDates ?: emptyList()
    if (previousRecalls?.lastRecallDate != null) {
      dates = listOf(previousRecalls.lastRecallDate) + dates
    }
    return dates
  }

  private fun findRecallTypeToDisplay(
    recallType: RecallType?,
    isIndeterminateSentence: Boolean?,
    isExtendedSentence: Boolean?
  ): ValueWithDetails {
    return if (isIndeterminateSentence == true || isExtendedSentence == true) {
      val textToDisplay = buildNotApplicableMessage(isIndeterminateSentence, isExtendedSentence, null)
      ValueWithDetails(textToDisplay, textToDisplay)
    } else {
      val partAValue = when (recallType?.selected?.value) {
        RecallTypeValue.STANDARD -> RecallTypeValue.STANDARD.displayValue
        RecallTypeValue.FIXED_TERM -> RecallTypeValue.FIXED_TERM.displayValue
        else -> null
      }
      ValueWithDetails(partAValue, recallType?.selected?.details)
    }
  }

  private fun additionalLicenceConditionsTextToDisplay(recommendation: RecommendationResponse): String? {
    val isStandardRecall: Boolean = recommendation.recallType?.selected?.value == RecallTypeValue.STANDARD
    return buildNotApplicableMessage(
      recommendation.isIndeterminateSentence,
      recommendation.isExtendedSentence,
      isStandardRecall
    )
      ?: if (recommendation.fixedTermAdditionalLicenceConditions?.selected == true) recommendation.fixedTermAdditionalLicenceConditions.details else EMPTY_STRING
  }

  private fun buildNotApplicableMessage(
    isIndeterminateSentence: Boolean?,
    isExtendedSentence: Boolean?,
    isStandardRecall: Boolean?
  ): String? {
    return if (isIndeterminateSentence == true) {
      "$NOT_APPLICABLE (not a determinate recall)"
    } else if (isExtendedSentence == true) {
      "$NOT_APPLICABLE (extended sentence recall)"
    } else if (isStandardRecall == true) {
      "$NOT_APPLICABLE (standard recall)"
    } else null
  }

  private fun convertBooleanToYesNo(value: Boolean?): String {
    return if (value == true) YES else if (value == false) NO else EMPTY_STRING
  }

  private fun buildAlternativeConditionsBreachedText(additionalLicenceConditions: AdditionalLicenceConditions?): String {

    val selectedOptions = if (additionalLicenceConditions?.selectedOptions != null) {
      additionalLicenceConditions.allOptions?.filter { sel ->
        additionalLicenceConditions.selectedOptions.any {
          it.subCatCode == sel.subCatCode && it.mainCatCode == sel.mainCatCode
        }
      }
    } else {
      // Left this line in to make the code backwards compatible after the issue described in MRD-1056 was fixed
      additionalLicenceConditions?.allOptions
        ?.filter { additionalLicenceConditions.selected?.contains(it.subCatCode) == true }
    }

    return selectedOptions?.foldIndexed(StringBuilder()) { index, builder, licenceCondition ->

      builder.append(licenceCondition.title)
      builder.append(System.lineSeparator())
      builder.append(licenceCondition.details)

      if (licenceCondition.note != null) {
        builder.append(System.lineSeparator())
        builder.append("Note: " + licenceCondition.note)
      }

      if (selectedOptions.size - 1 != index) {
        builder.append(System.lineSeparator())
        builder.append(System.lineSeparator())
      }
      builder
    }?.toString()
      ?: EMPTY_STRING
  }

  private fun buildFormattedLocalDate(dateToConvert: LocalDate?): String {
    return if (null != dateToConvert)
      convertLocalDateToDateWithSlashes(dateToConvert)
    else EMPTY_STRING
  }

  private fun getAddressDetails(addresses: List<Address>?): Pair<String, String> {
    val mainAddresses = addresses?.filter { !it.noFixedAbode }
    val noFixedAbodeAddresses = addresses?.filter { it.noFixedAbode }

    return if (mainAddresses?.isNotEmpty() == true && noFixedAbodeAddresses?.isEmpty() == true) {
      val addressConcat = mainAddresses.map {
        it.separatorFormattedAddress(", ")
      }
      Pair(addressConcat.joinToString("\n") { "$it" }, "")
    } else if (mainAddresses?.isEmpty() == true && noFixedAbodeAddresses?.isNotEmpty() == true) {
      Pair(
        "",
        "No fixed abode"
      )
    } else {
      Pair("", "")
    }
  }

  private fun buildLengthOfSentence(convictionDetail: ConvictionDetail?): String? {
    return convictionDetail?.let {
      val lengthOfSentence = it.lengthOfSentence?.toString() ?: EMPTY_STRING
      val lengthOfSentenceUnits = it.lengthOfSentenceUnits ?: EMPTY_STRING

      "$lengthOfSentence $lengthOfSentenceUnits"
    }
  }

  private fun getIndeterminateOrExtendedSentenceDetails(indeterminateOrExtendedSentenceDetails: IndeterminateOrExtendedSentenceDetails?, field: String): Pair<String, String?> {
    return if (indeterminateOrExtendedSentenceDetails != null) {
      if (indeterminateOrExtendedSentenceDetails.selected?.any { it.value == field } == true) {
        Pair(YES, indeterminateOrExtendedSentenceDetails.selected.filter { it.value == field }[0].details)
      } else {
        Pair(NO, EMPTY_STRING)
      }
    } else {
      Pair(EMPTY_STRING, EMPTY_STRING)
    }
  }

  private fun formatAddressWherePersonCanBeFound(details: String?): String? {
    return if (details?.isNullOrBlank() == false) "Police can find this person at: $details" else null
  }

  private fun formatMultipleDates(dates: List<LocalDate>?): String {
    if (dates != null) {
      return dates.joinToString(", ") { buildFormattedLocalDate(it) }
    }
    return EMPTY_STRING
  }
}
