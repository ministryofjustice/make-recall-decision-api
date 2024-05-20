package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.util.*

data class PpudUploadMandatoryDocumentRequest(
  val documentId: UUID,
  val category: DocumentCategory,
  val owningCaseworker: PpudUser,
)
