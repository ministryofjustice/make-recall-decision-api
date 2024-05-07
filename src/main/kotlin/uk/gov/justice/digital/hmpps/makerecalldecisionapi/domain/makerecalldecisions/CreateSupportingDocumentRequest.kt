package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class CreateSupportingDocumentRequest(
  val filename: String,
  val title: String,
  val type: String,
  val mimetype: String,
  val data: String,
)
