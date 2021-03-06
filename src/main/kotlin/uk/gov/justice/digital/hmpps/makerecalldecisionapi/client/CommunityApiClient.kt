package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.GroupedDocuments
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.MappaResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.RegistrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.DocumentNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoActiveConvictionsException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ReleaseDetailsNotFoundException
import java.time.Duration
import java.util.concurrent.TimeoutException

class CommunityApiClient(
  private val webClient: WebClient,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  private val timeoutCounter: Counter
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getRegistrations(crn: String): Mono<RegistrationsResponse> {
    log.info(normalizeSpace("Second about to get registrations for $crn"))

    val responseType = object : ParameterizedTypeReference<RegistrationsResponse>() {}
    val result = webClient
      .get()
      .uri("/secure/offenders/crn/$crn/registrations")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "registrations"
        )
      }
    log.info(normalizeSpace("Returning registrations for $crn"))
    return result
  }

  fun getActiveConvictions(crn: String): Mono<List<Conviction>> {
    log.info(normalizeSpace("About to get active convictions for $crn"))
    val responseType = object : ParameterizedTypeReference<List<Conviction>>() {}

    val result = webClient
      .get()
      .uri {
        it.path("/secure/offenders/crn/$crn/convictions")
          .queryParam("activeOnly", true)
          .build()
      }
      .retrieve()
      .onStatus(
        { httpStatus -> HttpStatus.NOT_FOUND == httpStatus },
        { throw NoActiveConvictionsException("No active convictions present for crn: $crn") }
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .onErrorResume { ex ->
        when (ex) {
          is NoActiveConvictionsException -> Mono.fromCallable { emptyList() }
          else -> Mono.error(ex)
        }
      }
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "convictions"
        )
      }

    log.info(normalizeSpace("Returning active convictions for $crn"))

    return result
  }

  fun getLicenceConditionsByConvictionId(crn: String, convictionId: Long?): Mono<LicenceConditions> {
    log.info(normalizeSpace("About to get licence conditions for $crn by convictionId $convictionId"))

    val responseType = object : ParameterizedTypeReference<LicenceConditions>() {}
    val result = webClient
      .get()
      .uri {
        it.path("/secure/offenders/crn/$crn/convictions/$convictionId/licenceConditions")
          .build()
      }
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "licenceConditions"
        )
      }
    log.info(normalizeSpace("Returning licence conditions for $crn by convictionId $convictionId"))
    return result
  }

  fun getAllMappaDetails(crn: String): Mono<MappaResponse> {
    val responseType = object : ParameterizedTypeReference<MappaResponse>() {}
    return webClient
      .get()
      .uri("/secure/offenders/crn/$crn/risk/mappa")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "mappa"
        )
      }
  }

  fun getAllOffenderDetails(crn: String): Mono<AllOffenderDetailsResponse> {
    log.info(normalizeSpace("About to get all offender details for $crn"))

    val responseType = object : ParameterizedTypeReference<AllOffenderDetailsResponse>() {}
    val result = webClient
      .get()
      .uri("/secure/offenders/crn/$crn/all")
      .retrieve()
      .onStatus(
        { httpStatus -> HttpStatus.NOT_FOUND == httpStatus },
        { throw PersonNotFoundException("No details available for crn: $crn") }
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "all offenders"
        )
      }
    log.info(normalizeSpace("Returning all offender details for $crn"))
    return result
  }

  fun getContactSummary(crn: String): Mono<ContactSummaryResponseCommunity> {
    log.info(normalizeSpace("About to get contact summary for $crn"))

    val responseType = object : ParameterizedTypeReference<ContactSummaryResponseCommunity>() {}

    val result = webClient
      .get()
      .uri("/secure/offenders/crn/$crn/contact-summary")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "contact summary"
        )
      }
    log.info(normalizeSpace("Returning contact summary for $crn"))
    return result
  }

  fun getReleaseSummary(crn: String): Mono<ReleaseSummaryResponse> {
    log.info(normalizeSpace("About to get release summary for $crn"))

    val responseType = object : ParameterizedTypeReference<ReleaseSummaryResponse>() {}
    val result = webClient
      .get()
      .uri("/secure/offenders/crn/$crn/release")
      .retrieve()
      .onStatus(
        { httpStatus -> HttpStatus.BAD_REQUEST == httpStatus },
        { throw ReleaseDetailsNotFoundException("No release details found for case $crn") }
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .onErrorResume { ex ->
        when (ex) {
          is ReleaseDetailsNotFoundException -> Mono.fromCallable { ReleaseSummaryResponse(lastRelease = null, lastRecall = null) }
          else -> Mono.error(ex)
        }
      }
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "release summary"
        )
      }
    log.info(normalizeSpace("Returning release summary for $crn"))
    return result
  }

  fun getUserAccess(crn: String): Mono<UserAccessResponse> {
    log.info(normalizeSpace("About to check user access details for $crn"))

    val responseType = object : ParameterizedTypeReference<UserAccessResponse>() {}
    val result = webClient
      .get()
      .uri("/secure/offenders/crn/$crn/userAccess")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "user access"
        )
      }
    log.info(normalizeSpace("Returning user access details for $crn"))
    return result
  }

  fun getGroupedDocuments(crn: String): Mono<GroupedDocuments> {
    log.info(normalizeSpace("About to get all grouped documents for $crn"))

    val responseType = object : ParameterizedTypeReference<GroupedDocuments>() {}
    val result = webClient
      .get()
      .uri("/secure/offenders/crn/$crn/documents/grouped")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "grouped documents"
        )
      }
    log.info(normalizeSpace("Returning all grouped documents for $crn"))
    return result
  }

  fun getDocumentByCrnAndId(crn: String, documentId: String): Mono<ResponseEntity<Resource>> {
    log.info(normalizeSpace("About to get document for $crn and documentId $documentId"))

    val result = webClient
      .get()
      .uri("/secure/offenders/crn/$crn/documents/$documentId")
      .retrieve()
      .onStatus(
        { httpStatus -> HttpStatus.NOT_FOUND == httpStatus },
        { throw DocumentNotFoundException("No document found for crn: $crn and documentId: $documentId") }
      )
      .toEntity(Resource::class.java)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "document by CRN and ID"
        )
      }

    log.info(normalizeSpace("Returning document for $crn and documentId $documentId"))
    return result
  }

  private fun handleTimeoutException(
    exception: Throwable?,
    endPoint: String
  ) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()

        throw ClientTimeoutException(
          "Community API Client - $endPoint endpoint",
          "No response within $nDeliusTimeout seconds"
        )
      }
    }
  }
}
