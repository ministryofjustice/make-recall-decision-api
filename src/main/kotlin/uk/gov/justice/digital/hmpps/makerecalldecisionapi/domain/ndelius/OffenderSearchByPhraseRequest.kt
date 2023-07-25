package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class OffenderSearchByPhraseRequest(
  val phrase: String? = null,
  val firstName: String? = null,
  val surname: String? = null,
  val crn: String? = null,
  val matchAllTerms: Boolean? = false
)
