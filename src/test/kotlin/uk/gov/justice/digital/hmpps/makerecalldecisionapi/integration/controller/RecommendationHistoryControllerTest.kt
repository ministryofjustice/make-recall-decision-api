package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import java.time.LocalDate

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RecommendationHistoryControllerTest() : IntegrationTestBase() {

  @Test
  fun `get recommendation history for a crn`() {
    // given
    val featureFlagString = "{\"flagConsiderRecall\": true }"
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    overviewResponse(crn)
    oasysAssessmentsResponse(crn)
    deleteAndCreateRecommendation(featureFlagString) // creates empty draft

    // and
    val startDate = LocalDate.now().minusDays(1L).toString()
    val endDate = LocalDate.now().plusDays(1L).toString()

    // and
    updateRecommendation(Status.RECALL_CONSIDERED)

    // and
    updateRecommendation(Status.DOCUMENT_DOWNLOADED)

    // when
    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/history/crn/$crn?startDate=$startDate&endDate=$endDate")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange(),
    )

    // then
    assertThat(response.getJSONArray("recommendations").length()).isEqualTo(2)
    assertThat(response.getInt("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(response.getString("crn")).isEqualTo(crn)

    // and
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getString("lastModifiedByUserName")).isEqualTo("some_user")
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getString("createdDate")).isNotNull
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getString("lastModifiedDate")).isNotNull
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getJSONObject("recallType").getJSONObject("selected").getString("value")).isEqualTo("FIXED_TERM")
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getString("status")).isEqualTo("RECALL_CONSIDERED")

    // and
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getString("lastModifiedByUserName")).isEqualTo("some_user")
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getString("createdDate")).isNotNull
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getString("lastModifiedDate")).isNotNull
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getJSONObject("recallType").getJSONObject("selected").getString("value")).isEqualTo("FIXED_TERM")
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getString("status")).isEqualTo("DOCUMENT_DOWNLOADED")
  }

  @Test
  fun `get recommendation history for a nomsId`() {
    // given
    val featureFlagString = "{\"flagConsiderRecall\": true }"
    val nomsId = 123
    offenderSearchByNomsIdResponse(nomsNumber = nomsId)
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    overviewResponse(crn)
    oasysAssessmentsResponse(crn)
    deleteAndCreateRecommendation(featureFlagString) // creates empty draft

    // and
    val startDate = LocalDate.now().minusDays(1L).toString()
    val endDate = LocalDate.now().plusDays(1L).toString()

    // and
    updateRecommendation(Status.RECALL_CONSIDERED)

    // and
    updateRecommendation(Status.DOCUMENT_DOWNLOADED)

    // when
    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/history/nomsId/$nomsId?startDate=$startDate&endDate=$endDate")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange(),
    )

    // then
    assertThat(response.getJSONArray("recommendations").length()).isEqualTo(2)
    assertThat(response.getInt("recommendationId")).isEqualTo(createdRecommendationId)
    assertThat(response.getString("crn")).isEqualTo(crn)

    // and
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getString("lastModifiedByUserName")).isEqualTo("some_user")
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getString("createdDate")).isNotNull
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getString("lastModifiedDate")).isNotNull
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getJSONObject("recallType").getJSONObject("selected").getString("value")).isEqualTo("FIXED_TERM")
    assertThat(response.getJSONArray("recommendations").getJSONObject(0).getString("status")).isEqualTo("RECALL_CONSIDERED")

    // and
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getString("lastModifiedByUserName")).isEqualTo("some_user")
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getString("createdDate")).isNotNull
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getString("lastModifiedDate")).isNotNull
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getJSONObject("recallType").getJSONObject("selected").getString("value")).isEqualTo("FIXED_TERM")
    assertThat(response.getJSONArray("recommendations").getJSONObject(1).getString("status")).isEqualTo("DOCUMENT_DOWNLOADED")
  }

  @Test
  fun `get recommendation history for a nomsId and no offender found`() {
    // given
    offenderSearchByNomsIdNotFoundResponse()

    // and
    val startDate = LocalDate.now().minusDays(1L).toString()
    val endDate = LocalDate.now().plusDays(1L).toString()

    // when and then
    webTestClient.get()
      .uri("/history/nomsId/$nomsId?startDate=$startDate&endDate=$endDate")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
      .jsonPath("$.userMessage")
      .isEqualTo("No response found: Offender search endpoint returned offender not found")
  }

  @Test
  fun `get recommendation history for a nomsId and no recommendation history found`() {
    // given
    deleteRecommendation()
    val nomsId = 123
    offenderSearchByNomsIdResponse(nomsNumber = nomsId)
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    overviewResponse(crn)
    oasysAssessmentsResponse(crn)

    // and
    val startDate = LocalDate.now().minusDays(1L).toString()
    val endDate = LocalDate.now().minusDays(1L).toString()

    // when and then
    webTestClient.get()
      .uri("/history/nomsId/$nomsId?startDate=$startDate&endDate=$endDate")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
      .jsonPath("$.userMessage")
      .isEqualTo("No recommendation available: No recommendation found for nomsId: $nomsId")
  }

  @Test
  fun `get recommendation history for a crn and no recommendation history found for date range`() {
    // given
    val featureFlagString = "{\"flagConsiderRecall\": true }"
    userAccessAllowed(crn)
    personalDetailsResponse(crn)
    overviewResponse(crn)
    oasysAssessmentsResponse(crn)
    deleteAndCreateRecommendation(featureFlagString) // creates empty draft

    // and
    updateRecommendation(Status.RECALL_CONSIDERED)

    // and
    updateRecommendation(Status.DOCUMENT_DOWNLOADED)

    // and
    val startDate = LocalDate.now().minusDays(3L).toString()
    val endDate = LocalDate.now().minusDays(2L).toString()

    // when and then
    webTestClient.get()
      .uri("/history/crn/$crn?startDate=$startDate&endDate=$endDate")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
      .jsonPath("$.userMessage")
      .isEqualTo("No recommendation available: No recommendation found for crn: $crn")
  }
}
