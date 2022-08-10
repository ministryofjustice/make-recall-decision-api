package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class RecallType(
  val selected: RecallTypeSelectedValue? = null,
  val allOptions: List<TextValueOption>? = null
)

data class RecallTypeSelectedValue(
  val value: RecallTypeValue? = null,
  val details: String? = null
)

enum class RecallTypeValue(val displayValue: String) {
  STANDARD("Standard"),
  FIXED_TERM("Fixed"),
  NO_RECALL("No recall")
}
