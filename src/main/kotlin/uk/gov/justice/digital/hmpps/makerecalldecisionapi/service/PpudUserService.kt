package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserRepository

@Transactional
@Service
internal class PpudUserService(
  val ppudUserRepository: PpudUserRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun fetchPpudUser(
    userName: String,
  ): PpudUserEntity? {
    return ppudUserRepository.findByUserName(userName)
  }
}
