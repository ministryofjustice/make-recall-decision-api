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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.secondUpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions.updateRecommendationRequestWithClearedValues
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.recommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.nowDate

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
  fun `update and get recommendation`() {
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    deleteAndCreateRecommendation()
    updateRecommendation(updateRecommendationRequest())
    updateRecommendation(secondUpdateRecommendationRequest())

    webTestClient.get()
      .uri("/recommendations/$createdRecommendationId")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(createdRecommendationId)
      .jsonPath("$.crn").isEqualTo(crn)
      .jsonPath("$.status").isEqualTo("DRAFT")
      .jsonPath("$.recallType.selected.value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.allOptions[1].value").isEqualTo("STANDARD")
      .jsonPath("$.recallType.allOptions[1].text").isEqualTo("Standard")
      .jsonPath("$.recallType.allOptions[0].value").isEqualTo("FIXED_TERM")
      .jsonPath("$.recallType.allOptions[0].text").isEqualTo("Fixed term")
      .jsonPath("$.recallType.allOptions[2].value").isEqualTo("NO_RECALL")
      .jsonPath("$.recallType.allOptions[2].text").isEqualTo("No recall")
      .jsonPath("$.custodyStatus.selected").isEqualTo("YES_PRISON")
      .jsonPath("$.custodyStatus.allOptions[0].value").isEqualTo("YES_PRISON")
      .jsonPath("$.custodyStatus.allOptions[0].text").isEqualTo("Yes, prison custody")
      .jsonPath("$.custodyStatus.allOptions[1].value").isEqualTo("YES_POLICE")
      .jsonPath("$.custodyStatus.allOptions[1].text").isEqualTo("Yes, police custody")
      .jsonPath("$.custodyStatus.allOptions[2].value").isEqualTo("NO")
      .jsonPath("$.custodyStatus.allOptions[2].text").isEqualTo("No")
      .jsonPath("$.responseToProbation").isEqualTo("They have not responded well")
      .jsonPath("$.isThisAnEmergencyRecall").isEqualTo(true)
      .jsonPath("$.hasVictimsInContactScheme.selected").isEqualTo("YES")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].value").isEqualTo("YES")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].text").isEqualTo("Yes")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].value").isEqualTo("NO")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].text").isEqualTo("No")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].value").isEqualTo("NOT_APPLICABLE")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].text").isEqualTo("N/A")
      .jsonPath("$.dateVloInformed").isEqualTo("2022-08-01")
      .jsonPath("$.personOnProbation.name").isEqualTo("John Smith")
      .jsonPath("$.alternativesToRecallTried.selected[0].value").isEqualTo("WARNINGS_LETTER")
      .jsonPath("$.alternativesToRecallTried.selected[0].details").isEqualTo("We sent a warning letter on 27th July 2022")
      .jsonPath("$.alternativesToRecallTried.selected[1].value").isEqualTo("DRUG_TESTING")
      .jsonPath("$.alternativesToRecallTried.selected[1].details").isEqualTo("Drug test passed")
      .jsonPath("$.alternativesToRecallTried.allOptions[0].value").isEqualTo("WARNINGS_LETTER")
      .jsonPath("$.alternativesToRecallTried.allOptions[0].text").isEqualTo("Warnings/licence breach letters")
      .jsonPath("$.alternativesToRecallTried.allOptions[1].value").isEqualTo("DRUG_TESTING")
      .jsonPath("$.alternativesToRecallTried.allOptions[1].text").isEqualTo("Drug testing")
      .jsonPath("$.hasArrestIssues.selected").isEqualTo(true)
      .jsonPath("$.hasArrestIssues.details").isEqualTo("Violent behaviour")

    val result = repository.findByCrnAndStatus(crn, Status.DRAFT.name)
    assertThat(result[0].data.lastModifiedBy, equalTo("SOME_USER"))
  }

  @Test
  fun `given an update that clears hidden fields then save in database and get recommendation with null values for hidden fields`() {
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    deleteAndCreateRecommendation()
    updateRecommendation(updateRecommendationRequest())
    updateRecommendation(updateRecommendationRequestWithClearedValues())

    webTestClient.get()
      .uri("/recommendations/$createdRecommendationId")
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.id").isEqualTo(createdRecommendationId)
      .jsonPath("$.crn").isEqualTo(crn)
      .jsonPath("$.status").isEqualTo("DRAFT")
      .jsonPath("$.hasVictimsInContactScheme.selected").isEqualTo("NO")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].value").isEqualTo("YES")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[0].text").isEqualTo("Yes")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].value").isEqualTo("NO")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[1].text").isEqualTo("No")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].value").isEqualTo("NOT_APPLICABLE")
      .jsonPath("$.hasVictimsInContactScheme.allOptions[2].text").isEqualTo("N/A")
      .jsonPath("$.dateVloInformed").isEqualTo(null)
      .jsonPath("$.hasArrestIssues.selected").isEqualTo(false)
      .jsonPath("$.hasArrestIssues.details").isEqualTo(null)

    val result = repository.findByCrnAndStatus(crn, Status.DRAFT.name)
    assertThat(result[0].data.lastModifiedBy, equalTo("SOME_USER"))
  }

  private fun updateRecommendation(recommendationRequest: String) {
    webTestClient.patch()
      .uri("/recommendations/$createdRecommendationId")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(recommendationRequest)
      )
      .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `generate a Part A from recommendation data`() {
    userAccessAllowed(crn)
    allOffenderDetailsResponse(crn)
    deleteAndCreateRecommendation()
    updateRecommendation(updateRecommendationRequest())

    val response = convertResponseToJSONObject(
      webTestClient.post()
        .uri("/recommendations/$createdRecommendationId/part-a")
        .contentType(MediaType.APPLICATION_JSON)
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus().isOk
    )

    assertThat(response.get("fileName")).isEqualTo("NAT_Recall_Part_A_" + nowDate() + "_Smith_J_A12345.docx")
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
