package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationSupportingDocumentEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class SupportingDocumentControllerTest() : IntegrationTestBase() {

  @Test
  fun `create supporting document`() {
    val recommendationId = "123"

    webTestClient.post()
      .uri("/recommendations/$recommendationId/documents")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "filename": "doc.docx",
              "type" : "PPUDPartA",
              "mimetype": "word",
              "data": "VGhlIGhpbGxzIGFyZSBhbGl2ZSB3aXRoIHRoZXQgc291bmQgb2YgbXVzaWM="
            }
          """,
        ),
      )
      .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION", "ROLE_MAKE_RECALL_DECISION_PPCS")))) }
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `retrieve supporting documents`() {
    val created = DateTimeHelper.utcNowDateTimeString()

    recommendationSupportingDocumentRepository.deleteAll()
    recommendationSupportingDocumentRepository.save(
      RecommendationSupportingDocumentEntity(
        recommendationId = 123,
        mimetype = "word",
        type = "PPUDPartA",
        filename = "doc.docx",
        created = created,
        createdByUserFullName = "Da Man",
        createdBy = "daman",
        uploaded = created,
        uploadedBy = "daman",
        uploadedByUserFullName = "Da Man",
        data = "Once upon a midnight dreary".encodeToByteArray(),
      ),
    )

    val response = convertResponseToJSONArray(
      webTestClient.get()
        .uri("/recommendations/123/documents")
        .headers {
          (
            listOf(
              it.authToken(
                roles = listOf(
                  "ROLE_MAKE_RECALL_DECISION",
                  "ROLE_MAKE_RECALL_DECISION_PPCS",
                ),
              ),
            )
            )
        }
        .exchange()
        .expectStatus().isOk,
    )

    assertThat(response.length()).isEqualTo(1)

    val doc = JSONObject(response.get(0).toString())

    assertThat(doc.get("recommendationId")).isEqualTo(123)
    assertThat(doc.get("createdBy")).isEqualTo("daman")
    assertThat(doc.get("createdByUserFullName")).isEqualTo("Da Man")
    assertThat(doc.get("created")).isEqualTo(created)
    assertThat(doc.get("uploadedBy")).isEqualTo("daman")
    assertThat(doc.get("uploadedByUserFullName")).isEqualTo("Da Man")
    assertThat(doc.get("uploaded")).isEqualTo(created)
    assertThat(doc.get("filename")).isEqualTo("doc.docx")
    assertThat(doc.get("type")).isEqualTo("PPUDPartA")
  }
}
