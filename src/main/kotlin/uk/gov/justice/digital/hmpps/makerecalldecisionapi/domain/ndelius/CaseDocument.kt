package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import java.time.ZonedDateTime

data class CaseDocument(
  val id: String?,
  val documentName: String?,
  val lastModifiedAt: ZonedDateTime?
)
