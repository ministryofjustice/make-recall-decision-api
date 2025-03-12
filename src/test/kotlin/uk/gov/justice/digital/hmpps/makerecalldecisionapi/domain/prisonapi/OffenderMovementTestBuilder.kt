package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDateTime

/**
 * Helper functions for generating instances of OffenderMovement with their
 * fields pre-filled with random values. Intended for use in unit tests.
 */

internal fun offenderMovement(
  offenderNo: String = randomString(),
  movementType: String = randomString(),
  movementTypeDescription: String = randomString(),
  fromAgency: String? = randomString(),
  fromAgencyDescription: String? = randomString(),
  toAgency: String? = randomString(),
  toAgencyDescription: String? = randomString(),
  movementDateTime: LocalDateTime = randomLocalDateTime(),
) = OffenderMovement(
  offenderNo,
  movementType,
  movementTypeDescription,
  fromAgency,
  fromAgencyDescription,
  toAgency,
  toAgencyDescription,
  movementDateTime,
)
