package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.repository.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.EstablishmentMappingEntity

interface EstablishmentMappingRepository : Repository<EstablishmentMappingEntity, String> {
  fun findAll(): List<EstablishmentMappingEntity>
}
