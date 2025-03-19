package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.PrisonApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison.agencyResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison.prisonSentencesAndOffences
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison.prisonTimelineResponse

@ActiveProfiles("test")
class PrisonApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var prisonApiClient: PrisonApiClient

  @Test
  fun `retrieves licence matches`() {
    // given
    val nomsId = "12345"

    prisonApiOffenderMatchResponse(nomsId, "Outside - released from Leeds", "1234", "KLN")

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
      .hasMessage("Prison api returned offender not found for nomis id A1234CR")
  }

  @Test
  fun `retrieves prison timelines`() {
    // given
    val request = HttpRequest.request().withPath("/api/offenders/" + nomsId + "/prison-timeline")

    prisonApi.`when`(request).respond(
      HttpResponse.response().withContentType(MediaType.APPLICATION_JSON)
        .withBody(prisonTimelineResponse("ABC")),
    )

    // when
    val actual = prisonApiClient.retrievePrisonTimelines(nomsId).block()

    // then
    assertThat(actual?.prisonerNumber, equalTo("ABC"))
  }

  @Test
  fun `timeline not found`() {
    val request = HttpRequest.request().withPath("/api/offenders/" + nomsId + "/prison-timeline")

    prisonApi.`when`(request).respond(
      HttpResponse.response().withStatusCode(404),
    )

    Assertions.assertThatThrownBy {
      prisonApiClient.retrievePrisonTimelines(nomsId).block()
    }.isInstanceOf(NotFoundException::class.java)
      .hasMessage("Prison api returned prison timeline not found for nomis id A1234CR")
  }

  @Test
  fun `retrieves prison sentences and offences`() {
    // given
    val request = HttpRequest.request().withPath("/api/offender-sentences/booking/" + 12 + "/sentences-and-offences")

    prisonApi.`when`(request).respond(
      HttpResponse.response().withContentType(MediaType.APPLICATION_JSON)
        .withBody(prisonSentencesAndOffences(12)),
    )

    // when
    val actual = prisonApiClient.retrieveSentencesAndOffences(12).block()

    // then
    assertThat(actual?.get(0)?.bookingId, equalTo(12))
  }

  @Test
  fun `sentence and offences not found`() {
    val request = HttpRequest.request().withPath("/api/offender-sentences/booking/12/sentences-and-offences")

    prisonApi.`when`(request).respond(
      HttpResponse.response().withStatusCode(404),
    )

    Assertions.assertThatThrownBy {
      prisonApiClient.retrieveSentencesAndOffences(12).block()
    }.isInstanceOf(NotFoundException::class.java)
      .hasMessage("Prison api returned sentences and offences not found for booking id 12")
  }

  @Test
  fun `retrieve agency`() {
    val request = HttpRequest.request()
      .withPath("/api/agencies/MDI")
      .withQueryStringParameter("activeOnly", "false")

    prisonApi.`when`(request).respond(
      HttpResponse.response().withContentType(MediaType.APPLICATION_JSON)
        .withBody(agencyResponse("MDI")),
    )

    val agency = prisonApiClient.retrieveAgency("MDI").block()

    assertThat(agency?.agencyId, equalTo("MDI"))
  }

  @Test
  fun `agency not found`() {
    val request = HttpRequest.request().withPath("/api/agencies/MDI")

    prisonApi.`when`(request).respond(
      HttpResponse.response().withStatusCode(404),
    )

    Assertions.assertThatThrownBy {
      prisonApiClient.retrieveAgency("MDI").block()
    }.isInstanceOf(NotFoundException::class.java)
      .hasMessage("Prison api returned agency not found for agency id MDI")
  }
}
