package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.toJsonNullableStringField
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

internal fun PrisonApiOffenderMovement.toJsonString() =
  """
      {
        "offenderNo": ${toJsonNullableStringField(offenderNo)},
        "createDateTime": ${toJsonNullableStringField(createDateTime)},
        "fromAgency": ${toJsonNullableStringField(fromAgency)},
        "fromAgencyDescription": ${toJsonNullableStringField(fromAgencyDescription)},
        "toAgency": ${toJsonNullableStringField(toAgency)},
        "toAgencyDescription": ${toJsonNullableStringField(toAgencyDescription)},
        "fromCity": ${toJsonNullableStringField(fromCity)},
        "toCity": ${toJsonNullableStringField(toCity)},
        "movementType": ${toJsonNullableStringField(movementType)},
        "movementTypeDescription": ${toJsonNullableStringField(movementTypeDescription)},
        "directionCode": ${toJsonNullableStringField(directionCode)},
        "movementDate": ${toJsonNullableStringField(movementDate)},
        "movementTime": ${toJsonNullableStringField(movementTime)},
        "movementReason": ${toJsonNullableStringField(movementReason)},
        "movementReasonCode": ${toJsonNullableStringField(movementReasonCode)},
        "commentText": ${toJsonNullableStringField(commentText)}
      }
  """.trimIndent()
