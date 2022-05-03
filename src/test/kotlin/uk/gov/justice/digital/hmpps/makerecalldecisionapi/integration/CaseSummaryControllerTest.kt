package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class CaseSummaryControllerTest : IntegrationTestBase() {

  @Test
  fun `retrieves simple case summary details`() {
    runBlockingTest {
      val crn = "X123456"
      unallocatedOffenderSearchResponse(crn)
      webTestClient.get()
        .uri("/cases/$crn/search")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.name").isEqualTo("Pontius Pilate")
        .jsonPath("$.dateOfBirth").isEqualTo("2000-11-09")
        .jsonPath("$.crn").isEqualTo(crn)
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runBlockingTest {
      val crn = "X123456"
      unallocatedOffenderSearchResponse(crn)
      webTestClient.get()
        .uri("/cases/$crn/search")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
