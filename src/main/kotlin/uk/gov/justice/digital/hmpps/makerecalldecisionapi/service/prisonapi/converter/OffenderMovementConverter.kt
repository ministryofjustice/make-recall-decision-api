package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.PrisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.OffenderMovement
import java.time.LocalDateTime

@Service
class OffenderMovementConverter {

  fun convert(prisonApiOffenderMovements: List<PrisonApiOffenderMovement>): List<OffenderMovement> {
    return prisonApiOffenderMovements.map { convert(it) }
  }

  fun convert(prisonApiOffenderMovement: PrisonApiOffenderMovement): OffenderMovement {
    return OffenderMovement(
      offenderNo = prisonApiOffenderMovement.offenderNo,
      movementType = prisonApiOffenderMovement.movementType,
      movementTypeDescription = prisonApiOffenderMovement.movementTypeDescription,
      fromAgency = prisonApiOffenderMovement.fromAgency,
      fromAgencyDescription = prisonApiOffenderMovement.fromAgencyDescription,
      toAgency = prisonApiOffenderMovement.toAgency,
      toAgencyDescription = prisonApiOffenderMovement.toAgencyDescription,
      movementDateTime = LocalDateTime.of(
        prisonApiOffenderMovement.movementDate,
        prisonApiOffenderMovement.movementTime,
      ),
    )
  }
}
