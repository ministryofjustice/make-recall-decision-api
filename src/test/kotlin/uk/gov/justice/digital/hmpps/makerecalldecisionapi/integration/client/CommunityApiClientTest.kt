package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
class CommunityApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var communityApiClient: CommunityApiClient

  @Test
  fun `retrieves requested document`() {
    // given
    val documentId = "54345"
    getDocumentResponse(crn, documentId)

    // when
    val actual = communityApiClient.getDocumentByCrnAndId(crn, documentId).block()

    // then
    assertThat(actual?.statusCode?.is2xxSuccessful, equalTo(true))
  }
}
