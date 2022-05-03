package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OffenderSearchByPhraseRequest

class OffenderSearchApiClient(private val webClient: WebClient) {

  fun searchOffenderByPhrase(request: OffenderSearchByPhraseRequest): Mono<OffenderDetailsResponse> {
    val responseType = object : ParameterizedTypeReference<OffenderDetailsResponse>() {}
    return webClient
      .post()
      .uri { builder ->
        builder.path("/phrase").queryParam("paged", "false").build()
      }
      .body(fromValue(request))
      .retrieve()
      .bodyToMono(responseType)
  }

}
