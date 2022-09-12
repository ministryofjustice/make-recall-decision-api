package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class IndeterminateSentenceType(
  val selected: IndeterminateSentenceTypeOptions? = null,
  val allOptions: List<TextValueOption>? = null
)

enum class IndeterminateSentenceTypeOptions(val partADisplayValue: String) {
  LIFE("Life sentence"),
  IPP("Imprisonment for Public Protection (IPP) sentence"),
  DPP("Detention for Public Protection (DPP) sentence"),
  NO("No")
}
