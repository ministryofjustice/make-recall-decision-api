package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class SupportingDocumentResponse(
  val id: Long,
  val recommendationId: Long?,
  var filename: String?,
  var type: String?,
  var data: String?,
)
