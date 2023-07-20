package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

@Deprecated("This is used to support non-paged offender search.", level = DeprecationLevel.WARNING)
data class SearchByCrnResponse(
  val userExcluded: Boolean?,
  val userRestricted: Boolean?,
  val name: String?,
  val crn: String?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?
)