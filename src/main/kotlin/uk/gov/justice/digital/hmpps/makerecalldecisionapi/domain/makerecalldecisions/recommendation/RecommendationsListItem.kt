package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class RecommendationsListItem(
  val statusForRecallType: RecommendationStatusForRecallType? = null,
  val lastModifiedBy: String? = null,
  val createdDate: String? = null,
  val lastModifiedDate: String? = null,
)
