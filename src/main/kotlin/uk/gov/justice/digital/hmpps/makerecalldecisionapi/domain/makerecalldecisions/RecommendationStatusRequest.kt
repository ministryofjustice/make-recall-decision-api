package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper

data class RecommendationStatusRequest(
  val activate: String? = null,
  val deActivate: String? = null
)

fun RecommendationStatusRequest.toActiveRecommendationStatusEntity(recommendationId: Long, userId: String?, createdByUserName: String?): RecommendationStatusEntity {
  return RecommendationStatusEntity(
    recommendationId = recommendationId,
    createdBy = userId,
    createdByUserName = createdByUserName,
    created = DateTimeHelper.utcNowDateTimeString(),
    status = activate,
    active = true
  )
}

data class RecommendationStatusResponse(
  val status: String? = null,
  val active: Boolean,
  var recommendationId: Long?,
  var createdBy: String?,
  var created: String?,
  var modifiedBy: String?,
  var modified: String?,
  val createdByUserName: String?,
  val modifiedByUserName: String?,
)
