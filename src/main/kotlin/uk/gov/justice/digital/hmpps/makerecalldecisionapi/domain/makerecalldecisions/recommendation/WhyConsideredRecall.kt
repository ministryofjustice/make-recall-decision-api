package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class WhyConsideredRecall(
  val selected: WhyConsideredRecallValue? = null,
  val allOptions: List<TextValueOption>? = null,
)

enum class WhyConsideredRecallValue() {
  RISK_INCREASED,
  CONTACT_STOPPED,
  RISK_INCREASED_AND_CONTACT_STOPPED,
}
