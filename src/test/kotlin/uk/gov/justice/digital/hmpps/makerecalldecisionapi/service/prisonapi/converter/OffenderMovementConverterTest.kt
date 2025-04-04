package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.assertMovementsAreEqual
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.domain.prisonApiOffenderMovement

class OffenderMovementConverterTest {

  private val converter = OffenderMovementConverter()

  @Test
  fun `converts offender movement`() {
    // given
    val prisonApiOffenderMovement = prisonApiOffenderMovement()

    // when
    val actualOffenderMovement = converter.convert(prisonApiOffenderMovement)

    // then
    assertMovementsAreEqual(actualOffenderMovement, prisonApiOffenderMovement)
  }

  @Test
  fun `converts a list of offenders`() {
    // given
    val prisonApiOffenderMovement1 = prisonApiOffenderMovement()
    val prisonApiOffenderMovement2 = prisonApiOffenderMovement()
    val prisonApiOffenderMovements = listOf(prisonApiOffenderMovement1, prisonApiOffenderMovement2)

    // when
    val actualOffenderMovements = converter.convert(prisonApiOffenderMovements)

    // then
    assertThat(actualOffenderMovements).hasSameSizeAs(prisonApiOffenderMovements)
    for (i in actualOffenderMovements.indices) {
      assertMovementsAreEqual(actualOffenderMovements[i], prisonApiOffenderMovements[i])
    }
  }

  @Test
  fun `converts an empty list to an empty list`() {
    // when
    val actualOffenderMovements = converter.convert(emptyList())

    // then
    assertThat(actualOffenderMovements).isEmpty()
  }
}
