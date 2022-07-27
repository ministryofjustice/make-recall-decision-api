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
      releaseSummaryResponse(crn)
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
        .jsonPath("$.convictions.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].offences.length()").isEqualTo(1)
        .jsonPath("$.convictions[0].offences[0].mainOffence").isEqualTo("true")
        .jsonPath("$.convictions[0].offences[0].description").isEqualTo("Robbery (other than armed robbery)")
        .jsonPath("$.convictions[0].sentenceDescription").isEqualTo("sentence description")
        .jsonPath("$.convictions[0].sentenceOriginalLength").isEqualTo("12")
        .jsonPath("$.convictions[0].sentenceOriginalLengthUnits").isEqualTo("days")
        .jsonPath("$.convictions[0].licenceExpiryDate").isEqualTo("2020-06-25")
        .jsonPath("$.convictions[0].sentenceExpiryDate").isEqualTo("2020-06-28")
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
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
      releaseSummaryResponse(crn)

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
        .jsonPath("$.convictions.length()").isEqualTo(0)
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
      releaseSummaryResponse(crn)
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
      releaseSummaryResponse(crn)
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
      releaseSummaryResponse(crn)

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
      releaseSummaryResponse(crn)

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
      releaseSummaryResponse(crn)

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
  fun `gateway timeout 503 given on Community Api timeout on release summary endpoint`() {
    runTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      convictionResponse(crn, staffCode)
      registrationsResponse()
      releaseSummaryResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - release summary endpoint: [No response within $nDeliusTimeout seconds]")
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
