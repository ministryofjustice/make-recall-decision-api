package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class ConvictionDocuments(
  val convictionId: String?,
  val documents: List<CaseDocument>?,
)
