package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.establishmentmapping.converter

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.EstablishmentMappingEntity

class EstablishmentMappingConverterTest {
  private val converter = EstablishmentMappingConverter()

  @Test
  fun `converts list of values to map`() {
    // given
    val establishmentMappingEntities = listOf(
      EstablishmentMappingEntity("a", "b"),
      EstablishmentMappingEntity("c", "d"),
      EstablishmentMappingEntity("e", "f"),
      EstablishmentMappingEntity("g", "b"),
    )
    val expectedMap = mapOf("a" to "b", "c" to "d", "e" to "f", "g" to "b")

    // when
    val actualMap = converter.convert(establishmentMappingEntities)

    // then
    assertThat(actualMap).isEqualTo(expectedMap)
  }

  @Test
  fun `converts empty list to empty map`() {
    // given
    val establishmentMappingEntities = emptyList<EstablishmentMappingEntity>()

    // when
    val actualMap = converter.convert(establishmentMappingEntities)

    // then
    assertThat(actualMap).isEmpty()
  }

  @Test
  fun `raises an error if the a NOMIS agency ID is found more than once in the given list`() {
    // given
    val establishmentMappingEntities = listOf(
      EstablishmentMappingEntity("a", "b"),
      EstablishmentMappingEntity("c", "d"),
      EstablishmentMappingEntity("a", "f"),
    )

    // when then
    assertThatThrownBy { converter.convert(establishmentMappingEntities) }
      .isInstanceOf(IllegalArgumentException::class.java)
  }
}
