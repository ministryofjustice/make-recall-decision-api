package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
class PrisonApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var prisonApiClient: PrisonApiClient

  @Test
  fun `retrieves licence matches`() {
    // given
    val nomsId = "12345"

    prisonApiOffenderMatchResponse(nomsId, "Outside - released from Leeds", "1234")

    // when
    val actual = prisonApiClient.retrieveOffender(nomsId).block()

    // then
    assertThat(actual?.locationDescription, equalTo("Outside - released from Leeds"))
  }

  @Test
  fun `offender not found`() {
    val offenderSearchRequest = HttpRequest.request()
      .withPath("/search/people")

    prisonApi.`when`(offenderSearchRequest).respond(
      HttpResponse.response().withStatusCode(404),
    )

    Assertions.assertThatThrownBy {
      prisonApiClient.retrieveOffender(nomsId).block()
    }.isInstanceOf(NotFoundException::class.java)
      .hasMessage("Offender search endpoint returned offender not found")
  }
}
