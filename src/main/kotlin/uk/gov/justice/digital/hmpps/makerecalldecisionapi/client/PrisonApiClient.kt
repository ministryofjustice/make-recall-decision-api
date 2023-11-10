package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.concurrent.TimeoutException

class PrisonApiClient(
  private val webClient: WebClient,
  @Value("\${prison.client.timeout}") private val prisonTimeout: Long,
  private val timeoutCounter: Counter,
) {

  fun retrieveOffender(
    nomsId: String,
  ): Mono<PrisonOffenderSearchResponse> {
    val responseType = object : ParameterizedTypeReference<PrisonOffenderSearchResponse>() {}
    return webClient
      .get()
      .uri { builder -> builder.path("/api/offenders/" + nomsId).build() }
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(prisonTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
        )
      }
  }

  private fun handleTimeoutException(
    exception: Throwable?,
  ) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()

        throw ClientTimeoutException(
          "Prison API Client",
          "No response within $prisonTimeout seconds",
        )
      }
    }
  }
}
