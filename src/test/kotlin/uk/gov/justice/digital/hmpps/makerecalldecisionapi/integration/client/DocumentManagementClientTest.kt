package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DocumentManagementClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.util.UUID

@ActiveProfiles("test")
class DocumentManagementClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var documentManagementClient: DocumentManagementClient

  @Test
  fun `uploads a document`() {
    // given
    val documentUid = "deae6fd3-2e16-4565-821a-7b99f949ff76"

    // and
    documentManagementApiUploadResponse(documentUid)

    // and
    val expected = documentUid

    // when
    val actual = documentManagementClient.uploadFile(
      crn = "123",
      file = "myFile".toByteArray(),
      filename = "",
      documentUuid = UUID.randomUUID().toString(),
      mimeType = null,
    ).block()

    // then
    assertThat(actual!!.toString()).isEqualTo(expected)
  }

  @Test
  fun `downloads a document`() {
    // given
    documentManagementApiDownloadResponse()

    // and
    val expected = "hello there!"

    // when
    val actual = documentManagementClient.downloadFileAsByteArray(documentUuid = "123").block()

    // then
    assertThat(String(actual!!)).isEqualTo(expected)
  }

  @Test
  fun `deletes a document`() {
    // given
    val documentUuid = "deae6fd3-2e16-4565-821a-7b99f949ff76"

    // and
    documentManagementApiDeleteResponse()

    // when
    val result = documentManagementClient.deleteFile(documentUuid)

    // then
    assertThat(result.block()).isNull()
  }
}
