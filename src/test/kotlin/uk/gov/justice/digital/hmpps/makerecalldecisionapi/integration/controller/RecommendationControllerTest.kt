package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.recommendationRequest

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RecommendationControllerTest() : IntegrationTestBase() {

  @Test
  fun `create and get recommendation`() {
    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn))
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isCreated
    )

    val idOfRecommendationJustCreated = response.get("id")
    val activeRecommendation = JSONObject(response.get("activeRecommendation").toString())

    assertThat(response.get("id")).isNotNull
    assertThat(response.get("status")).isEqualTo("DRAFT")
    assertThat(activeRecommendation.get("recommendationId")).isEqualTo(idOfRecommendationJustCreated)
    assertThat(activeRecommendation.get("lastModifiedDate")).isNotNull
    assertThat(activeRecommendation.get("lastModifiedBy")).isEqualTo("SOME_USER")

    webTestClient.get()
      .uri("/recommendations/$idOfRecommendationJustCreated")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(idOfRecommendationJustCreated)
      .jsonPath("$.crn").isEqualTo(crn)
  }

  @Test
  fun `update a recommendation`() {
    val crn = "A12345"
    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn))
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isCreated
    )

    val idOfRecommendationJustCreated = response.get("id")

    webTestClient.patch()
      .uri("/recommendations/$idOfRecommendationJustCreated")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(updateRecommendationRequest())
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(1)
      .jsonPath("$.crn").isEqualTo(crn)
      .jsonPath("$.recallType.value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.options[0].value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.options[0].text").isEqualTo("Fixed term")
      .jsonPath("$.recallType.options[1].value").isEqualTo("STANDARD")
      .jsonPath("$.recallType.options[1].text").isEqualTo("Standard")
      .jsonPath("$.recallType.options[2].value").isEqualTo("NO_RECALL")
      .jsonPath("$.recallType.options[2].text").isEqualTo("No recall")
      .jsonPath("$.status").isEqualTo("DRAFT")
//      .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(idOfRecommendationJustCreated)
//      .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
//      .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER") // TODO reintroduce once MRD-342 complete
  }

  @Test
  fun `handles scenario where no recommendation exists for given id on update`() {
    webTestClient.patch()
      .uri("/recommendations/999")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(updateRecommendationRequest())
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
      .jsonPath("$.userMessage")
      .isEqualTo("No recommendation available: No recommendation found for id: 999")
  }

  @Test
  fun `handles scenario where no recommendation exists for given id`() {
    runTest {
      webTestClient.get()
        .uri("/recommendations/999")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
        .jsonPath("$.userMessage")
        .isEqualTo("No recommendation available: No recommendation found for id: 999")
    }
  }

  @Test
  fun `access denied when insufficient privileges used for creation request`() {
    val crn = "X123456"
    webTestClient.post()
      .uri("/recommendations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(recommendationRequest(crn))
      )
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  private fun convertResponseToJSONObject(response: WebTestClient.ResponseSpec): JSONObject {
    val responseBodySpec = response.expectBody<String>()
    val responseEntityExchangeResult = responseBodySpec.returnResult()
    val responseString = responseEntityExchangeResult.responseBody
    return JSONObject(responseString)
  }

  companion object {
    @JvmStatic
    @BeforeAll
    fun setUpDb() {
      cleanUpDocker()
      ProcessBuilder("docker-compose", "-f", "docker-compose-integration-test-postgres.yml", "up", "-d").start()
      val ready = ProcessBuilder("./scripts/wait-for-it.sh", "127.0.0.1:5432", "--strict", "-t", "600").start()
      ready.waitFor()
    }

    @JvmStatic
    @AfterAll
    fun tearDownDb() {
      cleanUpDocker()
    }

    @JvmStatic
    private fun cleanUpDocker() {
      val clean = ProcessBuilder("./scripts/clean-up-docker.sh").start()
      clean.waitFor()
    }
  }
}
