package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity

class RecommendationToPartADataMapper {

  companion object Mapper {
    fun mapRecommendationDataToPartAData(recommendation: RecommendationEntity): PartAData {
      val custodyStatus = findCustodyStatusToDisplay(recommendation.data.custodyStatus)
      return PartAData(custodyStatus)
    }

    private fun findCustodyStatusToDisplay(custodyStatus: CustodyStatus?): String {
      return when (custodyStatus?.value) {
        CustodyStatusValue.YES_POLICE -> CustodyStatusValue.YES_POLICE.partADisplayValue
        CustodyStatusValue.YES_PRISON -> CustodyStatusValue.YES_PRISON.partADisplayValue
        else -> CustodyStatusValue.NO.partADisplayValue
      }
    }
  }
}
