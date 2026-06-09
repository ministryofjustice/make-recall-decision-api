package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.io.Serializable

data class IndeterminateSentenceType(
  val selected: IndeterminateSentenceTypeOptions? = null,
  val allOptions: List<TextValueOption>? = null,
) : Serializable

enum class IndeterminateSentenceTypeOptions(val partADisplayValue: String) {
  LIFE("Yes - Lifer"),
  IPP("Yes - IPP"),
  DPP("Yes - DPP"),
  DHMP("Yes - DHMP"),
  NO("No"),
}
