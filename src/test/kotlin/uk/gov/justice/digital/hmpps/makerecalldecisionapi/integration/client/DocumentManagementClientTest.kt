package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DocumentManagementClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
class DocumentManagementClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var documentManagementClient: DocumentManagementClient

  @Test
  fun `uploads a document`() {
    // given
    val documentUid = "deae6fd3-2e16-4565-821a-7b99f949ff76"

    // and
    documentManagementApiResponse(documentUid)

    // and
    val expected = documentUid

    // when
    val actual = documentManagementClient.uploadFile(crn = "123", "myFile".toByteArray(), "").block()

    // then
    assertThat(actual!!.toString()).isEqualTo(expected)
  }
}
