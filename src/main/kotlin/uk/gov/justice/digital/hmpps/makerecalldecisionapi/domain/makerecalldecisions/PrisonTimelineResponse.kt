package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.time.LocalDateTime

data class PrisonTimelineResponse(
  val prisonerNumber: String,
  val prisonPeriod: List<PrisonPeriod>,
)

data class PrisonPeriod(
  val bookNumber: String = "",
  val bookingId: Int,
  val entryDate: LocalDateTime? = null,
  val releaseDate: LocalDateTime? = null,
  val movementDates: List<Movement> = listOf(),
  val prisons: List<String> = listOf(),
)

data class Movement(
  val reasonInToPrison: String?,
  val dateInToPrison: LocalDateTime?,
  val inwardType: String?,
  val reasonOutOfPrison: String?,
  val dateOutOfPrison: LocalDateTime?,
  val outwardType: String?,
  val admittedIntoPrisonId: String?,
  val releaseFromPrisonId: String?,
)
