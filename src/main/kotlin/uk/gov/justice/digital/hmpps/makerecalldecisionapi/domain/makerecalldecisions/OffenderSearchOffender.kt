package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class OffenderSearchOffender(
  val name: String,
  val crn: String,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val userExcluded: Boolean,
  val userRestricted: Boolean,
)
