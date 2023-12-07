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
  val reasonInToPrison: String? = null,
  val dateInToPrison: LocalDateTime? = null,
  val inwardType: String? = null,
  val reasonOutOfPrison: String? = null,
  val dateOutOfPrison: LocalDateTime? = null,
  val outwardType: String? = null,
  val admittedIntoPrisonId: String? = null,
  val releaseFromPrisonId: String? = null,
)
