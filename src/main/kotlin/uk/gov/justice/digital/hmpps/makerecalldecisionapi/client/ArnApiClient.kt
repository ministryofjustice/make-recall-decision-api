package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskManagementResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.concurrent.TimeoutException

class ArnApiClient(
  private val webClient: WebClient,
  @Value("\${oasys.arn.client.timeout}") private val arnClientTimeout: Long,
  private val timeoutCounter: Counter,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getRiskSummary(crn: String): Mono<RiskSummaryResponse> {
    val responseType = object : ParameterizedTypeReference<RiskSummaryResponse>() {}
    log.info(StringUtils.normalizeSpace("About to get risk summary for $crn"))

    val result = webClient
      .get()
      .uri("/risks/crn/$crn/summary")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "risk summary",
        )
      }
    log.info(StringUtils.normalizeSpace("Returning risk summary for $crn"))
    return result
  }

  fun getAssessments(crn: String): Mono<AssessmentsResponse> {
    val responseType = object : ParameterizedTypeReference<AssessmentsResponse>() {}
    log.info(StringUtils.normalizeSpace("About to get assessments for $crn"))

    val result = webClient
      .get()
      .uri("/assessments/crn/$crn/offence")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "assessments",
        )
      }
    log.info(StringUtils.normalizeSpace("Returning assessments for $crn"))
    return result
  }

  fun getRiskScores(crn: String): Mono<List<RiskScoreResponse>> {
    val responseType = object : ParameterizedTypeReference<List<RiskScoreResponse>>() {}
    log.info(StringUtils.normalizeSpace("About to get risk scores for $crn"))

    val result = webClient
      .get()
      .uri("/risks/crn/$crn/predictors/all")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "risk scores",
        )
      }
    log.info(StringUtils.normalizeSpace("Returning risk scores for $crn"))
    return result
  }

  fun getRiskManagementPlan(crn: String): Mono<RiskManagementResponse> {
    val responseType = object : ParameterizedTypeReference<RiskManagementResponse>() {}
    log.info(StringUtils.normalizeSpace("About to get risk management plan for $crn"))

    val result = webClient
      .get()
      .uri("/risks/crn/$crn/risk-management-plan")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "risk management plan",
        )
      }
    log.info(StringUtils.normalizeSpace("Returning risk management plan for $crn"))
    return result
  }

  fun getRisksWithFullText(crn: String): Mono<RiskResponse> {
    val responseType = object : ParameterizedTypeReference<RiskResponse>() {}
    log.info(StringUtils.normalizeSpace("About to get risks with full text for $crn"))

    val result = webClient
      .get()
      .uri("/risks/crn/$crn/fulltext")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "risks",
        )
      }
    log.info(StringUtils.normalizeSpace("Returning all risks with full text for $crn"))
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
          "ARN API Client - $endPoint endpoint",
          "No response within $arnClientTimeout seconds",
        )
      }
    }
  }
}
