package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypePartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity

class RecommendationToPartADataMapper {

  companion object Mapper {
    fun mapRecommendationDataToPartAData(recommendation: RecommendationEntity): PartAData {
      val custodyStatus = findCustodyStatusToDisplay(recommendation.data.custodyStatus)
      val recallType = findRecallTypeToDisplay(recommendation.data.recallType)
      val isThisAnEmergencyRecall = convertBooleanToYesNo(recommendation.data.isThisAnEmergencyRecall)

      return PartAData(custodyStatus, recallType, recommendation.data.responseToProbation, isThisAnEmergencyRecall)
    }

    private fun findCustodyStatusToDisplay(custodyStatus: CustodyStatus?): String {
      return when (custodyStatus?.selected) {
        CustodyStatusValue.YES_POLICE -> CustodyStatusValue.YES_POLICE.partADisplayValue
        CustodyStatusValue.YES_PRISON -> CustodyStatusValue.YES_PRISON.partADisplayValue
        else -> CustodyStatusValue.NO.partADisplayValue
      }
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
      return if (value == true) "Yes" else "No"
    }
  }
}
