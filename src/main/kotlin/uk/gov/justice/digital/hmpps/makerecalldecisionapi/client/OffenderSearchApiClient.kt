package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchByPhraseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.concurrent.TimeoutException

class OffenderSearchApiClient(
  private val webClient: WebClient,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  private val timeoutCounter: Counter
) {
  fun searchOffenderByPhrase(request: OffenderSearchByPhraseRequest): Mono<List<OffenderDetails>> {
    val responseType = object : ParameterizedTypeReference<List<OffenderDetails>>() {}
    return webClient
      .post()
      .uri { builder ->
        builder.path("/search").queryParam("paged", "false").build()
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
        timeoutCounter.increment()

        throw ClientTimeoutException(
          "Offender Search API Client - search by phrase endpoint",
          "No response within $nDeliusTimeout seconds"
        )
      }
    }
  }
}
