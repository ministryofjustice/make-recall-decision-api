package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
class PrisonApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var prisonApiClient: PrisonApiClient

  @Test
  fun `retrieves licence matches`() {
    // given
    val nomsId = "12345"

    prisonApiMatchResponse(nomsId, "Outside - released from Leeds")

    // when
    val actual = prisonApiClient.retrieveOffender(nomsId).block()

    // then
    assertThat(actual?.locationDescription, equalTo("Outside - released from Leeds"))
  }
}
