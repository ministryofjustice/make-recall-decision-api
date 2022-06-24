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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.release.releaseSummaryDeliusResponse

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class ContactHistoryControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  val crn = "A12345"

  @Test
  fun `retrieves all contact history details`() {
    runBlockingTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(
        crn,
        contactSummaryResponse()
      )
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.releaseSummary.lastRelease.date").isEqualTo("2017-09-15")
        .jsonPath("$.releaseSummary.lastRecall.date").isEqualTo("2020-10-15")
        .jsonPath("$.contactSummary[0].contactStartDate").isEqualTo("2022-06-03T07:00:00Z")
        .jsonPath("$.contactSummary[0].descriptionType").isEqualTo("Registration Review")
        .jsonPath("$.contactSummary[0].outcome").isEmpty()
        .jsonPath("$.contactSummary[0].sensitive").isEmpty()
        .jsonPath("$.contactSummary[0].notes").isEqualTo("Comment added by John Smith on 05/05/2022")
        .jsonPath("$.contactSummary[0].enforcementAction").isEmpty()
        .jsonPath("$.contactSummary[1].contactStartDate").isEqualTo("2022-05-10T10:39:00Z")
        .jsonPath("$.contactSummary[1].descriptionType").isEqualTo("Police Liaison")
        .jsonPath("$.contactSummary[1].outcome").isEqualTo("Test - Not Clean / Not Acceptable / Unsuitable")
        .jsonPath("$.contactSummary[1].sensitive").isEqualTo(true)
        .jsonPath("$.contactSummary[1].notes").isEqualTo("This is a test")
        .jsonPath("$.contactSummary[1].enforcementAction").isEqualTo("Enforcement Letter Requested")
        .jsonPath("$.contactTypeGroups.length()").isEqualTo(2)
        .jsonPath("$.contactTypeGroups[0].groupId").isEqualTo("1")
        .jsonPath("$.contactTypeGroups[0].label").isEqualTo("Appointment")
        .jsonPath("$.contactTypeGroups[0].contactTypeCodes[0]").isEqualTo("COAP")
        .jsonPath("$.contactTypeGroups[1].groupId").isEqualTo("6")
        .jsonPath("$.contactTypeGroups[1].label").isEqualTo("Police")
        .jsonPath("$.contactTypeGroups[1].contactTypeCodes[0]").isEqualTo("C204")
    }
  }

  @Test
  fun `given empty contact history then handle response`() {
    runBlockingTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn, emptyContactSummaryResponse())
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
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
        .jsonPath("$.contactTypeGroups.length()").isEqualTo(0)
    }
  }

  @Test
  fun `given case is excluded then only return user access details`() {
    runBlockingTest {
      userAccessExcluded(crn)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
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
  fun `given no custody release response 400 error then handle contact history response`() {
    runBlockingTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(
        crn,
        contactSummaryResponse()
      )
      releaseSummaryResponseWithStatusCode(
        crn,
        releaseSummaryDeliusResponse(),
        400
      )

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
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
        .jsonPath("$.contactTypeGroups.length()").isEqualTo("2")
    }
  }

  @Test
  fun `gateway timeout 503 given on Community Api timeout on contact summary endpoint`() {
    runBlockingTest {
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn, contactSummary = contactSummaryResponse(), delaySeconds = nDeliusTimeout + 2)
      releaseSummaryResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
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
      userAccessAllowed(crn)
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn, contactSummaryResponse())
      releaseSummaryResponse(crn, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/contact-history")
        .headers { it.authToken() }
//        .headers { it.authToken(roles = listOf("ROLE_PROBATION")) }
//        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Community API Client - release summary endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }

//  @Test
//  fun `access denied when insufficient privileges used`() {
//    runBlockingTest {
//      userAccessAllowed(crn)
//      webTestClient.get()
//        .uri("/cases/$crn/contact-history")
//        .exchange()
//        .expectStatus()
//        .isUnauthorized
//    }
//  }
}
