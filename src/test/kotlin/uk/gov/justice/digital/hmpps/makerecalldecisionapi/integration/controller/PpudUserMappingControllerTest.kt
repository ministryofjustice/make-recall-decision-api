package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import com.fasterxml.jackson.core.type.TypeReference
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMapping
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMappingSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ppudUserMapping
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserMappingRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader

@Suppress("SameParameterValue")
@ActiveProfiles("test")
class PpudUserMappingControllerTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var ppudUserMappingRepository: PpudUserMappingRepository

  @BeforeEach
  fun setup() {
    ppudUserMappingRepository.deleteAll()
  }

  @Test
  fun `search mapped users`() {
    // given
    val userName = "UserName"
    val ppudUserFullName = "PpudUserFullName"
    val ppudUserName = "PpudUserName"
    val teamName = "TeamName"
    val searchReq = PpudUserMappingSearchRequest(userName)
    val entity = PpudUserMappingEntity(
      userName = userName,
      ppudUserFullName = ppudUserFullName,
      ppudTeamName = teamName,
      ppudUserName = ppudUserName,
    )
    ppudUserMappingRepository.save(entity)

    // when
    val response = convertResponseToJSONObject(
      postToSearchMappedUsers(
        searchReq,
      )
        .expectStatus().isOk,
    )

    // then
    val ppudUserMapping = response.get("ppudUserMapping") as JSONObject
    assertThat(ppudUserMapping.get("userName")).isEqualTo(userName)
    assertThat(ppudUserMapping.get("ppudUserFullName")).isEqualTo(ppudUserFullName)
    assertThat(ppudUserMapping.get("ppudTeamName")).isEqualTo(teamName)
    assertThat(ppudUserMapping.get("ppudUserName")).isEqualTo(ppudUserName)

    ppudUserMappingRepository.delete(entity)
  }

  @Test
  fun `search mapped users - unsuccessful`() {
    // given
    val userName = "UserName"
    val searchReq = PpudUserMappingSearchRequest(userName)
    // when
    val response = convertResponseToJSONObject(
      postToSearchMappedUsers(
        searchReq,
      )
        .expectStatus().isOk,
    )

    // then
    assertThat(response.isNull("ppudUserMapping"))
  }

  @Test
  fun `get all mapped users`() {
    // given
    val expectedPpudUserMappings = (1..3).map { ppudUserMapping() }
    val ppudUserMappingEntities = expectedPpudUserMappings.map {
      PpudUserMappingEntity(
        userName = it.userName,
        ppudUserFullName = it.ppudUserFullName,
        ppudTeamName = it.ppudTeamName,
        ppudUserName = it.ppudUserName,
      )
    }
    ppudUserMappingRepository.saveAll(ppudUserMappingEntities)

    // when
    val response = convertResponseToJSONArray(
      webTestClient.get()
        .uri("/ppud-user-mappings")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk,
    )

    // then
    val jacksonTypeReference: TypeReference<List<PpudUserMapping>> =
      object : TypeReference<List<PpudUserMapping>>() {}
    val actualPpudUserMappings = ResourceLoader.CustomMapper.readValue(response.toString(), jacksonTypeReference)
    assertThat(actualPpudUserMappings).usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
      .isEqualTo(expectedPpudUserMappings)

    ppudUserMappingRepository.deleteAll(ppudUserMappingEntities)
  }

  @Test
  fun `get a mapped user by id`() {
    // given
    val expectedPpudUserMappings = (1..3).map { ppudUserMapping() }
    val searchedForUserMapping = expectedPpudUserMappings.random()
    val ppudUserMappingEntities = expectedPpudUserMappings.map {
      PpudUserMappingEntity(
        userName = it.userName,
        ppudUserFullName = it.ppudUserFullName,
        ppudTeamName = it.ppudTeamName,
        ppudUserName = it.ppudUserName,
      )
    }
    val savedEntities = ppudUserMappingRepository.saveAll(ppudUserMappingEntities)
    val searchedForEntity = savedEntities.first { it.userName == searchedForUserMapping.userName }

    // when
    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/ppud-user-mappings/${searchedForEntity.id}")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk,
    )

    // then
    val jacksonTypeReference: TypeReference<PpudUserMapping> =
      object : TypeReference<PpudUserMapping>() {}
    val actualPpudUserMapping = ResourceLoader.CustomMapper.readValue(response.toString(), jacksonTypeReference)
    assertThat(actualPpudUserMapping.id).isEqualTo(searchedForEntity.id.toString())
    assertThat(actualPpudUserMapping).usingRecursiveComparison().ignoringFields("id").isEqualTo(searchedForUserMapping)

    ppudUserMappingRepository.deleteAll(savedEntities)
  }

  @Test
  fun `update existing user mapping`() {
    // given
    val existingUserMapping = ppudUserMapping()
    val existingEntity = PpudUserMappingEntity(
      userName = existingUserMapping.userName,
      ppudUserFullName = existingUserMapping.ppudUserFullName,
      ppudTeamName = existingUserMapping.ppudTeamName,
      ppudUserName = existingUserMapping.ppudUserName,
    )
    val savedEntity = ppudUserMappingRepository.save(existingEntity)

    val updatedUserMapping = ppudUserMapping()

    // when
    val response = convertResponseToJSONObject(
      webTestClient.put()
        .uri("/ppud-user-mappings/${savedEntity.id}")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(updatedUserMapping))
        .exchange()
        .expectStatus().isOk,
    )

    // then
    val jacksonTypeReference: TypeReference<PpudUserMapping> =
      object : TypeReference<PpudUserMapping>() {}
    val actualPpudUserMapping = ResourceLoader.CustomMapper.readValue(response.toString(), jacksonTypeReference)
    assertThat(actualPpudUserMapping.id).isEqualTo(savedEntity.id.toString())
    assertThat(actualPpudUserMapping).usingRecursiveComparison().ignoringFields("id").isEqualTo(updatedUserMapping)

    ppudUserMappingRepository.delete(savedEntity)
  }

  @Test
  fun `delete user mapping`() {
    // given
    val existingUserMappings = (1..3).map { ppudUserMapping() }
    val existingUserMapping = existingUserMappings.random()
    val existingEntities = existingUserMappings.map {
      PpudUserMappingEntity(
        userName = it.userName,
        ppudUserFullName = it.ppudUserFullName,
        ppudTeamName = it.ppudTeamName,
        ppudUserName = it.ppudUserName,
      )
    }
    val savedEntities = ppudUserMappingRepository.saveAll(existingEntities)
    val entityToDelete = savedEntities.first { it.userName == existingUserMapping.userName }

    // when
    webTestClient.delete()
      .uri("/ppud-user-mappings/${entityToDelete.id}")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNoContent

    // then
    webTestClient.get()
      .uri("/ppud-user-mappings/${entityToDelete.id}")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNotFound

    val response = convertResponseToJSONArray(
      webTestClient.get()
        .uri("/ppud-user-mappings")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk,
    )

    // then
    val jacksonTypeReference: TypeReference<List<PpudUserMapping>> =
      object : TypeReference<List<PpudUserMapping>>() {}
    val actualPpudUserMappings = ResourceLoader.CustomMapper.readValue(response.toString(), jacksonTypeReference)
    assertThat(actualPpudUserMappings).usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
      .isEqualTo(existingUserMappings.filter { it.userName != existingUserMapping.userName })
  }

  private fun postToSearchMappedUsers(requestBody: PpudUserMappingSearchRequest): WebTestClient.ResponseSpec = webTestClient.post()
    .uri("/user-mapping/search")
    .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
    .contentType(MediaType.APPLICATION_JSON)
    .body(BodyInserters.fromValue(requestBody))
    .exchange()
}
