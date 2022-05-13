package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ContactSummaryResponseCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.RegistrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ReleaseSummaryResponse

class CommunityApiClient(private val webClient: WebClient) {

  fun getRegistrations(crn: String): Mono<RegistrationsResponse> {
    val responseType = object : ParameterizedTypeReference<RegistrationsResponse>() {}
    return webClient
      .get()
      .uri("/secure/offenders/crn/$crn/registrations")
      .retrieve()
      .bodyToMono(responseType)
  }

  fun getConvictions(crn: String): Mono<List<ConvictionResponse>> {
    val responseType = object : ParameterizedTypeReference<List<ConvictionResponse>>() {}
    return webClient
      .get()
      .uri("/secure/offenders/crn/$crn/convictions")
      .retrieve()
      .bodyToMono(responseType)
  }

  fun getAllOffenderDetails(crn: String): Mono<AllOffenderDetailsResponse> {
    val responseType = object : ParameterizedTypeReference<AllOffenderDetailsResponse>() {}
    return webClient
      .get()
      .uri("/secure/offenders/crn/$crn/all")
      .retrieve()
      .bodyToMono(responseType)
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
  }

  fun getReleaseSummary(crn: String): Mono<ReleaseSummaryResponse> {
    val responseType = object : ParameterizedTypeReference<ReleaseSummaryResponse>() {}
    return webClient
      .get()
      .uri("/secure/offenders/crn/$crn/release")
      .retrieve()
      .bodyToMono(responseType)
  }
}
