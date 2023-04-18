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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.GroupedDocuments
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.DocumentNotFoundException
import java.time.Duration
import java.util.concurrent.TimeoutException

@Deprecated("Migrating to DeliusClient")
class CommunityApiClient(
  private val webClient: WebClient,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  private val timeoutCounter: Counter
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
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
