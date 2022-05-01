package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OffenderSearchByPhraseRequest
import java.time.LocalDate

@Service//TODO remove this and have Weclient Config includent OffenderSearchApi ... follow CommunitApiPattern
// see WebClientConfiguration
class OffenderSearchApiClient(private val webClient: WebClient) {

  fun searchOffenderByPhrase(request: OffenderSearchByPhraseRequest) : OffenderDetailsResponse {
    val crn = "X123456"
    return OffenderDetailsResponse(
      name = "Pontius Pilate",
      dateOfBirth = LocalDate.parse("2000-04-26"),
      crn = crn
    )
  }

//  fun getConvictions(crn: String): Mono<List<Conviction>> {

//    val responseType = object : ParameterizedTypeReference<List<Conviction>>() {}
//    return webClient
//      .get()
//      .uri("/offenders/crn/$crn/convictions")
//      .retrieve()
//      .bodyToMono(responseType)
//  }
//  }


}
