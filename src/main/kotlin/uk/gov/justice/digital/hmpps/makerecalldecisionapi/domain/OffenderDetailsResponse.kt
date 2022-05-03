package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class OffenderDetailsResponse(
  val content: List<Content>
)

data class Content(
  val firstName: String,
  val surname: String,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate
)
