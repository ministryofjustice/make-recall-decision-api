package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.recommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class RecommendationControllerTest() : IntegrationTestBase() {

  @Test
  fun `create recommendation`() {
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(recommendationRequest(crn))
        )
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isCreated
    )

    val idOfRecommendationJustCreated = response.get("id")

    assertThat(response.get("id")).isEqualTo(idOfRecommendationJustCreated)
    assertThat(response.get("status")).isEqualTo("DRAFT")
    assertThat(JSONObject(response.get("personOnProbation").toString()).get("name")).isEqualTo("John Smith")
  }

  @Test
  fun `get recommendation`() {
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    deleteAndCreateRecommendation()
    updateRecommendation()

    webTestClient.get()
      .uri("/recommendations/$createdRecommendationId")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(createdRecommendationId)
      .jsonPath("$.crn").isEqualTo(crn)
      .jsonPath("$.status").isEqualTo("DRAFT")
      .jsonPath("$.recallType.value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.options[1].value").isEqualTo("STANDARD")
      .jsonPath("$.recallType.options[1].text").isEqualTo("Standard")
      .jsonPath("$.recallType.options[0].value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.options[0].text").isEqualTo("Fixed term")
      .jsonPath("$.recallType.options[2].value").isEqualTo("NO_RECALL")
      .jsonPath("$.recallType.options[2].text").isEqualTo("No recall")
      .jsonPath("$.custodyStatus.value").isEqualTo("YES_PRISON")
      .jsonPath("$.custodyStatus.options[0].value").isEqualTo("YES_PRISON")
      .jsonPath("$.custodyStatus.options[0].text").isEqualTo("Yes, prison custody")
      .jsonPath("$.custodyStatus.options[1].value").isEqualTo("YES_POLICE")
      .jsonPath("$.custodyStatus.options[1].text").isEqualTo("Yes, police custody")
      .jsonPath("$.custodyStatus.options[2].value").isEqualTo("NO")
      .jsonPath("$.custodyStatus.options[2].text").isEqualTo("No")
      .jsonPath("$.personOnProbation.name").isEqualTo("John Smith")
  }

  private fun updateRecommendation() {
    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(updateRecommendationRequest())
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `update a recommendation`() {
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    deleteAndCreateRecommendation()

    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(updateRecommendationRequest())
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(createdRecommendationId)
      .jsonPath("$.crn").isEqualTo(crn)
      .jsonPath("$.recallType.value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.options[0].value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.options[0].text").isEqualTo("Fixed term")
      .jsonPath("$.recallType.options[1].value").isEqualTo("STANDARD")
      .jsonPath("$.recallType.options[1].text").isEqualTo("Standard")
      .jsonPath("$.recallType.options[2].value").isEqualTo("NO_RECALL")
      .jsonPath("$.recallType.options[2].text").isEqualTo("No recall")
      .jsonPath("$.custodyStatus.value").isEqualTo("YES_PRISON")
      .jsonPath("$.custodyStatus.options[0].value").isEqualTo("YES_PRISON")
      .jsonPath("$.custodyStatus.options[0].text").isEqualTo("Yes, prison custody")
      .jsonPath("$.custodyStatus.options[1].value").isEqualTo("YES_POLICE")
      .jsonPath("$.custodyStatus.options[1].text").isEqualTo("Yes, police custody")
      .jsonPath("$.custodyStatus.options[2].value").isEqualTo("NO")
      .jsonPath("$.custodyStatus.options[2].text").isEqualTo("No")
      .jsonPath("$.status").isEqualTo("DRAFT")
      .jsonPath("$.personOnProbation.name").isEqualTo("John Smith")

    val result = repository.findByCrnAndStatus(crn, Status.DRAFT.name)

    assertThat(result[0].data.lastModifiedBy, equalTo("SOME_USER"))
  }

  @Test
  fun `generate a Part A from recommendation data`() {
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    deleteAndCreateRecommendation()

    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations/$createdRecommendationId/part-a")
        .contentType(MediaType.APPLICATION_JSON)
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    assertThat(response.get("fileName")).isEqualTo("NAT_Recall_Part_A_$crn.docx")
    assertNotNull(response.get("fileContents"))
  }

  @Test
  fun `handles scenario where no recommendation exists for given id on update`() {
    webTestClient.patch()
      .uri("/recommendations/999")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(updateRecommendationRequest())
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
      .jsonPath("$.userMessage")
      .isEqualTo("No recommendation available: No recommendation found for id: 999")
  }

  @Test
  fun `handles scenario where no recommendation exists for given id`() {
    runTest {
      webTestClient.get()
        .uri("/recommendations/999")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
        .jsonPath("$.userMessage")
        .isEqualTo("No recommendation available: No recommendation found for id: 999")
    }
  }

  @Test
  fun `access denied when insufficient privileges used for creation request`() {
    val crn = "X123456"
    webTestClient.post()
      .uri("/recommendations")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(recommendationRequest(crn))
      )
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  private fun convertResponseToJSONObject(response: WebTestClient.ResponseSpec): JSONObject {
    val responseBodySpec = response.expectBody<String>()
    val responseEntityExchangeResult = responseBodySpec.returnResult()
    val responseString = responseEntityExchangeResult.responseBody
    return JSONObject(responseString)
  }
}
