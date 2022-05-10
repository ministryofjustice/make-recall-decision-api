package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class CaseSummaryOverviewResponse(
  val personDetails: PersonDetails?,
  val offences: List<Offence>?
)

data class PersonDetails(
  val name: String?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val age: Int?,
  val gender: String?,
  val crn: String?
)

data class ProbationTeam(
  val code: String?,
  val label: String?
)

data class Offence(
  val mainOffence: Boolean?,
  val description: String?
)
