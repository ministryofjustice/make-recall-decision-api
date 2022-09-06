package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToReadableDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.YES

class RecommendationToPartADataMapper {

  companion object Mapper {
    fun mapRecommendationDataToPartAData(recommendation: RecommendationEntity): PartAData {
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
        gender = recommendation.data.personOnProbation?.gender
      )
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
      return if (value == true) YES else NO
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
  }
}
