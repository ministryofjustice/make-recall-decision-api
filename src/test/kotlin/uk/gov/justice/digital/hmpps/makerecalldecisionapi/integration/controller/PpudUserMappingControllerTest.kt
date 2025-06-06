package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

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
class PpudUserMappingControllerTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var ppudUserMappingRepository: PpudUserMappingRepository

  @Test
  fun `search mapped users`() {
    // given
    val userName = "UserName"
    val ppudUserFullName = "PpudUserFullName"
    val ppudUserName = "PpudUserName"
    val teamName = "TeamName"
    val searchReq = PpudUserMappingSearchRequest(userName)
    val entity = PpudUserMappingEntity(userName = userName, ppudUserFullName = ppudUserFullName, ppudTeamName = teamName, ppudUserName = ppudUserName)
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
    assertThat(ppudUserMapping.get("fullName")).isEqualTo(ppudUserFullName)
    assertThat(ppudUserMapping.get("teamName")).isEqualTo(teamName)
    assertThat(ppudUserMapping.get("userName")).isEqualTo(ppudUserName)

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

  private fun postToSearchMappedUsers(requestBody: PpudUserMappingSearchRequest): WebTestClient.ResponseSpec = webTestClient.post()
    .uri("/user-mapping/search")
    .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
    .contentType(MediaType.APPLICATION_JSON)
    .body(BodyInserters.fromValue(requestBody))
    .exchange()
}
