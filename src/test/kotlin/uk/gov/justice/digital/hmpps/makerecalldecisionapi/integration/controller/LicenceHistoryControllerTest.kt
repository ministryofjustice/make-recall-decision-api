package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.contactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.emptyContactSummaryResponse

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class LicenceHistoryControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  @Test
  fun `retrieves licence history details`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(
        crn,
        contactSummaryResponse()
      )
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.releaseSummary.lastRecall.date").isEqualTo("2020-10-15")
        .jsonPath("$.contactSummary[0].contactStartDate").isEqualTo("2022-06-03T07:00:00")
        .jsonPath("$.contactSummary[0].descriptionType").isEqualTo("Registration Review")
        .jsonPath("$.contactSummary[0].outcome").isEmpty()
        .jsonPath("$.contactSummary[0].notes").isEqualTo("Comment added by John Smith on 05/05/2022")
        .jsonPath("$.contactSummary[0].enforcementAction").isEmpty()
        .jsonPath("$.contactSummary[1].contactStartDate").isEqualTo("2022-05-10T10:39:00")
        .jsonPath("$.contactSummary[1].descriptionType").isEqualTo("Police Liaison")
        .jsonPath("$.contactSummary[1].outcome").isEqualTo("Test - Not Clean / Not Acceptable / Unsuitable")
        .jsonPath("$.contactSummary[1].notes").isEqualTo("This is a test")
        .jsonPath("$.contactSummary[1].enforcementAction").isEqualTo("Enforcement Letter Requested")
    }
  }

  @Test
  fun `retrieves all licence history details`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(
        crn,
        contactSummaryResponse(),
        false
      )
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/all-licence-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.releaseSummary.lastRecall.date").isEqualTo("2020-10-15")
        .jsonPath("$.contactSummary[0].contactStartDate").isEqualTo("2022-06-03T07:00:00")
        .jsonPath("$.contactSummary[0].descriptionType").isEqualTo("Registration Review")
        .jsonPath("$.contactSummary[0].outcome").isEmpty()
        .jsonPath("$.contactSummary[0].notes").isEqualTo("Comment added by John Smith on 05/05/2022")
        .jsonPath("$.contactSummary[0].enforcementAction").isEmpty()
        .jsonPath("$.contactSummary[1].contactStartDate").isEqualTo("2022-05-10T10:39:00")
        .jsonPath("$.contactSummary[1].descriptionType").isEqualTo("Police Liaison")
        .jsonPath("$.contactSummary[1].outcome").isEqualTo("Test - Not Clean / Not Acceptable / Unsuitable")
        .jsonPath("$.contactSummary[1].notes").isEqualTo("This is a test")
        .jsonPath("$.contactSummary[1].enforcementAction").isEqualTo("Enforcement Letter Requested")
    }
  }

  @Test
  fun `given empty contact history then handle licence history response`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn, emptyContactSummaryResponse())
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.releaseSummary.lastRecall.date").isEqualTo("2020-10-15")
        .jsonPath("$.contactSummary").isArray()
        .jsonPath("$.contactSummary.length()").isEqualTo("0")
    }
  }

  @Test
  fun `given no custody release response 400 error then handle licence history response`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(
        crn,
        contactSummaryResponse()
      )
      releaseSummaryResponseWithStatusCode(
        crn,
        uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.releaseSummaryResponse(),
        400
      )

      webTestClient.get()
        .uri("/cases/$crn/licence-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.releaseSummary.lastRelease").isEmpty()
        .jsonPath("$.releaseSummary.lastRecall").isEmpty()
        .jsonPath("$.contactSummary").isArray()
        .jsonPath("$.contactSummary.length()").isEqualTo("2")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on contact summary endpoint`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn, contactSummary = contactSummaryResponse(), delaySeconds = nDeliusTimeout + 2)
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/licence-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - contact summary endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on release summary endpoint`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn, contactSummaryResponse())
      releaseSummaryResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/licence-history")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - release summary endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runBlockingTest {
      val crn = "X123456"
      webTestClient.get()
        .uri("/cases/$crn/licence-history")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
