package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.recommendationRequest

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RecommendationControllerTest() : IntegrationTestBase() {

  @Test
  fun `create and get recommendation`() {
    val crn = "A12345"
    webTestClient.post()
      .uri("/recommendations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(recommendationRequest(crn))
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isCreated
      .expectBody()
      .jsonPath("$.id").isEqualTo(1)

    webTestClient.get()
      .uri("/recommendations/1")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(1)
      .jsonPath("$.crn").isEqualTo(crn)
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
