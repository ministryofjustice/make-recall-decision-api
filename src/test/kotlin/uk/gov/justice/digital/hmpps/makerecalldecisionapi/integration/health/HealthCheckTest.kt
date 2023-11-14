package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.health

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockserver.model.HttpError
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.HttpStatusCode
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.EnableTransactionManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE
import javax.sql.DataSource

@ExperimentalCoroutinesApi
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
  properties = ["management.server.port=9999", "server.port=9999"],
)
class HealthCheckTest : IntegrationTestBase() {

  @Test
  @Order(1)
  fun `Health page reports ok`() {
    healthCheckIsUp("/health")
  }

  @Test
  @Order(2)
  fun `Health info reports version`() {
    healthCheckIsUpWith(
      "/health",
      HttpStatus.OK,
      "status" to "UP",
      "components.healthInfo.details.version" to LocalDateTime.now().format(ISO_DATE),
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
      HttpStatus.OK,
      "status" to "UP",
      "components.hmppsAuth.status" to "UP",
      "components.deliusIntegration.status" to "UP",
      "components.offenderSearchApi.status" to "UP",
      "components.gotenberg.status" to "UP",
      "components.ppudAutomationApi.status" to "UP",
    )
  }

  @Test
  fun `Health page reports if the component checks fail`() {
    oauthMock.clear(request().withPath("/auth/health/ping"))
    oauthMock.`when`(request().withPath("/auth/health/ping")).error(HttpError.error())

    deliusIntegration.clear(request().withPath("/health"))
    deliusIntegration.`when`(request().withPath("/health")).error(HttpError.error())

    offenderSearchApi.clear(request().withPath("/health/ping"))
    offenderSearchApi.`when`(request().withPath("/health/ping")).error(HttpError.error())

    gotenbergMock.clear(request().withPath("/health"))
    gotenbergMock.`when`(request().withPath("/health")).error(HttpError.error())

    ppudAutomationApi.clear(request().withPath("/health/ping"))
    ppudAutomationApi.`when`(request().withPath("/health/ping")).error(HttpError.error())

    healthCheckIsUpWith(
      "/health",
      HttpStatus.SERVICE_UNAVAILABLE,
      "components.hmppsAuth.status" to "DOWN",
      "components.deliusIntegration.status" to "DOWN",
      "components.offenderSearchApi.status" to "DOWN",
      "components.gotenberg.status" to "DOWN",
      "components.ppudAutomationApi.status" to "DOWN_BUT_OPTIONAL",
    )
  }

  @Test
  fun `Health page reports OK if PPUD Automation is the only component failing`() {
    ppudAutomationApi.clear(request().withPath("/health/ping"))
    ppudAutomationApi.`when`(request().withPath("/health/ping"))
      .respond(response().withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR_500.code()))

    healthCheckIsUpWith(
      "/health",
      HttpStatus.OK,
      "status" to "UP",
      "components.hmppsAuth.status" to "UP",
      "components.deliusIntegration.status" to "UP",
      "components.offenderSearchApi.status" to "UP",
      "components.gotenberg.status" to "UP",
      "components.ppudAutomationApi.status" to "DOWN_BUT_OPTIONAL",
    )
  }

  private fun healthCheckIsUp(healthUrl: String) {
    healthCheckIsUpWith(healthUrl, HttpStatus.OK, "status" to "UP")
  }

  private fun healthCheckIsUpWith(
    healthUrl: String,
    expectedStatus: HttpStatus,
    vararg jsonPathAssertions: Pair<String, String>,
  ) {
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

@Configuration
@EnableTransactionManagement
class JpaConfig(private val env: Environment) {
  @Bean
  fun dataSource(): DataSource {
    val dataSource = DriverManagerDataSource()
    dataSource.setDriverClassName("org.postgresql.Driver")
    dataSource.url = "jdbc:postgresql://127.0.0.1:5432/make_recall_decision"
    dataSource.username = "mrd_user"
    dataSource.password = "secret"
    return dataSource
  }
}
