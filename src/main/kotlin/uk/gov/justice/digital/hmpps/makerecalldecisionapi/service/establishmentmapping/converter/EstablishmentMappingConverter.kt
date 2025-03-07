package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.establishmentmapping.converter

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.EstablishmentMappingEntity

@Service
internal class EstablishmentMappingConverter {

  fun convert(establishmentMappingEntities: List<EstablishmentMappingEntity>): Map<String, String> {
    val mappings =
      establishmentMappingEntities.associate { entity ->
        Pair(
          entity.nomisAgencyId,
          entity.ppudEstablishment,
        )
      }

    if (establishmentMappingEntities.size == mappings.size) {
      return mappings
    } else {
      val duplicateAgencyId = findDuplicateAgencyId(establishmentMappingEntities)
      throw IllegalArgumentException("Duplicate NOMIS Agency ID found in establishment mapping: $duplicateAgencyId")
    }
  }

  private fun findDuplicateAgencyId(establishmentMappingEntities: List<EstablishmentMappingEntity>): String {
    val nomisAgencyIds = mutableSetOf<String>()
    for (entity in establishmentMappingEntities) {
      if (nomisAgencyIds.contains(entity.nomisAgencyId)) {
        return entity.nomisAgencyId
      }
      nomisAgencyIds.add(entity.nomisAgencyId)
    }
    throw IllegalStateException("Expected to find a duplicate NOMIS Agency ID, but found none")
  }
}
