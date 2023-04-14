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
  fun `create a recommendation statuses`() {
    // given
    createRecommendation()

    // when
    val response = createOrUpdateRecommendation(activate = "NEW_STATUS", anotherToActivate = "ANOTHER_NEW_STATUS")

    // then
    val newStatus = JSONObject(response.get(0).toString())
    assertThat(newStatus.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(newStatus.get("active")).isEqualTo(true)
    assertThat(newStatus.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(newStatus.get("createdByUserFullName")).isEqualTo("some_user")
    assertThat(newStatus.get("created")).isNotNull
    assertThat(newStatus.get("modifiedBy")).isEqualTo(null)
    assertThat(newStatus.get("modified")).isEqualTo(null)
    assertThat(newStatus.get("modifiedByUserFullName")).isEqualTo(null)
    assertThat(newStatus.get("name")).isEqualTo("NEW_STATUS")

    // and
    val anotherNewStatus = JSONObject(response.get(1).toString())
    assertThat(anotherNewStatus.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(anotherNewStatus.get("active")).isEqualTo(true)
    assertThat(anotherNewStatus.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(anotherNewStatus.get("createdByUserFullName")).isEqualTo("some_user")
    assertThat(anotherNewStatus.get("created")).isNotNull
    assertThat(anotherNewStatus.get("modifiedBy")).isEqualTo(null)
    assertThat(anotherNewStatus.get("modified")).isEqualTo(null)
    assertThat(anotherNewStatus.get("modifiedByUserFullName")).isEqualTo(null)
    assertThat(anotherNewStatus.get("name")).isEqualTo("ANOTHER_NEW_STATUS")
  }

  @Test
  fun `update a recommendation statuses`() {
    // given
    createRecommendation()
    createOrUpdateRecommendation(activate = "OLD_STATUS", anotherToActivate = "ANOTHER_OLD_STATUS") // 2 here
    createOrUpdateRecommendation(activate = "NEW_STATUS", anotherToActivate = "ANOTHER_NEW_STATUS", deactivate = "OLD_STATUS", anotherToDeactivate = "ANOTHER_OLD_STATUS") // 3 here

    // when
    val response = convertResponseToJSONArray(
      webTestClient.get()
        .uri("/recommendations/$createdRecommendationId/statuses")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")))) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    assertThat(response.length()).isEqualTo(4)
    val newStatusActivated = JSONObject(response.get(0).toString())
    val anotherNewStatusActivated = JSONObject(response.get(1).toString())
    val oldStatusDeactivated = JSONObject(response.get(2).toString())
    val anotherOldStatusDeactivated = JSONObject(response.get(3).toString())

    // and
    assertThat(newStatusActivated.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(newStatusActivated.get("active")).isEqualTo(true)
    assertThat(newStatusActivated.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(newStatusActivated.get("createdByUserFullName")).isEqualTo("some_user")
    assertThat(newStatusActivated.get("created")).isNotNull
    assertThat(newStatusActivated.get("modifiedBy")).isEqualTo(null)
    assertThat(newStatusActivated.get("modified")).isEqualTo(null)
    assertThat(newStatusActivated.get("modifiedByUserFullName")).isEqualTo(null)
    assertThat(newStatusActivated.get("name")).isEqualTo("NEW_STATUS")

    // and
    assertThat(anotherNewStatusActivated.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(anotherNewStatusActivated.get("active")).isEqualTo(true)
    assertThat(anotherNewStatusActivated.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(anotherNewStatusActivated.get("createdByUserFullName")).isEqualTo("some_user")
    assertThat(anotherNewStatusActivated.get("created")).isNotNull
    assertThat(anotherNewStatusActivated.get("modifiedBy")).isEqualTo(null)
    assertThat(anotherNewStatusActivated.get("modified")).isEqualTo(null)
    assertThat(anotherNewStatusActivated.get("modifiedByUserFullName")).isEqualTo(null)
    assertThat(anotherNewStatusActivated.get("name")).isEqualTo("ANOTHER_NEW_STATUS")

    // and
    assertThat(oldStatusDeactivated.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(oldStatusDeactivated.get("active")).isEqualTo(false)
    assertThat(oldStatusDeactivated.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(oldStatusDeactivated.get("createdByUserFullName")).isEqualTo("some_user")
    assertThat(oldStatusDeactivated.get("created")).isNotNull
    assertThat(oldStatusDeactivated.get("modifiedBy")).isEqualTo("SOME_USER")
    assertThat(oldStatusDeactivated.get("modified")).isNotNull
    assertThat(oldStatusDeactivated.get("modifiedByUserFullName")).isEqualTo("some_user")
    assertThat(oldStatusDeactivated.get("name")).isEqualTo("OLD_STATUS")

    // and
    assertThat(anotherOldStatusDeactivated.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(anotherOldStatusDeactivated.get("active")).isEqualTo(false)
    assertThat(anotherOldStatusDeactivated.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(anotherOldStatusDeactivated.get("createdByUserFullName")).isEqualTo("some_user")
    assertThat(anotherOldStatusDeactivated.get("created")).isNotNull
    assertThat(anotherOldStatusDeactivated.get("modifiedBy")).isEqualTo("SOME_USER")
    assertThat(anotherOldStatusDeactivated.get("modified")).isNotNull
    assertThat(anotherOldStatusDeactivated.get("modifiedByUserFullName")).isEqualTo("some_user")
    assertThat(anotherOldStatusDeactivated.get("name")).isEqualTo("ANOTHER_OLD_STATUS")
  }

  @Test
  fun `get recommendation statuses when only old status exists`() {
    // given
    createRecommendation()

    // when
    val response = convertResponseToJSONArray(
      webTestClient.get()
        .uri("/recommendations/$createdRecommendationId/statuses")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")))) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    assertThat(response.length()).isEqualTo(1)
    val oldStatus = JSONObject(response.get(0).toString())

    // and
    assertThat(oldStatus.get("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(oldStatus.get("active")).isEqualTo(true)
    assertThat(oldStatus.get("createdBy")).isEqualTo("SOME_USER")
    assertThat(oldStatus.get("createdByUserFullName")).isEqualTo(null)
    assertThat(oldStatus.get("created")).isNotNull
    assertThat(oldStatus.get("modifiedBy")).isEqualTo(null)
    assertThat(oldStatus.get("modified")).isEqualTo(null)
    assertThat(oldStatus.get("modifiedByUserFullName")).isEqualTo(null)
    assertThat(oldStatus.get("name")).isEqualTo("DRAFT")
  }

  private fun createOrUpdateRecommendation(activate: String, anotherToActivate: String? = null, deactivate: String? = null, anotherToDeactivate: String? = null) =
    convertResponseToJSONArray(
      webTestClient.patch()
        .uri("/recommendations/$createdRecommendationId/status")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationStatusRequest(activate = activate, anotherToActivate = anotherToActivate, deactivate = deactivate, anotherToDeactivate = anotherToDeactivate))
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
