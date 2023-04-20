package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpStatus.GATEWAY_TIMEOUT
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
class DocumentControllerTest(
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) : IntegrationTestBase() {

  val documentId = "543456"

  @Test
  fun `retrieves document from CRN and document ID`() {
    runTest {
      userAccessAllowed(crn)
      getDocumentResponse(crn, documentId)

      webTestClient.get()
        .uri("/cases/$crn/documents/$documentId")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isOk
        .expectHeader()
        .contentDisposition(ContentDisposition.parse("attachment; filename=\"myPdfTest.pdf\""))
        .expectHeader()
        .contentType("application/pdf;charset=UTF-8")
    }
  }

  @Test
  fun `handles scenario where no document exists for crn`() {
    runTest {
      userAccessAllowed(crn)
      noDocumentAvailable(crn, documentId)

      webTestClient.get()
        .uri("/cases/$crn/documents/$documentId")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .isNotFound
        .expectBody()
        .jsonPath("$.userMessage").isEqualTo("No personal details available: No details available for endpoint: /document/A12345/543456")
    }
  }

  @Test
  fun `gateway timeout 503 given on Delius timeout`() {
    runTest {
      userAccessAllowed(crn)
      getDocumentResponse(crn, documentId, delaySeconds = nDeliusTimeout + 2)

      webTestClient.get()
        .uri("/cases/$crn/documents/$documentId")
        .headers { it.authToken(roles = listOf("ROLE_MAKE_RECALL_DECISION")) }
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .jsonPath("$.status").isEqualTo(GATEWAY_TIMEOUT.value())
        .jsonPath("$.userMessage")
        .isEqualTo("Client timeout: Delius integration client - /document/A12345/543456 endpoint: [No response within $nDeliusTimeout seconds]")
    }
  }
}
