package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import java.time.OffsetDateTime

data class Content(
  val contactId: Long?,
  val contactStart: OffsetDateTime?,
  val type: ContactType?,
  val outcome: ContactOutcome? = null,
  val notes: String? = null,
  val enforcement: EnforcementAction? = null,
  val sensitive: Boolean?,
  val description: String?,
)
