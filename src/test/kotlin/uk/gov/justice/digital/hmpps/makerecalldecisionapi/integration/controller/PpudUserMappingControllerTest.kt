package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMappingSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.PpudUserMappingRepository

@Suppress("SameParameterValue")
@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class PpudUserMappingControllerTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var ppudUserMappingRepository: PpudUserMappingRepository

  @Test
  fun `search mapped users`() {
    // given
    val userName = "UserName"
    val ppudUserFullName = "PpudUserFullName"
    val teamName = "TeamName"
    val searchReq = PpudUserMappingSearchRequest(userName)
    val entity = PpudUserMappingEntity(userName = userName, ppudUserFullName = ppudUserFullName, ppudTeamName = teamName)
    ppudUserMappingRepository.save(entity)

    // when
    val response = convertResponseToJSONObject(
      postToSearchMappedUsers(
        searchReq,
      )
        .expectStatus().isOk,
    )

    // then
    val responseEntity = JSONObject(response.get("ppudUserMapping").toString())

    // and
    assertThat(responseEntity.get("ppudUserFullName")).isEqualTo(ppudUserFullName)
    assertThat(responseEntity.get("ppudTeamName")).isEqualTo(teamName)
    assertThat(responseEntity.get("userName")).isEqualTo(userName)

    ppudUserMappingRepository.delete(entity)
  }

  private fun postToSearchMappedUsers(requestBody: PpudUserMappingSearchRequest): WebTestClient.ResponseSpec =
    webTestClient.post()
      .uri("/user/search-mapped-users")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(requestBody))
      .exchange()
}
