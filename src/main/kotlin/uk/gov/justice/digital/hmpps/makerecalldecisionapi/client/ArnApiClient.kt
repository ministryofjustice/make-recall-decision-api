package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.concurrent.TimeoutException

class ArnApiClient(
  private val webClient: WebClient,
  @Value("\${oasys.arn.client.timeout}") private val arnClientTimeout: Long,
  private val timeoutCounter: Counter
) {
  fun getRiskSummary(crn: String): Mono<RiskSummaryResponse> {
    val responseType = object : ParameterizedTypeReference<RiskSummaryResponse>() {}
    return webClient
      .get()
      .uri("/risks/crn/$crn/summary")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "risk summary"
        )
      }
  }

  private fun handleTimeoutException(
    exception: Throwable?,
    endPoint: String
  ) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()

        throw ClientTimeoutException(
          "ARN API Client - $endPoint endpoint",
          "No response within $arnClientTimeout seconds"
        )
      }
    }
  }
}
