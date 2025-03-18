package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.establishmentmapping

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.establishmentMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.EstablishmentMappingRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.establishmentmapping.converter.EstablishmentMappingConverter

@ExtendWith(MockitoExtension::class)
class EstablishmentMappingServiceTest {

  @InjectMocks
  private lateinit var service: EstablishmentMappingService

  @Mock
  private lateinit var repository: EstablishmentMappingRepository

  @Mock
  private lateinit var converter: EstablishmentMappingConverter

  @Test
  fun `gets mappings list and converts to a map`() {
    // given
    val establishmentMappingEntities = listOf(establishmentMappingEntity(), establishmentMappingEntity())
    given(repository.findAll()).willReturn(establishmentMappingEntities)

    val expectedEstablishmentMappings = mapOf<String, String>()
    given(converter.convert(establishmentMappingEntities)).willReturn(expectedEstablishmentMappings)

    // when
    val actualEstablishmentMappings = service.getEstablishmentMappings()

    // then
    assertThat(actualEstablishmentMappings).isEqualTo(expectedEstablishmentMappings)
  }
}
