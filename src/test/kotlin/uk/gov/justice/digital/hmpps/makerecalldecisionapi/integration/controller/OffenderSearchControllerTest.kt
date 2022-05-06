package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class OffenderSearchControllerTest : IntegrationTestBase() {

  @Test
  fun `retrieves simple case summary details`() {
    runBlockingTest {
      val crn = "X123456"
      unallocatedOffenderSearchResponse(crn)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[0].name").isEqualTo("Pontius Pilate")
        .jsonPath("$[0].dateOfBirth").isEqualTo("2000-11-09")
        .jsonPath("$[0].crn").isEqualTo(crn)
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
