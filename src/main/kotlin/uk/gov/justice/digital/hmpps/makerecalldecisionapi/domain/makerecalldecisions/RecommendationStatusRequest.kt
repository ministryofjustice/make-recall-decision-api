package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper

data class RecommendationStatusRequest(
  val activate: String? = null, // TODO change to array
  val deActivate: String? = null // TODO change to array
)

fun RecommendationStatusRequest.toActiveRecommendationStatusEntity(recommendationId: Long, userId: String?, createdByUserName: String?): RecommendationStatusEntity {
  return RecommendationStatusEntity(
    recommendationId = recommendationId,
    createdBy = userId,
    createdByUserFullName = createdByUserName,
    created = DateTimeHelper.utcNowDateTimeString(),
    name = activate,
    active = true
  )
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
  val modifiedByUserFullName: String?
)
