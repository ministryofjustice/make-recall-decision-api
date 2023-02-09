package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDate

data class Offence(
  val mainOffence: Boolean?,
  val description: String?,
  val code: String?,
  val offenceDate: LocalDate?
)
