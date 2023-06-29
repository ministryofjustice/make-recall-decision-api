package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper

data class RecommendationStatusRequest(
  val activate: List<String>,
  val deActivate: List<String>
)

fun RecommendationStatusRequest.toActiveRecommendationStatusEntity(
  recommendationId: Long,
  userId: String?,
  createdByUserName: String?,
  recommendationHistoryId: Long? = null
): List<RecommendationStatusEntity> {
  return activate
    .map {
      RecommendationStatusEntity(
        recommendationId = recommendationId,
        createdBy = userId,
        createdByUserFullName = createdByUserName,
        created = DateTimeHelper.utcNowDateTimeString(),
        name = it,
        active = true,
        recommendationHistoryId = recommendationHistoryId
      )
    }
}

data class RecommendationStatusResponse(
  val name: String? = null,
  val active: Boolean,
  var recommendationId: Long?,
  var createdBy: String?,
  var created: String?,
  var modifiedBy: String?,
  var modified: String?,
  val createdByUserFullName: String?,
  val modifiedByUserFullName: String?,
  val recommendationHistoryId: Long?
) {
  companion object {
    fun fromRecommendationModel(model: RecommendationModel?, recommendationId: Long): RecommendationStatusResponse {
      return RecommendationStatusResponse(
        name = model?.status?.name,
        recommendationId = recommendationId,
        active = true,
        created = model?.createdDate,
        createdBy = model?.createdBy,
        createdByUserFullName = null,
        modified = null,
        modifiedBy = null,
        modifiedByUserFullName = null,
        recommendationHistoryId = null
      )
    }
  }
}
