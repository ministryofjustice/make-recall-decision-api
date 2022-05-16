package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi.OffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi.OffenderSearchByPhraseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.concurrent.TimeoutException

class OffenderSearchApiClient(
  private val webClient: WebClient,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) {
  fun searchOffenderByPhrase(request: OffenderSearchByPhraseRequest): Mono<OffenderDetailsResponse> {
    val responseType = object : ParameterizedTypeReference<OffenderDetailsResponse>() {}
    return webClient
      .post()
      .uri { builder ->
        builder.path("/phrase").queryParam("paged", "false").build()
      }
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .body(fromValue(request))
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex
        )
      }
  }

  private fun handleTimeoutException(
    exception: Throwable?
  ) {
    when (exception) {
      is TimeoutException -> {
        throw ClientTimeoutException(
          "Offender Search API Client - search by phrase endpoint",
          "No response within $nDeliusTimeout seconds"
        )
      }
    }
  }
}
