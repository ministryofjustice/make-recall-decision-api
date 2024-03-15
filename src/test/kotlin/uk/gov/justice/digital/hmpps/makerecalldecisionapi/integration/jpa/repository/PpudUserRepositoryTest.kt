package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserRepository

@ActiveProfiles("test")
class PpudUserRepositoryTest : IntegrationTestBase() {

  @Autowired
  private lateinit var ppudUserRepository: PpudUserRepository

  @BeforeEach
  fun before() {
    ppudUserRepository.deleteAll()
    ppudUserRepository.save(PpudUserEntity(userName = "UserA", ppudUserFullName = "Joe Bloggs", ppudTeamName = "Team 1"))
    ppudUserRepository.save(PpudUserEntity(userName = "UserB", ppudUserFullName = "Jane Doe", ppudTeamName = "Team 2"))
  }

  @Suppress("SpellCheckingInspection")
  @ParameterizedTest
  @ValueSource(strings = ["UserA", "USERA", "usEra"])
  fun `given a username in any case when finding user then matching user details are returned`(username: String) {
    val result = ppudUserRepository.findByUserNameIgnoreCase(username)
    assertThat(result?.userName).isEqualTo("UserA")
    assertThat(result?.ppudUserFullName).isEqualTo("Joe Bloggs")
  }
}
