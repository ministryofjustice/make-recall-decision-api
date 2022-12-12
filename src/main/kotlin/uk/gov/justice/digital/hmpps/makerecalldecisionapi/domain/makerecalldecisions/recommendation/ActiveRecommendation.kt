package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class ActiveRecommendation(
  val recommendationId: Long?,
  val lastModifiedDate: String?,
  val lastModifiedBy: String?,
  val recallType: RecallType?,
  val recallConsideredList: List<RecallConsidered>? = null
)
