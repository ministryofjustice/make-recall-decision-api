package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
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
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw NotFoundException("Prison api returned offender not found for nomis id " + nomsId) },
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(prisonTimeout))
      .doOnError { ex ->
        handleTimeoutException(exception = ex)
      }
  }

  fun retrieveImageData(
    facialImageId: String,
  ): Mono<ResponseEntity<ByteArrayResource>> {
    return webClient
      .get()
      .uri { builder -> builder.path("/api/images/" + facialImageId + "/data").build() }
      .retrieve()
      .toEntity(ByteArrayResource::class.java)
      .timeout(Duration.ofSeconds(prisonTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
        )
      }
  }

  private fun handleTimeoutException(exception: Throwable?) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()
        throw ClientTimeoutException("Prison API Client", "No response within $prisonTimeout seconds")
      }
    }
  }
}
