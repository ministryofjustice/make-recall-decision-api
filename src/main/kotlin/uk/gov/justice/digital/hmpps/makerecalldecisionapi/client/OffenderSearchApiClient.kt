package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchPagedResults
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchPeopleRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NotFoundException
import java.time.Duration
import java.util.concurrent.TimeoutException

class OffenderSearchApiClient(
  private val webClient: WebClient,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  private val timeoutCounter: Counter,
) {

  fun searchPeople(
    crn: String? = null,
    firstName: String? = null,
    surname: String? = null,
    page: Int,
    pageSize: Int,
  ): Mono<OffenderSearchPagedResults> {
    val request = OffenderSearchPeopleRequest(crn = crn, firstName = firstName, surname = surname)
    val responseType = object : ParameterizedTypeReference<OffenderSearchPagedResults>() {}
    return webClient
      .post()
      .uri { builder ->
        builder.path("/search/people")
          .queryParam("page", page)
          .queryParam("size", pageSize)
          .build()
      }
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .body(fromValue(request))
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw NotFoundException("Offender search endpoint returned offender not found") },
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { ex ->
        when (ex) {
          is TimeoutException -> {
            timeoutCounter.increment()

            throw ClientTimeoutException(
              "Offender Search API Client - search by phrase endpoint",
              "No response within $nDeliusTimeout seconds",
            )
          }
        }
      }
      .retry(2)
  }
}
