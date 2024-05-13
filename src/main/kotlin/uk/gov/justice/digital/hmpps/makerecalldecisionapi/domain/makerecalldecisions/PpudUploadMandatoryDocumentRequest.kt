package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class PpudUploadMandatoryDocumentRequest(
  val id: String,
  val category: DocumentCategory,
  val owningCaseworker: PpudUser,
)
