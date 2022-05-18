package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import java.time.OffsetDateTime

data class ContactSummaryResponse(
  val contactStartDate: OffsetDateTime?,
  val descriptionType: String?,
  val outcome: String? = null,
  val notes: String? = null,
  val enforcementAction: String? = null,
  val systemGenerated: Boolean?
)
