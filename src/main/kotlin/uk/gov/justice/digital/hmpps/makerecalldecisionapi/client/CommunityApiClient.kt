package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.RegistrationsResponse

class CommunityApiClient(private val webClient: WebClient) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getRegistrations(crn: String): Mono<RegistrationsResponse> {
    val responseType = object : ParameterizedTypeReference<RegistrationsResponse>() {}
    val registrationsEndpointResponse = webClient
      .get()
      .uri("/offenders/crn/$crn/registrations")
      .retrieve()
      .bodyToMono(responseType)
    log.info("retrieved response from community-api registrations endpoint for crn:: $crn")
    return registrationsEndpointResponse
  }

  fun getConvictions(crn: String): Mono<List<ConvictionResponse>> {
    val responseType = object : ParameterizedTypeReference<List<ConvictionResponse>>() {}
    val convictionsEndpointResponse = webClient
      .get()
      .uri("/offenders/crn/$crn/convictions")
      .retrieve()
      .bodyToMono(responseType)
    log.info("retrieved response from community-api convictions endpoint for crn:: $crn")
    return convictionsEndpointResponse
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
