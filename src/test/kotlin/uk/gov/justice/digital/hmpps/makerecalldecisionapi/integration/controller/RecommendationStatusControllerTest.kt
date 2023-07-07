package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequest

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RecommendationStatusControllerTest() : IntegrationTestBase() {

  @Test
  fun `create recommendation statuses`() {
    // given
    userResponse("some_user", "test@digital.justice.gov.uk")
    createRecommendation()

    // when
    val response = createOrUpdateRecommendationStatus(activate = "NEW_STATUS", anotherToActivate = "ANOTHER_NEW_STATUS")

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
  fun `update recommendation statuses`() {
    // given
    createRecommendation()
    createOrUpdateRecommendationStatus(activate = "OLD_STATUS", anotherToActivate = "ANOTHER_OLD_STATUS") // 2 here
    createOrUpdateRecommendationStatus(activate = "NEW_STATUS", anotherToActivate = "ANOTHER_NEW_STATUS", deactivate = "OLD_STATUS", anotherToDeactivate = "ANOTHER_OLD_STATUS") // 3 here

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
  fun `update recommendation statuses adds email address on ACO_SIGNED`() {
    // given
    userResponse("some_user", "test@digital.justice.gov.uk")
    createRecommendation()
    createOrUpdateRecommendationStatus(activate = "OLD_STATUS", anotherToActivate = "ANOTHER_OLD_STATUS") // 2 here
    createOrUpdateRecommendationStatus(activate = "ACO_SIGNED", anotherToActivate = "ANOTHER_NEW_STATUS", deactivate = "OLD_STATUS", anotherToDeactivate = "ANOTHER_OLD_STATUS") // 3 here

    // when
    val expectBody = webTestClient.get()
      .uri("/recommendations/$createdRecommendationId/statuses")
      .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")))) }
      .exchange()
      .expectStatus().isOk
      .expectBody()

    // then
    expectBody
      .jsonPath("$[0].name").isEqualTo("ACO_SIGNED")
      .jsonPath("$[0].emailAddress").isEqualTo("test@digital.justice.gov.uk")
      .jsonPath("$[1].emailAddress").doesNotExist()
      .jsonPath("$[2].emailAddress").doesNotExist()
      .jsonPath("$[3].emailAddress").doesNotExist()
  }

  @Test
  fun `update recommendation statuses adds email address on SPO_SIGNED`() {
    // given
    userResponse("some_user", "test@digital.justice.gov.uk")
    createRecommendation()
    createOrUpdateRecommendationStatus(activate = "OLD_STATUS", anotherToActivate = "ANOTHER_OLD_STATUS") // 2 here
    createOrUpdateRecommendationStatus(activate = "SPO_SIGNED", anotherToActivate = "ANOTHER_NEW_STATUS", deactivate = "OLD_STATUS", anotherToDeactivate = "ANOTHER_OLD_STATUS") // 3 here

    // when
    val expectBody = webTestClient.get()
      .uri("/recommendations/$createdRecommendationId/statuses")
      .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION_SPO")))) }
      .exchange()
      .expectStatus().isOk
      .expectBody()

    // then
    expectBody
      .jsonPath("$[0].name").isEqualTo("SPO_SIGNED")
      .jsonPath("$[0].emailAddress").isEqualTo("test@digital.justice.gov.uk")
      .jsonPath("$[1].emailAddress").doesNotExist()
      .jsonPath("$[2].emailAddress").doesNotExist()
      .jsonPath("$[3].emailAddress").doesNotExist()
  }

  @Test
  fun `ACO and SPO details present on recommendations response`() {
    // given
    createRecommendation()
    userResponse("spo_user", "spo@domain.com")
    createOrUpdateRecommendationStatus(activate = "SPO_SIGNED", anotherToActivate = "FOO", subject = "spo_user")
    userResponse("aco_user", "aco@domain.com")
    createOrUpdateRecommendationStatus(activate = "ACO_SIGNED", anotherToActivate = "BAR", subject = "aco_user")

    // when
    val recommendation = convertResponseToJSONObject(
      webTestClient.get()
        .uri(
          "/recommendations/$createdRecommendationId"
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    assertThat(recommendation.getString("countersignAcoEmail")).isEqualTo("aco@domain.com")
    assertThat(recommendation.getString("countersignSpoEmail")).isEqualTo("spo@domain.com")
    assertThat(recommendation.getString("countersignAcoName")).isEqualTo("aco_user")
    assertThat(recommendation.getString("countersignSpoName")).isEqualTo("spo_user")
    assertThat(recommendation.getString("countersignSpoDateTime")).isNotNull
    assertThat(recommendation.getString("countersignAcoDateTime")).isNotNull
  }

  @Test
  fun `ACO details present on recommendations response`() {
    // given
    createRecommendation()
    userResponse("aco_user", "aco@domain.com")
    createOrUpdateRecommendationStatus(activate = "ACO_SIGNED", anotherToActivate = "BAR", subject = "aco_user")

    // when
    val recommendation = convertResponseToJSONObject(
      webTestClient.get()
        .uri(
          "/recommendations/$createdRecommendationId"
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    assertThat(recommendation.getString("countersignAcoEmail")).isEqualTo("aco@domain.com")
    assertThat(recommendation.get("countersignSpoEmail")).isEqualTo(null)
    assertThat(recommendation.getString("countersignAcoName")).isEqualTo("aco_user")
    assertThat(recommendation.get("countersignSpoName")).isEqualTo(null)
    assertThat(recommendation.get("countersignSpoDateTime")).isEqualTo(null)
    assertThat(recommendation.getString("countersignAcoDateTime")).isNotNull
  }

  @Test
  fun `SPO details present on recommendations response`() {
    // given
    createRecommendation()
    userResponse("spo_user", "spo@domain.com")
    createOrUpdateRecommendationStatus(activate = "SPO_SIGNED", anotherToActivate = "FOO", subject = "spo_user")

    // when
    val recommendation = convertResponseToJSONObject(
      webTestClient.get()
        .uri(
          "/recommendations/$createdRecommendationId"
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    assertThat(recommendation.get("countersignAcoEmail")).isEqualTo(null)
    assertThat(recommendation.getString("countersignSpoEmail")).isEqualTo("spo@domain.com")
    assertThat(recommendation.get("countersignAcoName")).isEqualTo(null)
    assertThat(recommendation.getString("countersignSpoName")).isEqualTo("spo_user")
    assertThat(recommendation.getString("countersignSpoDateTime")).isNotNull
    assertThat(recommendation.get("countersignAcoDateTime")).isEqualTo(null)
  }

  @Test
  fun `ACO and SPO details both absent on recommendations response`() {
    // given
    createRecommendation()

    // when
    val recommendation = convertResponseToJSONObject(
      webTestClient.get()
        .uri(
          "/recommendations/$createdRecommendationId"
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    assertThat(recommendation.get("countersignAcoEmail")).isEqualTo(null)
    assertThat(recommendation.get("countersignSpoEmail")).isEqualTo(null)
    assertThat(recommendation.get("countersignAcoName")).isEqualTo(null)
    assertThat(recommendation.get("countersignSpoName")).isEqualTo(null)
    assertThat(recommendation.get("countersignSpoDateTime")).isEqualTo(null)
    assertThat(recommendation.get("countersignAcoDateTime")).isEqualTo(null)
  }

  @Test
  fun `statuses list present on recommendations response`() {
    // given
    createRecommendation()
    createOrUpdateRecommendationStatus(activate = "NEW_STATUS", anotherToActivate = "ANOTHER_NEW_STATUS")

    // when
    val recommendations = convertResponseToJSONObject(
      webTestClient.get()
        .uri(
          "/cases/$crn/recommendations"
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    val statuses = recommendations.getJSONArray("recommendations").getJSONObject(0).getJSONArray("statuses")
    assertThat(statuses.getJSONObject(0).getString("createdByUserFullName")).isEqualTo("some_user")
    assertThat(statuses.getJSONObject(0).getString("createdBy")).isEqualTo("SOME_USER")
    assertThat(statuses.getJSONObject(0).getString("created")).isNotNull
    assertThat(statuses.getJSONObject(0).getString("name")).isEqualTo("NEW_STATUS")
    assertThat(statuses.getJSONObject(0).get("modified")).isEqualTo(null)
    assertThat(statuses.getJSONObject(0).get("active")).isEqualTo(true)
    assertThat(statuses.getJSONObject(0).get("modifiedBy")).isEqualTo(null)
    assertThat(statuses.getJSONObject(0).get("modifiedByUserFullName")).isEqualTo(null)
    assertThat(statuses.getJSONObject(0).get("id")).isNotNull
    assertThat(statuses.getJSONObject(0).get("recommendationId")).isNotNull

    // and
    assertThat(statuses.getJSONObject(1).getString("createdByUserFullName")).isEqualTo("some_user")
    assertThat(statuses.getJSONObject(1).getString("createdBy")).isEqualTo("SOME_USER")
    assertThat(statuses.getJSONObject(1).getString("created")).isNotNull
    assertThat(statuses.getJSONObject(1).getString("name")).isEqualTo("ANOTHER_NEW_STATUS")
    assertThat(statuses.getJSONObject(1).get("modified")).isEqualTo(null)
    assertThat(statuses.getJSONObject(1).get("active")).isEqualTo(true)
    assertThat(statuses.getJSONObject(1).get("modifiedBy")).isEqualTo(null)
    assertThat(statuses.getJSONObject(1).get("modifiedByUserFullName")).isEqualTo(null)
    assertThat(statuses.getJSONObject(1).get("id")).isNotNull
    assertThat(statuses.getJSONObject(1).get("recommendationId")).isNotNull
  }

  @Test
  fun `recommendation history id set on statuses for update of recommendation`() {
    // given
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    deleteAndCreateRecommendation()
    recommendationModelResponse(crn)
    createOrUpdateRecommendationStatus(activate = "OLD_STATUS", anotherToActivate = "ANOTHER_OLD_STATUS")
    updateRecommendation(updateRecommendationRequest())
    createOrUpdateRecommendationStatus(activate = "NEW_STATUS", anotherToActivate = "ANOTHER_NEW_STATUS")

    // when
    val response = convertResponseToJSONArray(
      webTestClient.get()
        .uri("/recommendations/$createdRecommendationId/statuses")
        .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION",)))) }
        .exchange()
        .expectStatus().isOk
    )

    // then
    val oldStatusActivated = JSONObject(response.get(0).toString())
    val anotherOldStatusActivated = JSONObject(response.get(1).toString())
    val newStatusActivated = JSONObject(response.get(2).toString())
    val anotherNewStatusActivated = JSONObject(response.get(3).toString())

    // and
    assertThat(newStatusActivated.get("recommendationHistoryId")).isNotNull
    assertThat(anotherNewStatusActivated.get("recommendationHistoryId")).isNotNull
    assertThat(anotherOldStatusActivated.get("recommendationHistoryId")).isEqualTo(null)
    assertThat(oldStatusActivated.get("recommendationHistoryId")).isEqualTo(null)
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
