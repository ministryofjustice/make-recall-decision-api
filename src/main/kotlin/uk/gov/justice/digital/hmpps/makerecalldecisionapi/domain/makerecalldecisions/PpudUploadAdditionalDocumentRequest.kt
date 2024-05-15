package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.util.*

data class PpudUploadAdditionalDocumentRequest(
  val documentId: UUID,
  val title: String,
  val owningCaseworker: PpudUser,
)
