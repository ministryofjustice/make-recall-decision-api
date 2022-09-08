package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToDateWithSlashes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToReadableDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.YES
import java.time.LocalDate

class RecommendationToPartADataMapper {

  companion object Mapper {

    fun mapRecommendationDataToPartAData(recommendation: RecommendationEntity): PartAData {
      val firstName = recommendation.data.personOnProbation?.firstName
      val middleNames = recommendation.data.personOnProbation?.middleNames
      val lastName = recommendation.data.personOnProbation?.surname
      val (custodialTerm, extendedTerm) = extendedSentenceDetails(recommendation.data.convictionDetail)

      return PartAData(
        custodyStatus = ValueWithDetails(recommendation.data.custodyStatus?.selected?.partADisplayValue ?: EMPTY_STRING, recommendation.data.custodyStatus?.details),
        recallType = findRecallTypeToDisplay(recommendation.data.recallType),
        responseToProbation = recommendation.data.responseToProbation,
        whatLedToRecall = recommendation.data.whatLedToRecall,
        isThisAnEmergencyRecall = convertBooleanToYesNo(recommendation.data.isThisAnEmergencyRecall),
        hasVictimsInContactScheme = recommendation.data.hasVictimsInContactScheme?.selected?.partADisplayValue ?: EMPTY_STRING,
        dateVloInformed = convertLocalDateToReadableDate(recommendation.data.dateVloInformed),
        selectedAlternatives = recommendation.data.alternativesToRecallTried?.selected,
        hasArrestIssues = ValueWithDetails(convertBooleanToYesNo(recommendation.data.hasArrestIssues?.selected), recommendation.data.hasArrestIssues?.details),
        hasContrabandRisk = ValueWithDetails(convertBooleanToYesNo(recommendation.data.hasContrabandRisk?.selected), recommendation.data.hasContrabandRisk?.details),
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
        lengthOfSentence = recommendation.data.convictionDetail?.lengthOfSentence.toString() + " " + recommendation.data.convictionDetail?.lengthOfSentenceUnits,
        licenceExpiryDate = buildFormattedLocalDate(recommendation.data.convictionDetail?.licenceExpiryDate),
        sentenceExpiryDate = buildFormattedLocalDate(recommendation.data.convictionDetail?.sentenceExpiryDate),
        custodialTerm = custodialTerm,
        extendedTerm = extendedTerm,
        mappaCategory = recommendation.data.personOnProbation?.mappaCategory,
        mappaLevel = recommendation.data.personOnProbation?.mappaLevel
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

    private fun findRecallTypeToDisplay(recallType: RecallType?): ValueWithDetails {
      val partAValue = when (recallType?.selected?.value) {
        RecallTypeValue.STANDARD -> RecallTypeValue.STANDARD.displayValue
        RecallTypeValue.FIXED_TERM -> RecallTypeValue.FIXED_TERM.displayValue
        else -> null
      }

      return ValueWithDetails(partAValue, recallType?.selected?.details)
    }

    private fun convertBooleanToYesNo(value: Boolean?): String {
      if (value == true) return YES else if (value == false) return NO else return EMPTY_STRING
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

    private fun extendedSentenceDetails(convictionDetail: ConvictionDetail?): Pair<String, String> {
      return if (convictionDetail?.sentenceDescription.equals("Extended Determinate Sentence") ||
        convictionDetail?.sentenceDescription.equals("CJA - Extended Sentence")
      ) {
        Pair(
          convictionDetail?.lengthOfSentence.toString() + " " + convictionDetail?.lengthOfSentenceUnits,
          convictionDetail?.sentenceSecondLength.toString() + " " + convictionDetail?.sentenceSecondLengthUnits
        )
      } else Pair("", "")
    }
  }
}
