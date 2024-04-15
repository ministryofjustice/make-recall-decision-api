package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class SupportingDocumentMetaDataResponse(
  val id: Long,
  val recommendationId: Long?,
  var createdBy: String?,
  var createdByUserFullName: String?,
  var created: String?,
  var filename: String?,
  var type: String?,
  var uploadedBy: String?,
  var uploadedByUserFullName: String?,
  var uploaded: String?,
)
