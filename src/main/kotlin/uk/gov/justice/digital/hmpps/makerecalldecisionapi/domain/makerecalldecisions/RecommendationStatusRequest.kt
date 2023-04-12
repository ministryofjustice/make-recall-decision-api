package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper

data class RecommendationStatusRequest(
  val activate: List<String>,
  val deActivate: List<String>
)

fun RecommendationStatusRequest.toActiveRecommendationStatusEntity(
  recommendationId: Long,
  userId: String?,
  createdByUserName: String?
): List<RecommendationStatusEntity> {
  return activate
    .map {
      RecommendationStatusEntity(
        recommendationId = recommendationId,
        createdBy = userId,
        createdByUserFullName = createdByUserName,
        created = DateTimeHelper.utcNowDateTimeString(),
        name = it,
        active = true
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
  val modifiedByUserFullName: String?
)
