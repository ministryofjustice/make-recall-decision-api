package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class LastRecall(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val date: LocalDate?,
  val notes: String? = null,
  val reason: Reason? = null,
)
