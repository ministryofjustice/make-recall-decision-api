package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserMappingRepository

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PpudUserMappingServiceTest : ServiceTestBase() {

  @Test
  fun findByUserNameIgnoreCase() {
    val ppudUserMappingRepository = Mockito.mock(PpudUserMappingRepository::class.java)
    val userName = "UserName"
    val ppudUserFullName = "PpudUserFullName"
    val teamName = "TeamName"

    given(ppudUserMappingRepository.findByUserNameIgnoreCase(userName)).willReturn(
      PpudUserMappingEntity(
        id = 1,
        userName = userName,
        ppudTeamName = teamName,
        ppudUserFullName = ppudUserFullName,
      ),
    )

    val response = ppudUserMappingRepository.findByUserNameIgnoreCase(userName)
    assertThat(response?.ppudUserFullName).isEqualTo(ppudUserFullName)
    assertThat(response?.ppudTeamName).isEqualTo(teamName)
    assertThat(response?.userName).isEqualTo(userName)
  }
}
