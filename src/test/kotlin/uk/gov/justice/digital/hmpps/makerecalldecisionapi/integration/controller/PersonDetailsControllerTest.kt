package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class PersonDetailsControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  val crn = "A12345"

  @Test
  fun `retrieves person details`() {
    runBlockingTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.currentAddress.line1").isEqualTo("HMPPS Digital Studio 33 Scotland Street")
        .jsonPath("$.currentAddress.line2").isEqualTo("Sheffield City Centre")
        .jsonPath("$.currentAddress.town").isEqualTo("Sheffield")
        .jsonPath("$.currentAddress.postcode").isEqualTo("S3 7BS")
        .jsonPath("$.offenderManager.name").isEqualTo("Sheila Linda Hancock")
        .jsonPath("$.offenderManager.phoneNumber").isEqualTo("09056714321")
        .jsonPath("$.offenderManager.email").isEqualTo("first.last@digital.justice.gov.uk")
        .jsonPath("$.offenderManager.probationTeam.code").isEqualTo("C01T04")
        .jsonPath("$.offenderManager.probationTeam.label").isEqualTo("OMU A")
    }
  }

  @Test
  fun `handles scenario where no person exists matching crn`() {
    runBlockingTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponseWithNoOffender(crn)

      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
        .jsonPath("$.userMessage")
        .isEqualTo("No personal details available: No details available for crn: A12345")
    }
  }

  @Test
  fun `given case is excluded then only return user access details`() {
    runBlockingTest {
      userAccessRestricted(crn)

      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(true)
        .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(false)
        .jsonPath("$.userAccessResponse.restrictionMessage").isEqualTo("You are restricted from viewing this offender record. Please contact OM John Smith")
        .jsonPath("$.userAccessResponse.exclusionMessage").isEmpty
        .jsonPath("$.personalDetailsOverview").isEmpty
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout`() {
    runBlockingTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(GATEWAY_TIMEOUT.value())
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
//        .uri("/cases/$crn/personalDetailsOverview")
//        .exchange()
//        .expectStatus()
//        .isUnauthorized
//    }
//  }
}
