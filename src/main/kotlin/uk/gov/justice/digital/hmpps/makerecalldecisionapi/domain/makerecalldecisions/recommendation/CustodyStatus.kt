package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.io.Serializable

data class CustodyStatus(
  val selected: CustodyStatusValue? = null,
  val details: String? = null,
  val allOptions: List<TextValueOption>? = null,
) : Serializable

enum class CustodyStatusValue(val partADisplayValue: String) {
  YES_POLICE("Police Custody"),
  YES_PRISON("Prison Custody"),
  NO("No"),
}
