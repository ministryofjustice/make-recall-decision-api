package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
  fun `retrieves simple case summary details`() {
    runBlockingTest {
      val crn = "X123456"
      offenderSearchResponse(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
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
  fun `given excluded case for my user then change the name to Limited access`() {
    runBlockingTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userAccessExcluded(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("Limited access")
        .jsonPath("$[0].dateOfBirth").isEqualTo(null)
        .jsonPath("$[0].crn").isEqualTo(crn)
    }
  }

  @Test
  fun `given excluded case but not for my user then fill in the missing name details from community API all endpoint`() {
    runBlockingTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("John Smith")
        .jsonPath("$[0].dateOfBirth").isEqualTo(null)
        .jsonPath("$[0].crn").isEqualTo(crn)
    }
  }

  @Test
  fun `given restricted case for my user then change the name to Limited access`() {
    runBlockingTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userAccessRestricted(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("Limited access")
        .jsonPath("$[0].dateOfBirth").isEqualTo(null)
        .jsonPath("$[0].crn").isEqualTo(crn)
    }
  }

  @Test
  fun `gateway timeout 503 given on Offender Search Api timeout on offenders search by phrase endpoint`() {
    runBlockingTest {
      val crn = "X123456"
      offenderSearchResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
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
    runBlockingTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userAccessAllowed(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
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
  fun `gateway timeout 503 given on Community Api timeout on all endpoint`() {
    runBlockingTest {
      val crn = "X123456"
      limitedAccessPractitionerOffenderSearchResponse(crn)
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - all offenders endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

//  @Test
//  fun `access denied when insufficient privileges used`() {
//    runBlockingTest {
//      val crn = "X123456"
//      offenderSearchResponse(crn)
//      webTestClient.get()
//        .uri("/cases/$crn/search")
//        .exchange()
//        .expectStatus()
//        .isUnauthorized
//    }
//  }
}
