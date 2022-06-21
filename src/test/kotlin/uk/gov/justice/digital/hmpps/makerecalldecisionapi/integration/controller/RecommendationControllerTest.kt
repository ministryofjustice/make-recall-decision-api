package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
  fun `creates recommendation`() {
    runBlockingTest {
      val crn = "A12345"
      webTestClient.post()
        .uri("/cases/$crn/recommendation")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest())
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.recommendation").isEqualTo("NOT_RECALL")
        .jsonPath("$.alternateActions").isEqualTo("increase reporting")
    }
  }

  @Test
  fun `access denied when Returns an overview of the person detailsinsufficient privileges used`() {
    runBlockingTest {
      val crn = "X123456"
      webTestClient.get()
        .uri("/cases/$crn/recommendation")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
