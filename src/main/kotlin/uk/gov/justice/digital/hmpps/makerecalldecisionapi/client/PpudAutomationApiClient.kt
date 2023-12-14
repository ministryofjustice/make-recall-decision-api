package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudReferenceListResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.concurrent.TimeoutException

class PpudAutomationApiClient(
  private val webClient: WebClient,
  @Value("\${ppud-automation.client.timeout}") private val ppudAutomationTimeout: Long,
  private val timeoutCounter: Counter,
) {

  fun search(
    request: PpudSearchRequest,
  ): Mono<PpudSearchResponse> {
    val responseType = object : ParameterizedTypeReference<PpudSearchResponse>() {}
    return webClient
      .post()
      .uri { builder -> builder.path("/offender/search").build() }
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(request))
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
        )
      }
  }

  fun bookToPpud(
    nomisId: String,
    request: PpudBookRecall,
  ): Mono<PpudBookRecallResponse> {
    val responseType = object : ParameterizedTypeReference<PpudBookRecallResponse>() {}

    return webClient
      .post()
      .uri { builder -> builder.path("/offender/" + nomisId + "/recall").build() }
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(request))
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
        )
      }
  }

  fun retrieveList(name: String): Mono<PpudReferenceListResponse> {
    val responseType = object : ParameterizedTypeReference<PpudReferenceListResponse>() {}

    return webClient
      .get()
      .uri { builder -> builder.path("/reference/" + name).build() }
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
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
          "PPUD Automation API Client",
          "No response within $ppudAutomationTimeout seconds",
        )
      }
    }
  }
}
