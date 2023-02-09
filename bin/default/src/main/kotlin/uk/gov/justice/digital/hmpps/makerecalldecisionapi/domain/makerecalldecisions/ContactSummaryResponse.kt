package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import java.time.OffsetDateTime

data class ContactSummaryResponse(
  val contactStartDate: OffsetDateTime?,
  val descriptionType: String?,
  val code: String?,
  val outcome: String? = null,
  val notes: String? = null,
  val enforcementAction: String? = null,
  val systemGenerated: Boolean?,
  val sensitive: Boolean?,
  val contactDocuments: List<CaseDocument>?,
  val description: String?,
)
