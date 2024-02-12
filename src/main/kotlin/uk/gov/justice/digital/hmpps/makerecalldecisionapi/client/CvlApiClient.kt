package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionSearch
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceMatchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoCvlLicenceByIdException
import java.time.Duration
import java.util.concurrent.TimeoutException

class CvlApiClient(
  private val webClient: WebClient,
  @Value("\${cvl.client.timeout}") private val cvlTimeout: Long,
  private val timeoutCounter: Counter,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getLicenceMatch(crn: String, licenceConditionSearch: LicenceConditionSearch): Mono<List<LicenceMatchResponse>> {
    log.info(normalizeSpace("About to get licence match from CVL for crn $crn with nomis number ${licenceConditionSearch.nomsId}"))

    val responseType = object : ParameterizedTypeReference<List<LicenceMatchResponse>>() {}
    val result = webClient
      .post()
      .uri("/licence/match")
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .body(fromValue(licenceConditionSearch))
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(cvlTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "licence match",
        )
      }
      .retry(2)
    log.info(normalizeSpace("Returning licence match from CVL for $crn with nomis number ${licenceConditionSearch.nomsId}"))
    return result
  }

  fun getLicenceById(crn: String, licenceId: Int): Mono<LicenceConditionCvlResponse> {
    log.info(normalizeSpace("About to get licence from CVL for CRN $crn and licence id $licenceId"))

    val responseType = object : ParameterizedTypeReference<LicenceConditionCvlResponse>() {}
    val result = webClient
      .get()
      .uri("/licence/id/$licenceId")
      .retrieve()
      .onStatus(
        { httpStatus -> HttpStatus.NOT_FOUND == httpStatus },
        { throw NoCvlLicenceByIdException("No licence for licence id $licenceId found in CVL for crn: $crn") },
      )
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(cvlTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "licence by id",
        )
      }
      .retry(2)
    log.info(normalizeSpace("Returning licences from CVL for $crn and licence id $licenceId"))
    return result
  }

  private fun handleTimeoutException(
    exception: Throwable?,
    endPoint: String,
  ) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()

        throw ClientTimeoutException(
          "CVL API Client - $endPoint endpoint",
          "No response within $cvlTimeout seconds",
        )
      }
    }
  }
}
