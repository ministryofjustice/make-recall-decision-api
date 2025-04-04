package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class PrisonApiOffenderMovement(
  val offenderNo: String,
  val createDateTime: LocalDateTime,
  val fromAgency: String?,
  val fromAgencyDescription: String?,
  val toAgency: String?,
  val toAgencyDescription: String?,
  val fromCity: String?,
  val toCity: String?,
  val movementType: String,
  val movementTypeDescription: String,
  val directionCode: String,
  val movementDate: LocalDate,
  val movementTime: LocalTime,
  val movementReason: String,
  val movementReasonCode: String,
  val commentText: String?,
)
