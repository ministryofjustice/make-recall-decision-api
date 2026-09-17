package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMapping
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ppudUserMapping
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.ppudUserMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserMappingRepository

@ExtendWith(MockitoExtension::class)
internal class PpudUserMappingServiceTest : ServiceTestBase() {

  @InjectMocks
  lateinit var ppudUserMappingService: PpudUserMappingService

  @Mock
  lateinit var ppudUserMappingRepository: PpudUserMappingRepository

  @Captor
  lateinit var ppudUserMappingEntityCaptor: ArgumentCaptor<PpudUserMappingEntity>

  @Test
  fun findByUserNameIgnoreCase() {
    val userName = "UserName"
    val ppudUserFullName = "PpudUserFullName"
    val ppudUserName = "PpudUserName"
    val teamName = "TeamName"

    given(ppudUserMappingRepository.findByUserNameIgnoreCase(userName)).willReturn(
      PpudUserMappingEntity(
        id = 1,
        userName = userName,
        ppudTeamName = teamName,
        ppudUserFullName = ppudUserFullName,
        ppudUserName = ppudUserName,
      ),
    )

    val response = ppudUserMappingService.findByUserNameIgnoreCase(userName)
    assertThat(response?.ppudUserFullName).isEqualTo(ppudUserFullName)
    assertThat(response?.ppudTeamName).isEqualTo(teamName)
    assertThat(response?.userName).isEqualTo(userName)
    assertThat(response?.ppudUserName).isEqualTo(ppudUserName)
  }

  @Test
  fun unsuccessfulFindByUserNameIgnoreCase() {
    val userName = "UserNameNotFound"

    given(ppudUserMappingRepository.findByUserNameIgnoreCase(userName)).willReturn(null)

    val response = ppudUserMappingService.findByUserNameIgnoreCase(userName)
    assertThat(response).isNull()
  }

  @Test
  fun findById() {
    // given
    val userMappingEntity = ppudUserMappingEntity()
    val expectedUserMapping = PpudUserMapping(userMappingEntity)

    given(ppudUserMappingRepository.findById(userMappingEntity.id)).willReturn(java.util.Optional.of(userMappingEntity))

    // when
    val response = ppudUserMappingService.findById(userMappingEntity.id)

    // then
    assertThat(response).isEqualTo(expectedUserMapping)
  }

  @Test
  fun getAllUserMappings() {
    // given
    val userMappingEntities = (1..3).map { ppudUserMappingEntity() }
    val expectedUserMappings = userMappingEntities.map { PpudUserMapping(it) }

    given(ppudUserMappingRepository.findAll()).willReturn(userMappingEntities)

    // when
    val response = ppudUserMappingService.getAllUserMappings()

    // then
    assertThat(response).isEqualTo(expectedUserMappings)
  }

  @Test
  fun saveUserMapping() {
    // given
    val userMapping = ppudUserMapping()
    val userMappingEntity = PpudUserMappingEntity(
      userName = userMapping.userName,
      ppudUserFullName = userMapping.ppudUserFullName,
      ppudTeamName = userMapping.ppudTeamName,
      ppudUserName = userMapping.ppudUserName,
    )
    val savedUserMappingEntity = userMappingEntity.copy(id = 1)

    given(ppudUserMappingRepository.save(ppudUserMappingEntityCaptor.capture())).willReturn(savedUserMappingEntity)

    // when
    val response = ppudUserMappingService.saveUserMapping(userMapping)

    // then
    assertThat(response).isEqualTo(PpudUserMapping(savedUserMappingEntity))
    assertThat(ppudUserMappingEntityCaptor.value).usingRecursiveComparison().ignoringFields("id")
      .isEqualTo(savedUserMappingEntity)
  }

  @Test
  fun deleteUserMapping() {
    // given
    val userMappingId = 1L

    // when
    ppudUserMappingService.deleteUserMapping(userMappingId)

    // then
    verify(ppudUserMappingRepository).deleteById(userMappingId)
  }

  @Test
  fun updateUserMapping() {
    // given
    val existingUserMappingEntity = ppudUserMappingEntity()
    val updatedUserMapping = ppudUserMapping()
    val updatedUserMappingEntity = ppudUserMappingEntity()

    given(ppudUserMappingRepository.findById(existingUserMappingEntity.id)).willReturn(java.util.Optional.of(existingUserMappingEntity))
    given(ppudUserMappingRepository.save(ppudUserMappingEntityCaptor.capture())).willReturn(updatedUserMappingEntity)

    // when
    val response = ppudUserMappingService.updateUserMapping(existingUserMappingEntity.id, updatedUserMapping)

    // then
    assertThat(response).isEqualTo(PpudUserMapping(updatedUserMappingEntity))
    assertThat(ppudUserMappingEntityCaptor.value.id).isEqualTo(existingUserMappingEntity.id)
    assertThat(ppudUserMappingEntityCaptor.value).usingRecursiveComparison().ignoringFields("id")
      .isEqualTo(existingUserMappingEntity)
  }
}
