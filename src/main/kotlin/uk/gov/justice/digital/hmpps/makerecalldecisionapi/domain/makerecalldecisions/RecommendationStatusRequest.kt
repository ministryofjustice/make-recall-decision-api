package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper

data class RecommendationStatusRequest(
  val activate: List<String>,
  val deActivate: List<String>,
)

fun RecommendationStatusRequest.toActiveRecommendationStatusEntity(
  recommendationId: Long,
  userId: String?,
  createdByUserName: String?,
  email: String? = null,
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
        emailAddress = if (it == "ACO_SIGNED" || it == "SPO_SIGNED" || it == "PO_RECALL_CONSULT_SPO") email else null,
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
  val emailAddress: String? = null,
)
