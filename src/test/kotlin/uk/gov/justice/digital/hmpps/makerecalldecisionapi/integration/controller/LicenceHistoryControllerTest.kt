package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class LicenceHistoryControllerTest : IntegrationTestBase() {

  @Test
  fun `retrieves licence history details`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      contactSummaryResponse(crn)
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
  fun `access denied when insufficient privileges used`() {
    runBlockingTest {
      val crn = "X123456"
      unallocatedOffenderSearchResponse(crn)
      webTestClient.get()
        .uri("/cases/$crn/licence-history")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
