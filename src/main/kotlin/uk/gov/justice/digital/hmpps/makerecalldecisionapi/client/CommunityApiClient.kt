package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Conviction

class CommunityApiClient(private val webClient: WebClient) {
  fun getConvictions(crn: String): Mono<List<Conviction>> {
    val responseType = object : ParameterizedTypeReference<List<Conviction>>() {}
    return webClient
      .get()
      .uri("/offenders/crn/$crn/convictions")
      .retrieve()
      .bodyToMono(responseType)
  }

  fun getAllOffenderDetails(crn: String): Mono<AllOffenderDetailsResponse> {
    val responseType = object : ParameterizedTypeReference<AllOffenderDetailsResponse>() {}
    return webClient
      .get()
      .uri("/offenders/crn/$crn/all")
      .retrieve()
      .bodyToMono(responseType)
  }
}
