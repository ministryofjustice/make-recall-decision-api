package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi

import java.time.OffsetDateTime

data class ContactSummaryResponseCommunity(
  val content: List<Content>?
)

data class Content(
  val contactStart: OffsetDateTime?,
  val type: ContactType?,
  val outcome: ContactOutcome? = null,
  val notes: String? = null,
  val enforcement: EnforcementAction? = null,
)

data class ContactType(
  val description: String?,
  val systemGenerated: Boolean,
)

data class ContactOutcome(
  val description: String?,
)

data class EnforcementAction(
  val enforcementAction: EnforcementActionType?,
)

data class EnforcementActionType(
  val description: String?,
)
