package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMapping
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
  ) = ppudUserMappingRepository.findById(id).map { PpudUserMapping(it) }.orElse(null)

  fun getAllUserMappings() = ppudUserMappingRepository.findAll().map { PpudUserMapping(it) }

  fun saveUserMapping(
    ppudUserMapping: PpudUserMapping,
  ): PpudUserMapping {
    val newPpudUserMappingEntity = PpudUserMappingEntity(
      userName = ppudUserMapping.userName,
      ppudUserFullName = ppudUserMapping.ppudUserFullName,
      ppudTeamName = ppudUserMapping.ppudTeamName,
      ppudUserName = ppudUserMapping.ppudUserName,
    )
    return PpudUserMapping(ppudUserMappingRepository.save(newPpudUserMappingEntity))
  }

  fun deleteUserMapping(
    id: Long,
  ) = ppudUserMappingRepository.deleteById(id)

  fun updateUserMapping(
    id: Long,
    ppudUserMapping: PpudUserMapping,
  ): PpudUserMapping {
    val existingMapping = ppudUserMappingRepository.findById(id).orElseThrow { Exception("User mapping not found") }
    existingMapping.userName = ppudUserMapping.userName
    existingMapping.ppudUserFullName = ppudUserMapping.ppudUserFullName
    existingMapping.ppudTeamName = ppudUserMapping.ppudTeamName
    existingMapping.ppudUserName = ppudUserMapping.ppudUserName
    return PpudUserMapping(ppudUserMappingRepository.save(existingMapping))
  }
}
