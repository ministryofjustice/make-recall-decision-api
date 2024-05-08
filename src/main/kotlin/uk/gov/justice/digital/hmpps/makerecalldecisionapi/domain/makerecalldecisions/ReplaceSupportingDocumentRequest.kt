package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class ReplaceSupportingDocumentRequest(
  val title: String,
  val filename: String?,
  val mimetype: String?,
  val data: String?,
)
