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
class DeleteRecommendationRationaleControllerTest() : IntegrationTestBase() {

  @Test
  fun `get expired recommendation deletion info`() {
    // given
    createRecommendation()
    updateRecommendation()
    createOrUpdateRecommendationStatus(activate = "REC_DELETED", anotherToActivate = "ANOTHER_STATUS", subject = "prince herbert")

    // when
    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/system-delete-recommendation/$crn")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")))) }
        .exchange()
        .expectStatus().isOk,
    )

    // then
    assertThat(response.get("notes")).isEqualTo(
      "Recall Recommendation has been deleted due to Recommendation has expired\n" +
        "View the case summary for John Smith: environment-host/cases/A12345/overview",
    )
    assertThat(response.get("sensitive")).isEqualTo(false)
  }

  @Test
  fun `get delete recommendation rationale`() {
    // given
    createRecommendation()
    updateRecommendation()
    createOrUpdateRecommendationStatus(activate = "REC_DELETED", anotherToActivate = "ANOTHER_STATUS", subject = "prince herbert")

    // when
    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/delete-recommendation-rationale/$crn")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")))) }
        .exchange()
        .expectStatus().isOk,
    )

    // then
    assertThat(response.get("notes")).isEqualTo(
      "prince herbert said:\n" +
        "My wonderful rationale for deleting the recommendation\n" +
        "View the case summary for John Smith: environment-host/cases/A12345/overview",
    )
    assertThat(response.get("sensitive")).isEqualTo(false)
  }

  @Test
  fun `handles scenario where no deleted recommendation exists for given id on update`() {
    createRecommendation()
    updateRecommendation(recommendationRequest = updateRecommendationForNoRecallRequest())
    webTestClient.get()
      .uri("/delete-recommendation-rationale/$crn")
      .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")))) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
      .jsonPath("$.userMessage")
      .isEqualTo("No deleted recommendation rationale available: No deleted recommendation rationale available for crn:A12345")
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
