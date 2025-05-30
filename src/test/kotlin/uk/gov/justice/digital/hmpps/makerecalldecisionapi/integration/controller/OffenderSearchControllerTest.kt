package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters.fromValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import kotlin.random.Random

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class OffenderSearchControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
) : IntegrationTestBase() {

  @Test
  fun `retrieves simple case summary details using crn`() {
    runTest {
      val crn = "A123456"
      val firstName = "Joe"
      val lastName = "Bloggs"
      val dateOfBirth = "2000-11-09"
      findByCrnSuccess(crn, firstName, lastName, dateOfBirth)
      val requestBody = OffenderSearchRequest(crn = crn)
      webTestClient.post()
        .uri("/paged-search?page=0&pageSize=1")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .body(fromValue(requestBody))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.results.length()").isEqualTo(1)
        .jsonPath("$.results[0].name").isEqualTo("$firstName $lastName")
        .jsonPath("$.results[0].dateOfBirth").isEqualTo(dateOfBirth)
        .jsonPath("$.results[0].crn").isEqualTo(crn)
    }
  }

  @Test
  fun `retrieves simple case summary details using first and last name`() {
    runTest {
      val crn = "A123456"
      val firstName = "Joe"
      val lastName = "Bloggs"
      val dateOfBirth = "2000-11-09"
      findByNameSuccess(crn = crn, firstName = firstName, surname = lastName, dateOfBirth = dateOfBirth)
      val requestBody = OffenderSearchRequest(firstName = firstName, lastName = lastName)
      webTestClient.post()
        .uri("/paged-search?page=1&pageSize=1")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .body(fromValue(requestBody))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.results.length()").isEqualTo(1)
        .jsonPath("$.results[0].name").isEqualTo("$firstName $lastName")
        .jsonPath("$.results[0].dateOfBirth").isEqualTo(dateOfBirth)
        .jsonPath("$.results[0].crn").isEqualTo(crn)
    }
  }

  @Test
  fun `search response contains paging data`() {
    runTest {
      val crn = "A123456"
      val firstName = "test"
      val lastName = "test"
      val page = Random.Default.nextInt(1, 19)
      val pageSize = Random.Default.nextInt(1, 10)
      val totalNumberOfPages = Random.Default.nextInt(page, 20)
      findByNameSuccess(crn, firstName, lastName, pageNumber = page - 1, pageSize = pageSize, totalPages = totalNumberOfPages)
      val requestBody = OffenderSearchRequest(firstName = firstName, lastName = lastName)
      webTestClient.post()
        .uri("/paged-search?page=$page&pageSize=$pageSize")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .body(fromValue(requestBody))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.paging.page").isEqualTo(page)
        .jsonPath("$.paging.pageSize").isEqualTo(pageSize)
        .jsonPath("$.paging.totalNumberOfPages").isEqualTo(totalNumberOfPages)
    }
  }

  @Test
  fun `given excluded case for my user then set the user access excluded field`() {
    runTest {
      val crn = "X123456"
      findByCrnSuccess(crn)
      userAccessExcluded(crn)
      val requestBody = OffenderSearchRequest(crn = crn)
      webTestClient.post()
        .uri("/paged-search?page=0&pageSize=1")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .body(fromValue(requestBody))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.results.length()").isEqualTo(1)
        .jsonPath("$.results[0].name").isEmpty()
        .jsonPath("$.results[0].dateOfBirth").isEqualTo(null)
        .jsonPath("$.results[0].crn").isEqualTo(crn)
        .jsonPath("$.results[0].userExcluded").isEqualTo(true)
        .jsonPath("$.results[0].userRestricted").isEqualTo(false)
    }
  }

  @Test
  fun `given restricted case for my user then set the user restricted flag to true`() {
    runTest {
      val crn = "X123456"
      findByCrnSuccess(crn)
      userAccessRestricted(crn)
      val requestBody = OffenderSearchRequest(crn = crn)
      webTestClient.post()
        .uri("/paged-search?page=0&pageSize=1")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .body(fromValue(requestBody))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.results.length()").isEqualTo(1)
        .jsonPath("$.results[0].name").isEmpty
        .jsonPath("$.results[0].dateOfBirth").isEqualTo(null)
        .jsonPath("$.results[0].crn").isEqualTo(crn)
        .jsonPath("$.results[0].userExcluded").isEqualTo(false)
        .jsonPath("$.results[0].userRestricted").isEqualTo(true)
    }
  }

  @Test
  fun `gateway timeout 504 given on Offender Search Api timeout on offenders search people endpoint`() {
    runTest {
      val crn = "X123456"
      findByCrnSuccess(crn, delaySeconds = nDeliusTimeout + 2)
      val requestBody = OffenderSearchRequest(crn = crn)
      webTestClient.post()
        .uri("/paged-search?page=0&pageSize=1")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .body(fromValue(requestBody))
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Delius integration client - /case-summary/X123456 endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on Delius timeout on user access endpoint`() {
    runTest {
      val crn = "X123456"
      findByCrnSuccess(crn)
      userAccessAllowed(crn, delaySeconds = nDeliusTimeout + 2)
      val requestBody = OffenderSearchRequest(crn = crn)
      webTestClient.post()
        .uri("/paged-search?page=0&pageSize=1")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .body(fromValue(requestBody))
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Delius integration client - /user/SOME_USER/access/$crn endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `access denied on paged search endpoint when insufficient privileges used`() {
    runTest {
      webTestClient.post()
        .uri("/paged-search?page=0&pageSize=1")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
