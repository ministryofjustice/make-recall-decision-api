package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Movement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonPeriod
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDateTime

fun prisonTimelineResponse(
  prisonerNumber: String = randomString(),
  prisonPeriod: List<PrisonPeriod> = listOf(prisonPeriod()),
) = PrisonTimelineResponse(
  prisonerNumber = prisonerNumber,
  prisonPeriod = prisonPeriod,
)

fun prisonPeriod(
  bookNumber: String = randomString(),
  bookingId: Int = randomInt(),
  entryDate: LocalDateTime? = randomLocalDateTime(),
  releaseDate: LocalDateTime? = randomLocalDateTime(),
  movementDates: List<Movement> = listOf(movement()),
  prisons: List<String> = listOf(randomString()),
) = PrisonPeriod(
  bookNumber = bookNumber,
  bookingId = bookingId,
  entryDate = entryDate,
  releaseDate = releaseDate,
  movementDates = movementDates,
  prisons = prisons,
)

fun movement(
  reasonInToPrison: String? = randomString(),
  dateInToPrison: LocalDateTime? = randomLocalDateTime(),
  inwardType: String? = randomString(),
  reasonOutOfPrison: String? = randomString(),
  dateOutOfPrison: LocalDateTime? = randomLocalDateTime(),
  outwardType: String? = randomString(),
  admittedIntoPrisonId: String? = randomString(),
  releaseFromPrisonId: String? = randomString(),
) = Movement(
  reasonInToPrison = reasonInToPrison,
  dateInToPrison = dateInToPrison,
  inwardType = inwardType,
  reasonOutOfPrison = reasonOutOfPrison,
  dateOutOfPrison = dateOutOfPrison,
  outwardType = outwardType,
  admittedIntoPrisonId = admittedIntoPrisonId,
  releaseFromPrisonId = releaseFromPrisonId,
)
