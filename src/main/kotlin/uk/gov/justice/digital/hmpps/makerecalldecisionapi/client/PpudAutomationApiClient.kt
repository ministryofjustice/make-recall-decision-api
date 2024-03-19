package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.WebClientConfiguration.Companion.withRetry
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateSentenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudReferenceListResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PpudValidationException
import java.time.Duration
import java.util.concurrent.TimeoutException

class PpudAutomationApiClient(
  private val webClient: WebClient,
  @Value("\${ppud-automation.client.timeout}") private val ppudAutomationTimeout: Long,
  private val timeoutCounter: Counter,
  private val objectMapper: ObjectMapper,
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
        handleTimeoutException(ex)
      }
      .withRetry()
  }

  fun details(
    id: String,
  ): Mono<PpudDetailsResponse> {
    val responseType = object : ParameterizedTypeReference<PpudDetailsResponse>() {}
    return webClient
      .get()
      .uri { builder -> builder.path("/offender/" + id).build() }
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        handleTimeoutException(ex)
      }
      .withRetry()
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
        handleTimeoutException(ex)
      }
      .withRetry()
  }

  fun createOffender(request: PpudCreateOffenderRequest): Mono<PpudCreateOffenderResponse> {
    val responseType = object : ParameterizedTypeReference<PpudCreateOffenderResponse>() {}

    return webClient
      .post()
      .uri { builder -> builder.path("/offender").build() }
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(request))
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        if (ex is BadRequest) {
          throw PpudValidationException(objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java))
        }
        handleTimeoutException(ex)
      }
      .withRetry()
  }

  fun updateOffender(offenderId: String, request: PpudUpdateOffenderRequest): Mono<ResponseEntity<Void>> {
    return webClient
      .put()
      .uri { builder -> builder.path("/offender/" + offenderId).build() }
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(request))
      .retrieve()
      .toBodilessEntity()
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        if (ex is BadRequest) {
          throw PpudValidationException(objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java))
        }
        handleTimeoutException(ex)
      }
      .withRetry()
  }

  fun createSentence(
    offenderId: String,
    request: PpudCreateOrUpdateSentenceRequest,
  ): Mono<PpudCreateSentenceResponse> {
    val responseType = object : ParameterizedTypeReference<PpudCreateSentenceResponse>() {}
    return webClient
      .post()
      .uri { builder -> builder.path("/offender/" + offenderId + "/sentence").build() }
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(request))
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        if (ex is BadRequest) {
          throw PpudValidationException(objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java))
        }
        handleTimeoutException(ex)
      }
      .withRetry()
  }

  fun updateSentence(
    offenderId: String,
    sentenceId: String,
    request: PpudCreateOrUpdateSentenceRequest,
  ): Mono<ResponseEntity<Void>> {
    return webClient
      .put()
      .uri { builder -> builder.path("/offender/" + offenderId + "/sentence/" + sentenceId).build() }
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(request))
      .retrieve()
      .toBodilessEntity()
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        if (ex is BadRequest) {
          throw PpudValidationException(objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java))
        }
        handleTimeoutException(ex)
      }
      .withRetry()
  }

  fun updateOffence(
    offenderId: String,
    sentenceId: String,
    request: PpudUpdateOffenceRequest,
  ): Mono<ResponseEntity<Void>> {
    return webClient
      .put()
      .uri { builder -> builder.path("/offender/" + offenderId + "/sentence/" + sentenceId + "/offence").build() }
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(request))
      .retrieve()
      .toBodilessEntity()
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        if (ex is BadRequest) {
          throw PpudValidationException(objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java))
        }
        handleTimeoutException(ex)
      }
      .withRetry()
  }

  fun createOrUpdateRelease(
    offenderId: String,
    sentenceId: String,
    request: PpudCreateOrUpdateReleaseRequest,
  ): Mono<PpudCreateOrUpdateReleaseResponse> {
    val responseType = object : ParameterizedTypeReference<PpudCreateOrUpdateReleaseResponse>() {}
    return webClient
      .post()
      .uri { builder -> builder.path("/offender/" + offenderId + "/sentence/" + sentenceId + "/release").build() }
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(request))
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        if (ex is BadRequest) {
          throw PpudValidationException(objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java))
        }
        handleTimeoutException(ex)
      }
  }

  fun createRecall(
    offenderId: String,
    releaseId: String,
    request: PpudCreateRecallRequest,
  ): Mono<PpudCreateRecallResponse> {
    val responseType = object : ParameterizedTypeReference<PpudCreateRecallResponse>() {}
    return webClient
      .post()
      .uri { builder -> builder.path("/offender/" + offenderId + "/release/" + releaseId + "/recall").build() }
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(BodyInserters.fromValue(request))
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        if (ex is BadRequest) {
          throw PpudValidationException(objectMapper.readValue(ex.responseBodyAsString, ErrorResponse::class.java))
        }
        handleTimeoutException(ex)
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
        handleTimeoutException(ex)
      }
      .withRetry()
  }

  fun handleTimeoutException(
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
