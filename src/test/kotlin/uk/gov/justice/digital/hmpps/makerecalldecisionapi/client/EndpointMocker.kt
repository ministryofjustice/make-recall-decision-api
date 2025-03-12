package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi.PrisonApiClientTest.Companion.wiremock

class EndpointMocker {

  companion object {
    internal fun mockGetEndpointWithSuccess(endpoint: String, jsonResponse: String, delayInSeconds: Int = 0) {
      wiremock.stubFor(
        get(endpoint)
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.OK.value())
              .withHeader("Content-Type", "application/json")
              .withBody(jsonResponse)
              .withFixedDelay(delayInSeconds * 1000),
          ),
      )
    }

    internal fun mockGetEndpointWithFailure(endpoint: String, failureStatus: HttpStatus) {
      wiremock.stubFor(
        get(endpoint)
          .willReturn(
            aResponse().withStatus(failureStatus.value()),
          ),
      )
    }
  }
}
