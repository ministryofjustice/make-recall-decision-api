package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class PersonDetailsControllerTest : IntegrationTestBase() {

  @Test
  fun `retrieves person details`() {
    runBlockingTest {
      val crn = "A12345"
      allOffenderDetailsResponse(crn)
      registrationsResponse(crn)

      webTestClient.get()
        .uri("/cases/$crn/personal-details")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.personalDetailsOverview.name").isEqualTo("John Smith")
        .jsonPath("$.personalDetailsOverview.dateOfBirth").isEqualTo("1982-10-24")
        .jsonPath("$.personalDetailsOverview.age").isEqualTo("39")
        .jsonPath("$.personalDetailsOverview.gender").isEqualTo("Male")
        .jsonPath("$.personalDetailsOverview.crn").isEqualTo(crn)
        .jsonPath("$.currentAddress.line1").isEqualTo("32 HMPPS Digital Studio")
        .jsonPath("$.currentAddress.line2").isEqualTo("Sheffield City Centre")
        .jsonPath("$.currentAddress.town").isEqualTo("Sheffield")
        .jsonPath("$.currentAddress.postcode").isEqualTo("S3 7BS")
        .jsonPath("$.offenderManager.name").isEqualTo("Sheila Linda Hancock")
        .jsonPath("$.offenderManager.phoneNumber").isEqualTo("09056714321")
        .jsonPath("$.offenderManager.email").isEqualTo("first.last@digital.justice.gov.uk")
        .jsonPath("$.offenderManager.probationTeam.code").isEqualTo("C01T04")
        .jsonPath("$.offenderManager.probationTeam.label").isEqualTo("OMU A")
        .jsonPath("$.risk.flags.length()").isEqualTo(1)
        .jsonPath("$.risk.flags[0]").isEqualTo("Victim contact")
    }
  }

  @Test
  fun `access denied when insufficient privileges used`() {
    runBlockingTest {
      val crn = "X123456"
      unallocatedOffenderSearchResponse(crn)
      webTestClient.get()
        .uri("/cases/$crn/personalDetailsOverview")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }
  }
}
