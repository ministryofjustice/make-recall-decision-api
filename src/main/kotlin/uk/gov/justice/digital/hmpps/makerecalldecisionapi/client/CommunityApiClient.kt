package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.RegistrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.concurrent.TimeoutException

class CommunityApiClient(
  private val webClient: WebClient,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long
) {
  fun getRegistrations(crn: String): Mono<RegistrationsResponse> {
    val responseType = object : ParameterizedTypeReference<RegistrationsResponse>() {}
    return webClient
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
  }

  fun getConvictions(crn: String): Mono<List<ConvictionResponse>> {
    val responseType = object : ParameterizedTypeReference<List<ConvictionResponse>>() {}
    return webClient
      .get()
      .uri("/secure/offenders/crn/$crn/convictions")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "convictions"
        )
      }
  }

  fun getAllOffenderDetails(crn: String): Mono<AllOffenderDetailsResponse> {
    val responseType = object : ParameterizedTypeReference<AllOffenderDetailsResponse>() {}
    return webClient
      .get()
      .uri("/secure/offenders/crn/$crn/all")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "all offenders"
        )
      }
  }

  fun getContactSummary(crn: String): Mono<ContactSummaryResponseCommunity> {
    val responseType = object : ParameterizedTypeReference<ContactSummaryResponseCommunity>() {}

    val url = "/secure/offenders/crn/$crn/contact-summary"

    return webClient
      .get()
      .uri {
        it.path(url)
          .queryParam("contactTypes", "MO5", "LCL", "C204", "CARR", "C123", "C071", "COAP", "RECI")
          .build()
      }
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "contact summary"
        )
      }
  }

  fun getReleaseSummary(crn: String): Mono<ReleaseSummaryResponse> {
    val responseType = object : ParameterizedTypeReference<ReleaseSummaryResponse>() {}
    return webClient
      .get()
      .uri("/secure/offenders/crn/$crn/release")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "release summary"
        )
      }
  }

  private fun handleTimeoutException(
    exception: Throwable?,
    endPoint: String
  ) {
    when (exception) {
      is TimeoutException -> {
        throw ClientTimeoutException(
          "Community API Client - $endPoint endpoint",
          "No response within $nDeliusTimeout seconds"
        )
      }
    }
  }
}
