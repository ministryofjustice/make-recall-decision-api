package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class OffenderSearchControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  @Test
  fun `retrieves simple case summary details using crn`() {
    runTest {
      val crn = "X123456"
      offenderSearchResponse(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("Pontius Pilate")
        .jsonPath("$[0].dateOfBirth").isEqualTo("2000-11-09")
        .jsonPath("$[0].crn").isEqualTo(crn)
    }
  }

  @Test
  fun `retrieves simple case summary details using first and last name`() {
    runTest {
      val firstName = "Pontius"
      val lastName = "Pilate"
      offenderSearchResponse(firstName = firstName, surname = lastName)
      webTestClient.get()
        .uri("/search?lastName=$lastName&firstName=$firstName")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("Pontius Pilate")
        .jsonPath("$[0].dateOfBirth").isEqualTo("2000-11-09")
        .jsonPath("$[0].crn").isEqualTo("X123456")
    }
  }

  @Test
  fun `given excluded case for my user then set the user access excluded field`() {
    runTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userAccessExcluded(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("null null")
        .jsonPath("$[0].dateOfBirth").isEqualTo(null)
        .jsonPath("$[0].crn").isEqualTo(crn)
        .jsonPath("$[0].userExcluded").isEqualTo(true)
        .jsonPath("$[0].userRestricted").isEqualTo(false)
    }
  }

  @Test
  fun `given missing name and case is excluded but not for my user then default missing name details`() {
    runTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userAccessAllowed(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("No name available")
        .jsonPath("$[0].dateOfBirth").isEqualTo(null)
        .jsonPath("$[0].crn").isEqualTo(crn)
        .jsonPath("$[0].userExcluded").isEqualTo(null)
        .jsonPath("$[0].userRestricted").isEqualTo(null)
    }
  }

  @Test
  fun `given restricted case for my user then set the user restricted flag to true`() {
    runTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userAccessRestricted(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("null null")
        .jsonPath("$[0].dateOfBirth").isEqualTo(null)
        .jsonPath("$[0].crn").isEqualTo(crn)
        .jsonPath("$[0].userExcluded").isEqualTo(false)
        .jsonPath("$[0].userRestricted").isEqualTo(true)
    }
  }

  @Test
  fun `given case with no name and user access replies with 404 then set search to no name available`() {
    runTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userNotFound(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("No name available")
        .jsonPath("$[0].dateOfBirth").isEqualTo(null)
        .jsonPath("$[0].crn").isEqualTo(crn)
        .jsonPath("$[0].userExcluded").isEqualTo(null)
        .jsonPath("$[0].userRestricted").isEqualTo(null)
    }
  }

  @Test
  fun `gateway timeout 503 given on Offender Search Api timeout on offenders search by phrase endpoint`() {
    runTest {
      val crn = "X123456"
      offenderSearchResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Offender Search API Client - search by phrase endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on user access endpoint`() {
    runTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userAccessAllowed(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - user access endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runTest {
      val crn = "X123456"
      offenderSearchResponse(crn)
      webTestClient.get()
        .uri("/cases/$crn/search")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
