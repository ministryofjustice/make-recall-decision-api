package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DntrData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions.OUT_OF_TOUCH
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToDateWithSlashes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToReadableDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.splitDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NOT_APPLICABLE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.WHITE_SPACE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.YES
import java.time.LocalDate

class RecommendationToDocumentMapper {

  companion object Mapper {

    fun mapRecommendationDataToDNTRDocumentData(recommendation: RecommendationEntity): DntrData {
      return DntrData(
        whyConsideredRecall = getDisplayTextFromWhyConsideredRecall(recommendation.data.whyConsideredRecall)
      )
    }

    private fun getDisplayTextFromWhyConsideredRecall(whyConsideredRecall: WhyConsideredRecall?): String {
      return whyConsideredRecall?.allOptions
        ?.associate { it.value to it.text }
        ?.get(whyConsideredRecall.selected?.name) ?: EMPTY_STRING
    }

    fun mapRecommendationDataToPartAData(recommendation: RecommendationEntity): PartAData {
      val firstName = recommendation.data.personOnProbation?.firstName
      val middleNames = recommendation.data.personOnProbation?.middleNames
      val lastName = recommendation.data.personOnProbation?.surname
      val (custodialTerm, extendedTerm) = extendedSentenceDetails(recommendation.data.convictionDetail)
      val (lastRecordedAddress, noFixedAbode) = getAddressDetails(recommendation.data.personOnProbation?.addresses)
      val (lastDownloadDate, lastDownloadTime) = splitDateTime(recommendation.data.lastPartADownloadDateTime)
      val (behaviourSimilarToIndexOffencePresent, behaviourSimilarToIndexOffence) = getIndeterminateOrExtendedSentenceDetails(recommendation.data.indeterminateOrExtendedSentenceDetails, BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE.name)
      val (behaviourLeadingToSexualOrViolentOffencePresent, behaviourLeadingToSexualOrViolentOffence) = getIndeterminateOrExtendedSentenceDetails(recommendation.data.indeterminateOrExtendedSentenceDetails, BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE.name)
      val (outOfTouchPresent, outOfTouch) = getIndeterminateOrExtendedSentenceDetails(recommendation.data.indeterminateOrExtendedSentenceDetails, OUT_OF_TOUCH.name)

      return PartAData(
        custodyStatus = ValueWithDetails(
          recommendation.data.custodyStatus?.selected?.partADisplayValue ?: EMPTY_STRING,
          recommendation.data.custodyStatus?.details
        ),
        recallType = findRecallTypeToDisplay(
          recommendation.data.recallType,
          recommendation.data.isIndeterminateSentence,
          recommendation.data.isExtendedSentence
        ),
        responseToProbation = recommendation.data.responseToProbation,
        whatLedToRecall = recommendation.data.whatLedToRecall,
        isThisAnEmergencyRecall = convertBooleanToYesNo(recommendation.data.isThisAnEmergencyRecall),
        isExtendedSentence = convertBooleanToYesNo(recommendation.data.isExtendedSentence),
        hasVictimsInContactScheme = recommendation.data.hasVictimsInContactScheme?.selected?.partADisplayValue
          ?: EMPTY_STRING,
        indeterminateSentenceType = recommendation.data.indeterminateSentenceType?.selected?.partADisplayValue
          ?: EMPTY_STRING,
        dateVloInformed = convertLocalDateToReadableDate(recommendation.data.dateVloInformed),
        selectedAlternatives = recommendation.data.alternativesToRecallTried?.selected,
        hasArrestIssues = ValueWithDetails(
          convertBooleanToYesNo(recommendation.data.hasArrestIssues?.selected),
          recommendation.data.hasArrestIssues?.details
        ),
        hasContrabandRisk = ValueWithDetails(
          convertBooleanToYesNo(recommendation.data.hasContrabandRisk?.selected),
          recommendation.data.hasContrabandRisk?.details
        ),
        selectedStandardConditionsBreached = recommendation.data.licenceConditionsBreached?.standardLicenceConditions?.selected,
        additionalConditionsBreached = buildAlternativeConditionsBreachedText(recommendation.data.licenceConditionsBreached?.additionalLicenceConditions),
        isUnderIntegratedOffenderManagement = recommendation.data.underIntegratedOffenderManagement?.selected,
        localPoliceContact = recommendation.data.localPoliceContact,
        vulnerabilities = recommendation.data.vulnerabilities,
        gender = recommendation.data.personOnProbation?.gender,
        dateOfBirth = recommendation.data.personOnProbation?.dateOfBirth,
        name = formatFullName(firstName, middleNames, lastName),
        ethnicity = recommendation.data.personOnProbation?.ethnicity,
        croNumber = recommendation.data.personOnProbation?.croNumber,
        pncNumber = recommendation.data.personOnProbation?.pncNumber,
        mostRecentPrisonerNumber = recommendation.data.personOnProbation?.mostRecentPrisonerNumber,
        nomsNumber = recommendation.data.personOnProbation?.nomsNumber,
        indexOffenceDescription = recommendation.data.convictionDetail?.indexOffenceDescription,
        dateOfOriginalOffence = buildFormattedLocalDate(recommendation.data.convictionDetail?.dateOfOriginalOffence),
        dateOfSentence = buildFormattedLocalDate(recommendation.data.convictionDetail?.dateOfSentence),
        lengthOfSentence = buildLengthOfSentence(recommendation.data.convictionDetail),
        licenceExpiryDate = buildFormattedLocalDate(recommendation.data.convictionDetail?.licenceExpiryDate),
        sentenceExpiryDate = buildFormattedLocalDate(recommendation.data.convictionDetail?.sentenceExpiryDate),
        custodialTerm = custodialTerm,
        extendedTerm = extendedTerm,
        mappa = recommendation.data.personOnProbation?.mappa,
        lastRecordedAddress = lastRecordedAddress,
        noFixedAbode = noFixedAbode,
        lastPersonCompletingFormName = recommendation.data.userNamePartACompletedBy,
        lastPersonCompletingFormEmail = recommendation.data.userEmailPartACompletedBy,
        region = recommendation.data.region,
        localDeliveryUnit = recommendation.data.localDeliveryUnit,
        dateOfDecision = lastDownloadDate,
        timeOfDecision = lastDownloadTime,
        indexOffenceDetails = recommendation.data.indexOffenceDetails,
        fixedTermAdditionalLicenceConditions = additionalLicenceConditionsTextToDisplay(recommendation),
        behaviourSimilarToIndexOffence = behaviourSimilarToIndexOffence,
        behaviourSimilarToIndexOffencePresent = behaviourSimilarToIndexOffencePresent,
        behaviourLeadingToSexualOrViolentOffence = behaviourLeadingToSexualOrViolentOffence,
        behaviourLeadingToSexualOrViolentOffencePresent = behaviourLeadingToSexualOrViolentOffencePresent,
        outOfTouch = outOfTouch,
        outOfTouchPresent = outOfTouchPresent,
        otherPossibleAddresses = formatAddressWherePersonCanBeFound(recommendation.data.mainAddressWherePersonCanBeFound?.details)
      )
    }

    private fun formatFullName(firstName: String?, middleNames: String?, surname: String?): String? {
      val formattedField = if (firstName.isNullOrBlank()) {
        surname
      } else if (middleNames.isNullOrBlank()) {
        "$firstName $surname"
      } else "$firstName $middleNames $surname"
      return formattedField
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

    private fun additionalLicenceConditionsTextToDisplay(recommendation: RecommendationEntity): String? {
      val isStandardRecall: Boolean = recommendation.data.recallType?.selected?.value == RecallTypeValue.STANDARD
      return buildNotApplicableMessage(
        recommendation.data.isIndeterminateSentence,
        recommendation.data.isExtendedSentence,
        isStandardRecall
      )
        ?: if (recommendation.data.fixedTermAdditionalLicenceConditions?.selected == true) recommendation.data.fixedTermAdditionalLicenceConditions?.details else EMPTY_STRING
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
      val selectedOptions = additionalLicenceConditions?.allOptions
        ?.filter { additionalLicenceConditions.selected?.contains(it.subCatCode) == true }

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

    private fun extendedSentenceDetails(convictionDetail: ConvictionDetail?): Pair<String?, String?> {
      return if (convictionDetail?.sentenceDescription.equals("Extended Determinate Sentence") ||
        convictionDetail?.sentenceDescription.equals("CJA - Extended Sentence")
      ) {
        val lengthOfSentence = buildLengthOfSentence(convictionDetail)
        val sentenceSecondLength = convictionDetail?.sentenceSecondLength?.toString() ?: EMPTY_STRING
        val sentenceSecondLengthUnits = convictionDetail?.sentenceSecondLengthUnits ?: EMPTY_STRING

        Pair(lengthOfSentence, sentenceSecondLength + WHITE_SPACE + sentenceSecondLengthUnits)
      } else Pair(null, null)
    }

    private fun getAddressDetails(addresses: List<Address>?): Pair<String, String> {
      val mainAddresses = addresses?.filter { !it.noFixedAbode }
      val noFixedAbodeAddresses = addresses?.filter { it.noFixedAbode }

      return if (mainAddresses?.isNotEmpty() == true && noFixedAbodeAddresses?.isEmpty() == true) {
        val addressConcat = mainAddresses.map {
          it.commaFormattedAddress()
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
      if (indeterminateOrExtendedSentenceDetails?.selected?.any { it.value == field } == true) {
        return Pair(YES, indeterminateOrExtendedSentenceDetails.selected.filter { it.value == field }[0].details)
      }
      return Pair(NO, EMPTY_STRING)
    }

    private fun formatAddressWherePersonCanBeFound(details: String?): String? {
      return if (details?.isNullOrBlank() == false) "Police can find this person at: $details" else null
    }
  }
}
