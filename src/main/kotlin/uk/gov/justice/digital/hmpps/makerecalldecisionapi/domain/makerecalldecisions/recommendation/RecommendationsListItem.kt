package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class RecommendationsListItem(
  val recommendationId: Long? = null,
  val statusForRecallType: RecommendationStatusForRecallType? = null,
  val lastModifiedByName: String? = null,
  val createdDate: String? = null,
  val lastModifiedDate: String? = null,
)
