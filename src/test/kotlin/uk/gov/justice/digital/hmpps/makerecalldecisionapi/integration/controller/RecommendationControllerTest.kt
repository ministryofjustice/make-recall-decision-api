package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.recommendationRequest

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RecommendationControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  @Autowired private val objectMapper: ObjectMapper
) : IntegrationTestBase() {

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

  @Test
  fun `access denied when insufficient privileges used for Get request`() {
    runTest {
      webTestClient.get()
        .uri("/recommendations/123")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
