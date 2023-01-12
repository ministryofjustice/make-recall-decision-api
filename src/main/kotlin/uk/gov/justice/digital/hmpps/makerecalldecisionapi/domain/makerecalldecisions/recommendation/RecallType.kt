package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class ManagerRecallDecision(
  val selected: ManagerRecallDecisionTypeSelectedValue? = null,
  val allOptions: List<TextValueOption>? = null,
  var isSentToDelius: Boolean? = false,
  val createdBy: String? = null,
  val createdDate: String? = null
)

data class ManagerRecallDecisionTypeSelectedValue(
  val value: ManagerRecallDecisionTypeValue? = null,
  val details: String? = null
)

data class RecallType(
  val selected: RecallTypeSelectedValue? = null,
  val allOptions: List<TextValueOption>? = null
)

data class RecallTypeSelectedValue(
  val value: RecallTypeValue? = null,
  val details: String? = null
)

enum class ManagerRecallDecisionTypeValue(val displayValue: String) {
  NO_RECALL("Do not recall"),
  RECALL("Recall")
}

enum class RecallTypeValue(val displayValue: String) {
  STANDARD("Standard"),
  FIXED_TERM("Fixed"),
  NO_RECALL("No recall")
}
