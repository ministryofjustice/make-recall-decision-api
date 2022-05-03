package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ConvictionResponse

class CommunityApiClient(private val webClient: WebClient) {
  fun getConvictions(crn: String): Mono<List<ConvictionResponse>> {
    val responseType = object : ParameterizedTypeReference<List<ConvictionResponse>>() {}
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
