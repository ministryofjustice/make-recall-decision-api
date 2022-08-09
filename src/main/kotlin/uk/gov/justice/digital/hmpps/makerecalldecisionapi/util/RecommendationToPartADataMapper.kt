package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypePartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternative
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertLocalDateToReadableDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.YES

class RecommendationToPartADataMapper {

  companion object Mapper {
    fun mapRecommendationDataToPartAData(recommendation: RecommendationEntity): PartAData {
      return PartAData(
        custodyStatus = recommendation.data.custodyStatus?.selected?.partADisplayValue ?: EMPTY_STRING,
        recallType = findRecallTypeToDisplay(recommendation.data.recallType),
        responseToProbation = recommendation.data.responseToProbation,
        isThisAnEmergencyRecall = convertBooleanToYesNo(recommendation.data.isThisAnEmergencyRecall),
        hasVictimsInContactScheme = recommendation.data.hasVictimsInContactScheme?.selected?.partADisplayValue ?: EMPTY_STRING,
        dateVloInformed = convertLocalDateToReadableDate(recommendation.data.dateVloInformed),
        selectedAlternativesMap = convertToMap(recommendation.data.alternativesToRecallTried?.selected)
      )
    }

    private fun convertToMap(selectedAlternatives: List<SelectedAlternative>?): Map<String, String> {
      val selectedAlternativesMap = selectedAlternatives?.associate { it.value to it.details } ?: emptyMap()
      return mapOf(
        "warning_letter_details" to (selectedAlternativesMap["WARNINGS_LETTER"] ?: EMPTY_STRING),
        "drug_testing_details" to (selectedAlternativesMap["DRUG_TESTING"] ?: EMPTY_STRING),
        "increased_frequency_details" to (selectedAlternativesMap["INCREASED_FREQUENCY"] ?: EMPTY_STRING),
        "extra_licence_conditions_details" to (selectedAlternativesMap["EXTRA_LICENCE_CONDITIONS"] ?: EMPTY_STRING),
        "referral_to_approved_premises_details" to (selectedAlternativesMap["REFERRAL_TO_APPROVED_PREMISES"] ?: EMPTY_STRING),
        "referral_to_other_teams_details" to (selectedAlternativesMap["REFERRAL_TO_OTHER_TEAMS"] ?: EMPTY_STRING),
        "increased_frequency_details" to (selectedAlternativesMap["INCREASED_FREQUENCY"] ?: EMPTY_STRING),
        "referral_to_partnership_agencies_details" to (selectedAlternativesMap["REFERRAL_TO_PARTNERSHIP_AGENCIES"] ?: EMPTY_STRING),
        "risk_escalation_details" to (selectedAlternativesMap["RISK_ESCALATION"] ?: EMPTY_STRING),
        "alternative_to_recall_other_details" to (selectedAlternativesMap["ALTERNATIVE_TO_RECALL_OTHER"] ?: EMPTY_STRING)
      )
    }

    private fun findRecallTypeToDisplay(recallType: RecallType?): RecallTypePartA {
      val partAValue = when (recallType?.selected?.value) {
        RecallTypeValue.STANDARD -> RecallTypeValue.STANDARD.displayValue
        RecallTypeValue.FIXED_TERM -> RecallTypeValue.FIXED_TERM.displayValue
        else -> null
      }

      return RecallTypePartA(partAValue, recallType?.selected?.details)
    }

    private fun convertBooleanToYesNo(value: Boolean?): String {
      return if (value == true) YES else NO
    }
  }
}
