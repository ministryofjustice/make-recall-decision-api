package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserMappingRepository

@Transactional
@Service
internal class PpudUserMappingService(
  val ppudUserMappingRepository: PpudUserMappingRepository,
) {
  fun findByUserNameIgnoreCase(
    username: String,
  ): PpudUserMappingEntity? = ppudUserMappingRepository.findByUserNameIgnoreCase(username)
}
