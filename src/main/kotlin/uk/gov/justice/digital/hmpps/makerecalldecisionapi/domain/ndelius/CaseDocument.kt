package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class CaseDocument(
  val id: String?,
  val documentName: String?,
  val author: String?,
  val type: CaseDocumentType?,
  val extendedDescription: String?,
  val lastModifiedAt: String?,
  val createdAt: String?,
  val parentPrimaryKeyId: Long?,
)
