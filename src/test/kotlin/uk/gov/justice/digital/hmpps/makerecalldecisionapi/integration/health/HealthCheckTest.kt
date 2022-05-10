package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.health

import org.junit.jupiter.api.Test
import org.mockserver.model.HttpError
import org.mockserver.model.HttpRequest.request
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = ["management.server.port=9999", "server.port=9999"])
class HealthCheckTest : IntegrationTestBase() {

  @Test
  fun `Health page reports ok`() {
    healthCheckIsUp("/health")
  }

  @Test
  fun `Health info reports version`() {
    healthCheckIsUpWith(
      "/health",
      "status" to "UP",
      "components.healthInfo.details.version" to LocalDateTime.now().format(ISO_DATE)
    )
  }

  @Test
  fun `Health ping page is accessible`() {
    healthCheckIsUp("/health/ping")
  }

  @Test
  fun `readiness reports ok`() {
    healthCheckIsUp("/health/readiness")
  }

  @Test
  fun `liveness reports ok`() {
    healthCheckIsUp("/health/liveness")
  }

  @Test
  fun `Health page reports ok when all component checks are ok`() {
    healthCheckIsUpWith(
      "/health",
      "status" to "UP",
      "components.hmppsAuth.status" to "UP",
      "components.communityApi.status" to "UP",
      "components.offenderSearchApi.status" to "UP",
      "components.gotenberg.status" to "UP"
    )
  }

  @Test
  fun `Health page reports if the component checks fail`() {
    oauthMock.clear(request().withPath("/auth/health/ping"))
    oauthMock.`when`(request().withPath("/auth/health/ping")).error(HttpError.error())

    communityApi.clear(request().withPath("/ping"))
    communityApi.`when`(request().withPath("/ping")).error(HttpError.error())

    offenderSearchApi.clear(request().withPath("/ping"))
    offenderSearchApi.`when`(request().withPath("/ping")).error(HttpError.error())

    gotenbergMock.clear(request().withPath("/health"))
    gotenbergMock.`when`(request().withPath("/health")).error(HttpError.error())

    healthCheckIsUpWith(
      "/health",
      "components.hmppsAuth.status" to "UNKNOWN",
      "components.communityApi.status" to "UNKNOWN",
      "components.offenderSearchApi.status" to "UNKNOWN",
      "components.gotenberg.status" to "UNKNOWN"
    )
  }

  private fun healthCheckIsUp(healthUrl: String) {
    healthCheckIsUpWith(healthUrl, "status" to "UP")
  }

  private fun healthCheckIsDownWith(expectedStatus: HttpStatus, vararg jsonPathAssertions: Pair<String, String>) {
    webTestClient.get()
      .uri("/health")
      .exchange()
      .expectStatus()
      .isEqualTo(expectedStatus)
      .expectBody()
      .jsonPath("status").isEqualTo("DOWN")
      .apply {
        jsonPathAssertions.forEach { (jsonPath, equalTo) ->
          hasJsonPath(jsonPath, equalTo)
        }
      }
  }

  private fun healthCheckIsUpWith(healthUrl: String, vararg jsonPathAssertions: Pair<String, String>, expectedStatus: HttpStatus = HttpStatus.OK) {
    webTestClient.get()
      .uri(healthUrl)
      .exchange()
      .expectStatus()
      .isEqualTo(expectedStatus)
      .expectBody()
      .apply {
        jsonPathAssertions.forEach { (jsonPath, equalTo) ->
          hasJsonPath(jsonPath, equalTo)
        }
      }
  }

  private fun WebTestClient.BodyContentSpec.hasJsonPath(jsonPath: String, equalTo: String) =
    jsonPath(jsonPath).isEqualTo(equalTo)
}
