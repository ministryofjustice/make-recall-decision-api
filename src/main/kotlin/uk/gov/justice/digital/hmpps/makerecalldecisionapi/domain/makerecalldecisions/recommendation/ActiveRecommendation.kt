package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

data class ActiveRecommendation(
  val recommendationId: Long?,
  val lastModifiedDate: String?,
  val lastModifiedBy: String?,
  val lastModifiedByName: String?,
  val recallType: RecallType?,
  val recallConsideredList: List<RecallConsidered>? = null,
  val status: Status? = null,
  val managerRecallDecision: ManagerRecallDecision? = null,
)
