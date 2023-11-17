package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.OUT_OF_TOUCH
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionSection
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PractitionerDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationMetaData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RegionService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToDateWithSlashes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToReadableDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.splitDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NOT_APPLICABLE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.YES
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
    val (outOfTouchPresent, outOfTouch) = getIndeterminateOrExtendedSentenceDetails(
      recommendation.indeterminateOrExtendedSentenceDetails,
      OUT_OF_TOUCH.name,
    )
    val (countersignSpoDate, countersignSpoTime) = splitDateTime(metadata.countersignSpoDateTime)
    val (countersignAcoDate, countersignAcoTime) = splitDateTime(metadata.countersignAcoDateTime)
    val lastRelease = recommendation.previousReleases?.lastReleaseDate
    val previousReleasesList = buildPreviousReleasesList(recommendation.previousReleases)
    val previousRecallsList = buildPreviousRecallsList(recommendation.previousRecalls)

    return DocumentData(
      custodyStatus = ValueWithDetails(
        recommendation.custodyStatus?.selected?.partADisplayValue ?: EMPTY_STRING,
        recommendation.custodyStatus?.details,
      ),
      recallType = findRecallTypeToDisplay(
        recommendation.recallType,
        recommendation.isIndeterminateSentence,
        recommendation.isExtendedSentence,
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
      additionalConditionsBreached = buildAlternativeConditionsBreachedText(
        recommendation.licenceConditionsBreached?.additionalLicenceConditions,
        recommendation.cvlLicenceConditionsBreached?.additionalLicenceConditions,
        recommendation.cvlLicenceConditionsBreached?.bespokeLicenceConditions,
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
      supervisingPractitioner = determineSupervisingPractitioner(recommendation, flags),
      revocationOrderRecipients = recommendation.revocationOrderRecipients ?: emptyList(),
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
    if (flags.flagProbationAdmin) {
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
    } else {
      return PractitionerDetails(
        name = metadata.userNamePartACompletedBy ?: "",
        telephone = "",
        email = metadata.userEmailPartACompletedBy ?: "",
        region = recommendation.region ?: "",
        localDeliveryUnit = recommendation.localDeliveryUnit ?: "",
      )
    }
  }

  private suspend fun determineSupervisingPractitioner(
    recommendation: RecommendationResponse,
    flags: FeatureFlags,
  ): PractitionerDetails {
    return if (flags.flagProbationAdmin && recommendation.whoCompletedPartA?.isPersonProbationPractitionerForOffender != true) {
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
  }

  private fun buildPreviousReleasesList(previousReleases: PreviousReleases?): List<LocalDate> {
    return previousReleases?.previousReleaseDates ?: emptyList()
  }

  private fun buildPreviousRecallsList(previousRecalls: PreviousRecalls?): List<LocalDate> {
    var dates: List<LocalDate> = previousRecalls?.previousRecallDates ?: emptyList()
    if (previousRecalls?.lastRecallDate != null) {
      dates = listOf(previousRecalls.lastRecallDate) + dates
    }
    return dates
  }

  private fun findRecallTypeToDisplay(
    recallType: RecallType?,
    isIndeterminateSentence: Boolean?,
    isExtendedSentence: Boolean?,
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
      isStandardRecall,
    )
      ?: if (recommendation.fixedTermAdditionalLicenceConditions?.selected == true) recommendation.fixedTermAdditionalLicenceConditions.details else EMPTY_STRING
  }

  private fun buildNotApplicableMessage(
    isIndeterminateSentence: Boolean?,
    isExtendedSentence: Boolean?,
    isStandardRecall: Boolean?,
  ): String? {
    return if (isIndeterminateSentence == true) {
      "$NOT_APPLICABLE (not a determinate recall)"
    } else if (isExtendedSentence == true) {
      "$NOT_APPLICABLE (extended sentence recall)"
    } else if (isStandardRecall == true) {
      "$NOT_APPLICABLE (standard recall)"
    } else {
      null
    }
  }

  private fun convertBooleanToYesNo(value: Boolean?): String {
    return if (value == true) YES else if (value == false) NO else EMPTY_STRING
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

  private fun buildAlternativeConditionsBreachedText(
    additionalLicenceConditions: AdditionalLicenceConditions?,
    cvlAdditionalLicenceConditions: LicenceConditionSection?,
    cvlBespokeLicenceConditions: LicenceConditionSection?,
  ): String {
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

    val builder = StringBuilder()

    cvlAdditionalLicenceConditions?.selected
      ?.map { code -> cvlAdditionalLicenceConditions.allOptions?.find { it.code == code }?.text }
      ?.fold(builder) { buffer, text ->
        buffer.append(text)
        builder.append(System.lineSeparator())
        builder.append(System.lineSeparator())
        buffer
      }

    cvlBespokeLicenceConditions?.selected
      ?.map { code -> cvlBespokeLicenceConditions.allOptions?.find { it.code == code }?.text }
      ?.fold(builder) { buffer, text ->
        buffer.append(text)
        builder.append(System.lineSeparator())
        builder.append(System.lineSeparator())
        buffer
      }

    return builder.toString().trim()
  }

  private fun buildFormattedLocalDate(dateToConvert: LocalDate?): String {
    return if (null != dateToConvert) {
      convertLocalDateToDateWithSlashes(dateToConvert)
    } else {
      EMPTY_STRING
    }
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

  private fun buildLengthOfSentence(convictionDetail: ConvictionDetail?): String? {
    return convictionDetail?.let {
      val lengthOfSentence = it.lengthOfSentence?.toString() ?: EMPTY_STRING
      val lengthOfSentenceUnits = it.lengthOfSentenceUnits ?: EMPTY_STRING

      "$lengthOfSentence $lengthOfSentenceUnits"
    }
  }

  private fun getIndeterminateOrExtendedSentenceDetails(
    indeterminateOrExtendedSentenceDetails: IndeterminateOrExtendedSentenceDetails?,
    field: String,
  ): Pair<String, String?> {
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
    return if (details?.isBlank() == false) "Police can find this person at: $details" else null
  }

  private fun formatMultipleDates(dates: List<LocalDate>?): String {
    if (dates != null) {
      return dates.joinToString(", ") { buildFormattedLocalDate(it) }
    }
    return EMPTY_STRING
  }
}
