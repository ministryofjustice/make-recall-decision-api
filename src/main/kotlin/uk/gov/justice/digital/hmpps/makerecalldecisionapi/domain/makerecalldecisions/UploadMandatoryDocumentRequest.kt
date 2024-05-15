package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class UploadMandatoryDocumentRequest(
  val id: Long,
  val category: String,
)
