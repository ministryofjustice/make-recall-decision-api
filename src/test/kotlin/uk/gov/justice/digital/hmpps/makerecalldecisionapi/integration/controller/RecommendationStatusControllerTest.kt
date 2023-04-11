package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.recommendationStatusRequest

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RecommendationStatusControllerTest() : IntegrationTestBase() {

  @Test
  fun `create a recommendation name`() {
    // given
    createRecommendation()

    // when
    val response = createOrUpdateRecommendation(activate = "NEW_STATUS")

    // then
    assertThat(response.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(response.get("active")).isEqualTo(true)
    assertThat(response.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(response.get("createdByUserFullName")).isEqualTo("some_user")
    assertThat(response.get("created")).isNotNull
    assertThat(response.get("modifiedBy")).isEqualTo(null)
    assertThat(response.get("modified")).isEqualTo(null)
    assertThat(response.get("modifiedByUserFullName")).isEqualTo(null)
    assertThat(response.get("name")).isEqualTo("NEW_STATUS")
  }

  @Test
  fun `update a recommendation name`() {
    // given
    createRecommendation()
    createOrUpdateRecommendation(activate = "OLD_STATUS")
    createOrUpdateRecommendation(activate = "NEW_STATUS", deactivate = "OLD_STATUS")

    // when
    val response = convertResponseToJSONArray(
      webTestClient.get()
        .uri("/recommendations/$createdRecommendationId/statuses")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")))) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    assertThat(response.length()).isEqualTo(2)
    val deactivated = JSONObject(response.get(1).toString())
    val activated = JSONObject(response.get(0).toString())

    // and
    assertThat(activated.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(activated.get("active")).isEqualTo(true)
    assertThat(activated.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(activated.get("createdByUserFullName")).isEqualTo("some_user")
    assertThat(activated.get("created")).isNotNull
    assertThat(activated.get("modifiedBy")).isEqualTo(null)
    assertThat(activated.get("modified")).isEqualTo(null)
    assertThat(activated.get("modifiedByUserFullName")).isEqualTo(null)
    assertThat(activated.get("name")).isEqualTo("NEW_STATUS")

    // and
    assertThat(deactivated.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(deactivated.get("active")).isEqualTo(false)
    assertThat(deactivated.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(deactivated.get("createdByUserFullName")).isEqualTo("some_user")
    assertThat(deactivated.get("created")).isNotNull
    assertThat(deactivated.get("modifiedBy")).isEqualTo("SOME_USER")
    assertThat(deactivated.get("modified")).isNotNull
    assertThat(deactivated.get("modifiedByUserFullName")).isEqualTo("some_user")
    assertThat(deactivated.get("name")).isEqualTo("OLD_STATUS")
  }

  private fun createOrUpdateRecommendation(activate: String, deactivate: String? = null) =
    convertResponseToJSONObject(
      webTestClient.patch()
        .uri("/recommendations/$createdRecommendationId/status")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationStatusRequest(activate = activate, deactivate = deactivate))
        )
        .headers {
          (
            listOf(
              it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO"))
            )
            )
        }
        .exchange()
        .expectStatus().isOk
    )

  private fun createRecommendation() {
    oasysAssessmentsResponse(crn)
    userAccessAllowed(crn)
    personalDetailsResponseOneTimeOnly(crn)
    mappaDetailsResponse(crn, category = 1, level = 1)
    convictionResponse(crn, "011")
    licenceConditionsResponse(crn, 2500614567)
    personalDetailsResponseOneTimeOnly(crn)
    deleteAndCreateRecommendation()
  }
}
