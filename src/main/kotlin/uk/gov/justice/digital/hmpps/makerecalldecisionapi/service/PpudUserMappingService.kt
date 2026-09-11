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

  fun findById(
    id: Long,
  ) = ppudUserMappingRepository.findById(id).orElse(null)

  fun getAllUserMappings() = ppudUserMappingRepository.findAll()

  fun saveUserMapping(
    ppudUserMappingEntity: PpudUserMappingEntity,
  ): PpudUserMappingEntity = ppudUserMappingRepository.save(ppudUserMappingEntity)

  fun deleteUserMapping(
    id: Long,
  ) = ppudUserMappingRepository.deleteById(id)

  fun updateUserMapping(
    id: Long,
    ppudUserMappingEntity: PpudUserMappingEntity,
  ): PpudUserMappingEntity {
    val existingMapping = ppudUserMappingRepository.findById(id).orElseThrow { Exception("User mapping not found") }
    existingMapping.userName = ppudUserMappingEntity.userName
    existingMapping.ppudUserFullName = ppudUserMappingEntity.ppudUserFullName
    existingMapping.ppudTeamName = ppudUserMappingEntity.ppudTeamName
    existingMapping.ppudUserName = ppudUserMappingEntity.ppudUserName
    return ppudUserMappingRepository.save(existingMapping)
  }
}
