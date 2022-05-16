package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.OffsetDateTime

data class ContactSummaryResponse(
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
  val contactStartDate: OffsetDateTime?,
  val descriptionType: String?,
  val outcome: String? = null,
  val notes: String? = null,
  val enforcementAction: String? = null,
  val systemGenerated: Boolean?
)
