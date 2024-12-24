package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ppud

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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateMinuteRequest
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserSearchRequest
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

  fun search(request: PpudSearchRequest): Mono<PpudSearchResponse> {
    return post("/offender/search", request, object : ParameterizedTypeReference<PpudSearchResponse>() {})
  }

  fun details(id: String): Mono<PpudDetailsResponse> {
    return get("/offender/$id", object : ParameterizedTypeReference<PpudDetailsResponse>() {})
  }

  fun bookToPpud(nomisId: String, request: PpudBookRecall): Mono<PpudBookRecallResponse> {
    return post("/offender/$nomisId/recall", request, object : ParameterizedTypeReference<PpudBookRecallResponse>() {})
  }

  fun createOffender(request: PpudCreateOffenderRequest): Mono<PpudCreateOffenderResponse> {
    return post("/offender", request, object : ParameterizedTypeReference<PpudCreateOffenderResponse>() {})
  }

  fun updateOffender(offenderId: String, request: PpudUpdateOffenderRequest): Mono<ResponseEntity<Void>> {
    return put("/offender/$offenderId", request)
  }

  fun createSentence(offenderId: String, request: PpudCreateOrUpdateSentenceRequest): Mono<PpudCreateSentenceResponse> {
    return post(
      "/offender/$offenderId/sentence",
      request,
      object : ParameterizedTypeReference<PpudCreateSentenceResponse>() {},
    )
  }

  fun updateSentence(
    offenderId: String,
    sentenceId: String,
    request: PpudCreateOrUpdateSentenceRequest,
  ): Mono<ResponseEntity<Void>> {
    return put("/offender/$offenderId/sentence/$sentenceId", request)
  }

  fun updateOffence(
    offenderId: String,
    sentenceId: String,
    request: PpudUpdateOffenceRequest,
  ): Mono<ResponseEntity<Void>> {
    return put("/offender/$offenderId/sentence/$sentenceId/offence", request)
  }

  fun createOrUpdateRelease(
    offenderId: String,
    sentenceId: String,
    request: PpudCreateOrUpdateReleaseRequest,
  ): Mono<PpudCreateOrUpdateReleaseResponse> {
    return post(
      "/offender/$offenderId/sentence/$sentenceId/release",
      request,
      object : ParameterizedTypeReference<PpudCreateOrUpdateReleaseResponse>() {},
    )
  }

  fun createRecall(
    offenderId: String,
    releaseId: String,
    request: PpudCreateRecallRequest,
  ): Mono<PpudCreateRecallResponse> {
    return post(
      "/offender/$offenderId/release/$releaseId/recall",
      request,
      object : ParameterizedTypeReference<PpudCreateRecallResponse>() {},
    )
  }

  fun uploadMandatoryDocument(
    recallId: String,
    request: PpudUploadMandatoryDocumentRequest,
  ): Mono<ResponseEntity<Void>> {
    return put("/recall/$recallId/mandatory-document", request)
  }

  fun uploadAdditionalDocument(
    recallId: String,
    request: PpudUploadAdditionalDocumentRequest,
  ): Mono<ResponseEntity<Void>> {
    return put("/recall/$recallId/additional-document", request)
  }

  fun createMinute(
    recallId: String,
    request: PpudCreateMinuteRequest,
  ): Mono<ResponseEntity<Void>> {
    return put("/recall/$recallId/minutes", request)
  }

  fun retrieveList(name: String): Mono<PpudReferenceListResponse> {
    return get("/reference/$name", object : ParameterizedTypeReference<PpudReferenceListResponse>() {})
  }

  fun searchActiveUsers(request: PpudUserSearchRequest): Mono<PpudUserResponse> {
    return post("/user/search", request, object : ParameterizedTypeReference<PpudUserResponse>() {})
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

  private fun <T> get(url: String, responseType: ParameterizedTypeReference<T>): Mono<T> {
    return webClient
      .get()
      .uri { builder -> builder.path(url).build() }
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(ppudAutomationTimeout))
      .doOnError { ex ->
        handleTimeoutException(ex)
      }
      .withRetry()
  }

  private fun <T> post(url: String, request: Any, responseType: ParameterizedTypeReference<T>): Mono<T> {
    return webClient
      .post()
      .uri { builder -> builder.path(url).build() }
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

  private fun put(url: String, request: Any): Mono<ResponseEntity<Void>> {
    return webClient
      .put()
      .uri { builder -> builder.path(url).build() }
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
}
