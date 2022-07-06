package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class GroupedDocuments(
  val documents: List<CaseDocument>?,
  val convictions: List<ConvictionDocuments>?
)
