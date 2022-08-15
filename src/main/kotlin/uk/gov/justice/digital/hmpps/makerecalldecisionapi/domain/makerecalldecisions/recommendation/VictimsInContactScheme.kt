package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class VictimsInContactScheme(
  val selected: VictimsInContactSchemeValue? = null,
  val allOptions: List<TextValueOption>? = null
)

enum class VictimsInContactSchemeValue(val partADisplayValue: String) {
  YES("Yes"),
  NO("No"),
  NOT_APPLICABLE("N/A")
}
