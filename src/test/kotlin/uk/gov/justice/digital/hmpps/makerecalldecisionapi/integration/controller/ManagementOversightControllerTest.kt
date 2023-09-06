package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationForNoRecallRequest

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
        .expectStatus().isOk,
    )

    // then
    assertThat(response.get("notes")).isEqualTo(
      "John Smith said:\n" +
        "details of no recall selected\n" +
        "View the case summary for John Smith: environment-host/cases/A12345/overview",
    )
    assertThat(response.get("sensitive")).isEqualTo(false)
  }

  @Test
  fun `handles scenario where no management oversight exists for given id on update`() {
    createRecommendation()
    updateRecommendation(recommendationRequest = updateRecommendationForNoRecallRequest())
    webTestClient.get()
      .uri("/managementOversight/$crn")
      .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")))) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
      .jsonPath("$.userMessage")
      .isEqualTo("No management oversight available: No management oversight available for crn:A12345")
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
