package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class CaseOverviewControllerTest : IntegrationTestBase() {

  @Test
  fun `retrieves case summary details`() {
    runBlockingTest {
      val crn = "A12345"
      val staffCode = "STFFCDEU"
      allOffenderDetailsResponse(crn)
      unallocatedConvictionResponse(crn, staffCode)

      webTestClient.get()
        .uri("/cases/$crn/overview")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personDetails.name").isEqualTo("John Smith")
        .jsonPath("$.personDetails.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personDetails.age").isEqualTo("39")
        .jsonPath("$.personDetails.gender").isEqualTo("Male")
        .jsonPath("$.personDetails.crn").isEqualTo(crn)
        .jsonPath("$.offences.length()").isEqualTo(1)
        .jsonPath("$.offences[0].mainOffence").isEqualTo("true")
        .jsonPath("$.offences[0].description").isEqualTo("Robbery (other than armed robbery)")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runBlockingTest {
      val crn = "X123456"
      unallocatedOffenderSearchResponse(crn)
      webTestClient.get()
        .uri("/cases/$crn/overview")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
