package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Registration(
  val registrationId: String? = null,
  val active: Boolean?,
  val register: CodeDescriptionItem? = null,
  val type: CodeDescriptionItem?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val startDate: LocalDate? = null,
  val notes: String? = null,
)

data class CodeDescriptionItem(
  val code: String?,
  val description: String?,
)
