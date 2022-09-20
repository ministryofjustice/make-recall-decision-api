package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class WhyConsideredRecall(
  val selected: WhyConsideredRecallValue? = null,
  val allOptions: List<TextValueOption>? = null
)

enum class WhyConsideredRecallValue(val displayValue: String) {
  RISK_INCREASED("Your risk is assessed as increased"),
  CONTACT_STOPPED("Contact with your probation practitioner has broken down"),
  RISK_INCREASED_AND_CONTACT_STOPPED("Your risk is assessed as increased and contact with your probation practitioner has broken down")
}
