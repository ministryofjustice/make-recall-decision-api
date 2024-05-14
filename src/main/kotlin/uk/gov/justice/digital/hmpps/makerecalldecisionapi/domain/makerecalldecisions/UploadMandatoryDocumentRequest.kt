package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class UploadMandatoryDocumentRequest(
  val id: String,
  val category: String,
)
