package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

data class OffenderSearchByPhraseRequest(
  val matchAllTerms: Boolean? = false,
  val phrase: String,
  val probationAreasFilter: List<String>

)
