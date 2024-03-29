package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

data class RecommendationsListItem(
  val recommendationId: Long? = null,
  val lastModifiedByName: String? = null,
  val createdDate: String? = null,
  val lastModifiedDate: String? = null,
  val status: Status? = null,
  val statuses: List<RecommendationStatusEntity>? = emptyList(),
  val recallType: RecallType? = null,
  val deleted: Boolean = false,
)
