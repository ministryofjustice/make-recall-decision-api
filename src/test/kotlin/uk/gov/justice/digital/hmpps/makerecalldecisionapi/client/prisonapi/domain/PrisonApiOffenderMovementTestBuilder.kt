package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain

import org.assertj.core.api.Assertions.assertThat
import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.OffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Helper functions for generating instances of OffenderMovement with their
 * fields pre-filled with random values. Intended for use in unit tests.
 */

internal fun prisonApiOffenderMovement(
  offenderNo: String = randomString(),
  createDateTime: LocalDateTime = randomLocalDateTime(),
  fromAgency: String = randomString(),
  fromAgencyDescription: String = randomString(),
  toAgency: String = randomString(),
  toAgencyDescription: String = randomString(),
  fromCity: String = randomString(),
  toCity: String = randomString(),
  movementType: String = randomString(),
  movementTypeDescription: String = randomString(),
  directionCode: String = randomString(),
  movementDate: LocalDate = randomLocalDate(),
  movementTime: LocalTime = randomLocalTime(),
  movementReason: String = randomString(),
  movementReasonCode: String = randomString(),
  commentText: String = randomString(),
) = PrisonApiOffenderMovement(
  offenderNo,
  createDateTime,
  fromAgency,
  fromAgencyDescription,
  toAgency,
  toAgencyDescription,
  fromCity,
  toCity,
  movementType,
  movementTypeDescription,
  directionCode,
  movementDate,
  movementTime,
  movementReason,
  movementReasonCode,
  commentText,
)

internal fun PrisonApiOffenderMovement.toJson() = json(toJsonString())

internal fun PrisonApiOffenderMovement.toJsonString(): String =
  ResourceLoader.CustomMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)

fun assertMovementsAreEqual(
  movement: OffenderMovement,
  prisonApiMovement: PrisonApiOffenderMovement,
) {
  assertThat(movement).isNotNull
  assertThat(movement).usingRecursiveComparison()
    .ignoringFields("nomisId", "movementDateTime")
    .isEqualTo(prisonApiMovement)
  assertThat(movement.nomisId).isEqualTo(prisonApiMovement.offenderNo)
  assertThat(movement.movementDateTime.toLocalDate()).isEqualTo(prisonApiMovement.movementDate)
  assertThat(movement.movementDateTime.toLocalTime()).isEqualTo(prisonApiMovement.movementTime)
}
