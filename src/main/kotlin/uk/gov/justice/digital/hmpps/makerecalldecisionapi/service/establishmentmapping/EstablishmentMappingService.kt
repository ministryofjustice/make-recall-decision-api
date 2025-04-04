package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.establishmentmapping

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.EstablishmentMappingRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.establishmentmapping.converter.EstablishmentMappingConverter

@Service
internal class EstablishmentMappingService(
  private val repository: EstablishmentMappingRepository,
  private val converter: EstablishmentMappingConverter,
) {

  fun getEstablishmentMappings(): Map<String, String> {
    val establishmentMappingEntities = repository.findAll()

    val establishmentMappings = converter.convert(establishmentMappingEntities)

    return establishmentMappings
  }
}
