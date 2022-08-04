package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class CustodyStatus(
  val value: CustodyStatusValue? = null,
  val options: List<TextValueOption>? = null

)

enum class CustodyStatusValue(val partADisplayValue: String) {
  YES_POLICE("Police Custody"),
  YES_PRISON("Prison Custody"),
  NO("No")
}
