package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.PrisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.prisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.offenderMovement
import java.time.LocalDateTime

class OffenderMovementConverterTest {

  private val converter = OffenderMovementConverter()

  @Test
  fun `converts offender movement`() {
    // given
    val prisonApiOffenderMovement = prisonApiOffenderMovement()
    val expectedOffenderMovement = offenderMovementFrom(prisonApiOffenderMovement)

    // when
    val actualOffenderMovement = converter.convert(prisonApiOffenderMovement)

    // then
    assertThat(actualOffenderMovement).isEqualTo(expectedOffenderMovement)
  }

  @Test
  fun `converts a list of offenders`() {
    // given
    val prisonApiOffenderMovement1 = prisonApiOffenderMovement()
    val prisonApiOffenderMovement2 = prisonApiOffenderMovement()
    val prisonApiOffenderMovements = listOf(prisonApiOffenderMovement1, prisonApiOffenderMovement2)
    val expectedOffenderMovements = listOf(
      offenderMovementFrom(prisonApiOffenderMovement1),
      offenderMovementFrom(prisonApiOffenderMovement2),
    )

    // when
    val actualOffenderMovements = converter.convert(prisonApiOffenderMovements)

    // then
    assertThat(actualOffenderMovements).isEqualTo(expectedOffenderMovements)
  }

  @Test
  fun `converts an empty list to an empty list`() {
    // when
    val actualOffenderMovements = converter.convert(emptyList())

    // then
    assertThat(actualOffenderMovements).isEmpty()
  }

  private fun offenderMovementFrom(prisonApiOffenderMovement: PrisonApiOffenderMovement) =
    offenderMovement(
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
