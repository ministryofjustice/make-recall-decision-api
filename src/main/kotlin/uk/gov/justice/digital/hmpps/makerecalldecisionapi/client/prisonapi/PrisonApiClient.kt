package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.prisonapi

import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.WebClientConfiguration.Companion.withRetry
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Agency
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.PrisonTimelineResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.PrisonApiOffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.prisonapi.SentenceCalculationDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutRuntimeException
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
  ): Mono<Offender> {
    val responseType = object : ParameterizedTypeReference<Offender>() {}
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
      .withRetry()
  }

  fun retrieveImageData(
    facialImageId: String,
  ): Mono<ResponseEntity<ByteArrayResource>> = webClient
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
    .withRetry()

  fun retrievePrisonTimelines(
    nomsId: String,
  ): Mono<PrisonTimelineResponse> {
    val responseType = object : ParameterizedTypeReference<PrisonTimelineResponse>() {}
    return webClient
      .get()
      .uri { builder -> builder.path("/api/offenders/" + nomsId + "/prison-timeline").build() }
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw NotFoundException("Prison api returned prison timeline not found for nomis id " + nomsId) },
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(prisonTimeout))
      .doOnError { ex ->
        handleTimeoutException(exception = ex)
      }
      .withRetry()
  }

  fun retrieveSentencesAndOffences(bookingId: Int): Mono<List<Sentence>> {
    val responseType = object : ParameterizedTypeReference<List<Sentence>>() {}
    return webClient
      .get()
      .uri { builder ->
        builder.path("/api/offender-sentences/booking/" + bookingId + "/sentences-and-offences").build()
      }
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw NotFoundException("Prison api returned sentences and offences not found for booking id " + bookingId) },
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(prisonTimeout))
      .doOnError { ex ->
        handleTimeoutException(exception = ex)
      }
      .withRetry()
  }

  fun bookingSentenceDetails(bookingId: Int): Mono<SentenceCalculationDates> {
    val responseType = object : ParameterizedTypeReference<SentenceCalculationDates>() {}
    return webClient
      .get()
      .uri { builder -> builder.path("/api/bookings/$bookingId/sentenceDetail").build() }
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw NotFoundException("Prison API found no sentence details for booking ID $bookingId") },
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(prisonTimeout))
      .doOnError { ex ->
        handleTimeoutRuntimeException(exception = ex)
      }
      .withRetry()
  }

  fun retrieveAgency(agencyId: String): Mono<Agency> {
    val responseType = object : ParameterizedTypeReference<Agency>() {}
    return webClient
      .get()
      .uri { builder ->
        builder.path("/api/agencies/$agencyId").queryParam("activeOnly", false).build()
      }
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw NotFoundException("Prison api returned agency not found for agency id $agencyId") },
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(prisonTimeout))
      .doOnError { ex ->
        handleTimeoutException(exception = ex)
      }
      .withRetry()
  }

  fun retrieveOffenderMovements(nomsId: String): Mono<List<PrisonApiOffenderMovement>> {
    val responseType = object : ParameterizedTypeReference<List<PrisonApiOffenderMovement>>() {}
    return webClient
      .get()
      .uri { builder -> builder.path("api/movements/offender/$nomsId").build() }
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw NotFoundException("Prison API found no movements for NOMIS ID $nomsId") },
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(prisonTimeout))
      .doOnError { ex ->
        handleTimeoutRuntimeException(exception = ex)
      }
      .withRetry()
  }

  private fun handleTimeoutException(exception: Throwable?) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()
        throw ClientTimeoutException("Prison API Client", "No response within $prisonTimeout seconds")
      }
    }
  }

  private fun handleTimeoutRuntimeException(exception: Throwable?) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()
        throw ClientTimeoutRuntimeException("Prison API Client", "No response within $prisonTimeout seconds")
      }
    }
  }
}
