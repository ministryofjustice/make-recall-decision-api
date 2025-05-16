package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
@Deprecated(
  "Endpoints are deprecated and replaced with POST on paged-search",
  ReplaceWith(""),
  DeprecationLevel.WARNING,
)
class OffenderSearchControllerDeprecatedTest : IntegrationTestBase() {

  @Test
  fun `backward compatible endpoint retrieves simple case summary details using crn`() {
    runTest {
      val crn = "X123456"
      val firstName = "Pontius"
      val lastName = "Pilate"
      val dateOfBirth = "2000-11-30"
      findByCrnSuccess(crn, firstName, lastName, dateOfBirth)
      webTestClient.get()
        .uri("/search?crn=$crn")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].name").isEqualTo("$firstName $lastName")
        .jsonPath("$[0].dateOfBirth").isEqualTo(dateOfBirth)
        .jsonPath("$[0].crn").isEqualTo(crn)
    }
  }

  @Test
  fun `access denied on deprecated search endpoint when insufficient privileges used`() {
    runTest {
      webTestClient.get()
        .uri("/search?crn=$crn")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
