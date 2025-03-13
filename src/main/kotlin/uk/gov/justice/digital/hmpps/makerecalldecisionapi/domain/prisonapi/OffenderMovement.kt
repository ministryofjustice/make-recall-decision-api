package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi

import java.time.LocalDateTime

data class OffenderMovement(
  val nomisId: String,
  val movementType: String,
  val movementTypeDescription: String,
  val fromAgency: String?,
  val fromAgencyDescription: String?,
  val toAgency: String?,
  val toAgencyDescription: String?,
  val movementDateTime: LocalDateTime,
)
