package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity

@Repository
interface PpudUserMappingRepository : JpaRepository<PpudUserMappingEntity, Long> {
  fun findByUserNameIgnoreCase(@Param("userName") userName: String): PpudUserMappingEntity?
}
