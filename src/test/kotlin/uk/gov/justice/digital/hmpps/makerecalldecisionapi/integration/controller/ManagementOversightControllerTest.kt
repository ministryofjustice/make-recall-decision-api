package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationForNoRecallRequest

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class ManagementOversightControllerTest : IntegrationTestBase() {

  @Test
  fun `get management oversight`() {
    // given
    createRecommendation()
    updateRecommendation()

    // when
    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/managementOversight/$crn")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")))) }
        .exchange()
        .expectStatus().isOk,
    )

    // then
    assertThat(response.get("notes")).isEqualTo(
      "Joe Bloggs said:\n" +
        "details of no recall selected\n" +
        "View the case summary for Joe Bloggs: environment-host/cases/A12345/overview",
    )
    assertThat(response.get("sensitive")).isEqualTo(false)
  }

  @Test
  fun `get consideration rationale`() {
    // given
    createRecommendation()

    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue("{\"triggerLeadingToRecall\": \"some trigger\", \"responseToProbation\": \"some response\", \"sendConsiderationRationaleToDelius\": true, \"considerationSensitive\": true}"),
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().is2xxSuccessful

    // when
    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/recallConsiderationRationale/$crn")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")))) }
        .exchange()
        .expectStatus().isOk,
    )

    // then
    val notes = response.get("notes") as String
    assertThat(notes).contains("some_user said")
    assertThat(notes).contains("some trigger")
    assertThat(notes).contains("some response")
    assertThat(notes).contains("View the case summary for Joe Bloggs: environment-host/cases/A12345/overview")

    assertThat(response.get("sensitive")).isEqualTo(true)
  }

  @Test
  fun `handles scenario where no management oversight exists for given id on update`() {
    createRecommendation()
    updateRecommendation(recommendationRequest = updateRecommendationForNoRecallRequest())
    webTestClient.get()
      .uri("/managementOversight/$crn")
      .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")))) }
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
