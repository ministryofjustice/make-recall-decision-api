package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class ManagementOversightControllerTest() : IntegrationTestBase() {

  @Test
  fun `get management oversight`() {
    // given
    createRecommendation()
    updateRecommendation()

    // when
    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/managementOversight/$crn")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")))) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    assertThat(response.get("notes")).isEqualTo("Comment added by some_user on 01/01/2023 at 15:00: some_user entered the following into the service 'Decide if someone should be recalled or not': details of no recall selected View the case summary: environment-host/cases/A12345/overview")
    assertThat(response.get("sensitive")).isEqualTo(false)
  }

  private fun createRecommendation() {
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    personalDetailsResponseOneTimeOnly(crn)
    licenceConditionsResponse(crn, 2500614567)
    personalDetailsResponseOneTimeOnly(crn)
    deleteAndCreateRecommendation()
  }
}
