package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationSupportingDocumentEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper
import java.util.*

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class SupportingDocumentControllerTest(
  @Value("\${document-management.client.timeout}") private val documentManagamentClientTimeout: Long,
) : IntegrationTestBase() {

  @Test
  fun `create supporting document`() {
    documentManagementApiUploadResponse(documentUuid = UUID.randomUUID().toString())

    val recommendationId = "123"

    val data = "While I pondered, weak and weary"

    val response = uploadDocument(recommendationId, data)

    val base64Data = response.getString("data")
    val result = recommendationSupportingDocumentRepository.findById((response.get("id") as Int).toLong())
    assertThat(result.get().documentUuid).isNotNull
    assertThat(Base64.getDecoder().decode(base64Data).decodeToString()).isEqualTo(data)
  }

  @Test
  fun `retrieve supporting documents`() {
    val created = DateTimeHelper.utcNowDateTimeString()
    documentManagementApiDownloadResponse("Once upon a midnight dreary")

    recommendationSupportingDocumentRepository.deleteAll()
    recommendationSupportingDocumentRepository.save(
      RecommendationSupportingDocumentEntity(
        recommendationId = 123,
        mimetype = "word",
        title = "title",
        type = "PPUDPartA",
        filename = "doc.docx",
        created = created,
        createdByUserFullName = "Da Man",
        createdBy = "daman",
        uploaded = created,
        uploadedBy = "daman",
        uploadedByUserFullName = "Da Man",
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

  @Test
  fun `retrieve supporting document`() {
    val created = DateTimeHelper.utcNowDateTimeString()
    documentManagementApiDownloadResponse("Once upon a midnight dreary")

    recommendationSupportingDocumentRepository.deleteAll()
    val result = recommendationSupportingDocumentRepository.save(
      RecommendationSupportingDocumentEntity(
        recommendationId = 123,
        mimetype = "word",
        title = "title",
        type = "PPUDPartA",
        filename = "doc.docx",
        created = created,
        createdByUserFullName = "Da Man",
        createdBy = "daman",
        uploaded = created,
        uploadedBy = "daman",
        uploadedByUserFullName = "Da Man",
      ),
    )

    val response = convertResponseToJSONObject(
      webTestClient.get()
        .uri("/recommendations/123/documents/" + result.id)
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

    assertThat(response.get("recommendationId")).isEqualTo(123)
    assertThat(response.get("filename")).isEqualTo("doc.docx")
    assertThat(response.get("type")).isEqualTo("PPUDPartA")
    assertThat(response.get("data")).isEqualTo(base64("Once upon a midnight dreary"))
    assertThat(response.get("id")).isNotNull()
  }

  @Test
  fun `update supporting documents`() {
    val created = DateTimeHelper.utcNowDateTimeString()

    documentManagementApiUploadResponse(documentUuid = UUID.randomUUID().toString())
    documentManagementApiDeleteResponse()

    recommendationSupportingDocumentRepository.deleteAll()
    val saved = recommendationSupportingDocumentRepository.save(
      RecommendationSupportingDocumentEntity(
        recommendationId = 123,
        mimetype = "word",
        title = "title",
        type = "PPUDPartA",
        filename = "doc.docx",
        created = created,
        createdByUserFullName = "Da Man",
        createdBy = "daman",
        uploaded = created,
        uploadedBy = "daman",
        uploadedByUserFullName = "Da Man",
      ),
    )

    val newData = base64("While I pondered, weak and weary")

    webTestClient.patch()
      .uri("/recommendations/123/documents/" + saved.id)
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "filename": "doc.docx",              
              "mimetype": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
              "title": "new title",
              "data": "$newData"
            }
          """,
        ),
      )
      .headers { (listOf(it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION", "ROLE_MAKE_RECALL_DECISION_PPCS")))) }
      .exchange()
      .expectStatus().isOk

    val result = recommendationSupportingDocumentRepository.findById(saved.id)
    assertThat(result.get().title).isEqualTo("new title")
  }

  @Test
  fun `delete supporting documents`() {
    val created = DateTimeHelper.utcNowDateTimeString()
    documentManagementApiDeleteResponse()
    recommendationSupportingDocumentRepository.deleteAll()
    val saved = recommendationSupportingDocumentRepository.save(
      RecommendationSupportingDocumentEntity(
        recommendationId = 123,
        mimetype = "word",
        title = "title",
        type = "PPUDPartA",
        filename = "doc.docx",
        created = created,
        createdByUserFullName = "Da Man",
        createdBy = "daman",
        uploaded = created,
        uploadedBy = "daman",
        uploadedByUserFullName = "Da Man",
      ),
    )

    webTestClient.delete()
      .uri("/recommendations/123/documents/" + saved.id)
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
      .expectStatus().isOk

    assertThat(recommendationSupportingDocumentRepository.findByRecommendationId(123)).isEmpty()
  }

  @Test
  fun `gateway timeout 503 given on Document Management API timeout for delete endpoint`() {
    runTest {
      val recommendationId = "123"
      val documentUuid = UUID.randomUUID().toString()
      documentManagementApiUploadResponse(documentUuid = documentUuid)
      documentManagementApiDownloadResponse()
      documentManagementApiDeleteResponse(delaySeconds = documentManagamentClientTimeout + 2)
      val id = uploadDocument(recommendationId = recommendationId, data = "hello there!").get("id").toString()
      webTestClient.delete()
        .uri("/recommendations/$recommendationId/documents/$id")
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
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .value<String> { userMessage ->
          assertThat(userMessage).contains("Document Management client - /documents/$documentUuid")
          assertThat(userMessage).contains("endpoint: [No response within $documentManagamentClientTimeout seconds")
        }
    }
  }

  @Test
  fun `gateway timeout 503 given on Document Management API timeout for get endpoint`() {
    runTest {
      val recommendationId = "123"
      val documentUuid = UUID.randomUUID().toString()
      documentManagementApiUploadResponse(documentUuid = documentUuid)
      documentManagementApiDownloadResponse(delaySeconds = documentManagamentClientTimeout + 2)
      val id = uploadDocument(recommendationId = recommendationId, data = "hello there!").get("id").toString()
      webTestClient.get()
        .uri("/recommendations/$recommendationId/documents/$id")
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
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .value<String> { userMessage ->
          assertThat(userMessage).contains("Document Management client - /documents/$documentUuid")
          assertThat(userMessage).contains("endpoint: [No response within $documentManagamentClientTimeout seconds")
        }
    }
  }

  @Test
  fun `gateway timeout 503 given on Document Management API timeout for create endpoint`() {
    runTest {
      val data = "While I pondered, weak and weary"
      val recommendationId = "123"
      documentManagementApiUploadResponse(documentUuid = UUID.randomUUID().toString(), delaySeconds = documentManagamentClientTimeout + 2)
      webTestClient.post()
        .uri("/recommendations/$recommendationId/documents")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "filename": "doc.docx",
              "type" : "PPUDPartA",
              "title": "title",
              "mimetype": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
              "data": "${base64(data)}"
            }
          """,
          ),
        )
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
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(HttpStatus.GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .value<String> { userMessage ->
          assertThat(userMessage).contains("Document Management client - /documents/PPUD_RECALL/")
          assertThat(userMessage).contains("endpoint: [No response within $documentManagamentClientTimeout seconds")
        }
    }
  }

  fun base64(arg: String): String = Base64.getEncoder().encodeToString(arg.encodeToByteArray())

  private fun uploadDocument(recommendationId: String, data: String) = convertResponseToJSONObject(
    webTestClient.post()
      .uri("/recommendations/$recommendationId/documents")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
              {
                "filename": "doc.docx",
                "type" : "PPUDPartA",
                "title": "title",
                "mimetype": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "data": "${base64(data)}"
              }
            """,
        ),
      )
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
}
