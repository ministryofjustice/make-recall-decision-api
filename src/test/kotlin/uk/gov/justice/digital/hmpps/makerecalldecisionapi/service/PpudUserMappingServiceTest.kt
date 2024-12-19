package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserMappingRepository

@ExtendWith(MockitoExtension::class)
internal class PpudUserMappingServiceTest : ServiceTestBase() {

  @Mock
  lateinit var ppudUserMappingRepository: PpudUserMappingRepository

  @InjectMocks
  lateinit var ppudUserMappingService: PpudUserMappingService

  @Test
  fun findByUserNameIgnoreCase() {
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

    val response = ppudUserMappingService.findByUserNameIgnoreCase(userName)
    assertThat(response?.ppudUserFullName).isEqualTo(ppudUserFullName)
    assertThat(response?.ppudTeamName).isEqualTo(teamName)
    assertThat(response?.userName).isEqualTo(userName)
  }

  @Test
  fun unsuccessfulFindByUserNameIgnoreCase() {
    val userName = "UserNameNotFound"

    given(ppudUserMappingRepository.findByUserNameIgnoreCase(userName)).willReturn(null)

    val response = ppudUserMappingService.findByUserNameIgnoreCase(userName)
    assertThat(response).isNull()
  }
}
