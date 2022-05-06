package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi

import javax.validation.constraints.NotEmpty

data class OffenderSearchByPhraseRequest(
  @field:NotEmpty
  val phrase: String,
  val matchAllTerms: Boolean? = false
)
