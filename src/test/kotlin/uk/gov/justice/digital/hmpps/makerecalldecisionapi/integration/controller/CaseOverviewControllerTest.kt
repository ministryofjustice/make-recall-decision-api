package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class CaseOverviewControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  val staffCode = "STFFCDEU"

  @Test
  fun `retrieves case summary details`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      deleteAndCreateRecommendation()

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.offences.length()").isEqualTo(1)
        .jsonPath("$.offences[0].mainOffence").isEqualTo("true")
        .jsonPath("$.offences[0].description").isEqualTo("Robbery (other than armed robbery)")
        .jsonPath("$.risk.flags.length()").isEqualTo(1)
        .jsonPath("$.risk.flags[0]").isEqualTo("Victim contact")
        .jsonPath("$.activeRecommendation.recommendationId").isEqualTo(createdRecommendationId)
        .jsonPath("$.activeRecommendation.lastModifiedDate").isNotEmpty
        .jsonPath("$.activeRecommendation.lastModifiedBy").isEqualTo("SOME_USER")
    }
  }

  @Test
  fun `returns empty offences list where where no active convictions exist`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      noActiveConvictionResponse(crn)
      registrationsResponse()

      val result = webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.offences.length()").isEqualTo(0)
    }
  }

  @Test
  fun `given case is excluded then only return user access details`() {
    runTest {
      userAccessExcluded(crn)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.userAccessResponse.userRestricted").isEqualTo(false)
        .jsonPath("$.userAccessResponse.userExcluded").isEqualTo(true)
        .jsonPath("$.userAccessResponse.exclusionMessage").isEqualTo("You are excluded from viewing this offender record. Please contact OM John Smith")
        .jsonPath("$.userAccessResponse.restrictionMessage").isEmpty
        .jsonPath("$.personalDetailsOverview").isEmpty
    }
  }

  @Test
  fun `given case has no recommendation in database then do not return any active recommendation`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      // Delete recommendation from database if one was created in previous tests
      deleteRecommendation()

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.activeRecommendation").isEmpty()
    }
  }

  @Test
  fun `given case has recommendation in non-draft state in database then do not return any active recommendation`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      deleteAndCreateRecommendation()
      updateRecommendation(Status.DOCUMENT_CREATED)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.activeRecommendation").isEmpty()
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on convictions endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn, delaySeconds = nDeliusTimeout + 2)
      convictionResponse(crn, staffCode)
      registrationsResponse()

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - all offenders endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on all offenders endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode, delaySeconds = nDeliusTimeout + 2)
      registrationsResponse()

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - convictions endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on registrations endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse(delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - registrations endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runTest {
      webTestClient.get()
        .uri("/cases/$crn/overview")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
