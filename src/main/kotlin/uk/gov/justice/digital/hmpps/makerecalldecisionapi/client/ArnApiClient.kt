package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ContingencyPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskManagementResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.concurrent.TimeoutException

class ArnApiClient(
  private val webClient: WebClient,
  @Value("\${oasys.arn.client.timeout}") private val arnClientTimeout: Long,
  private val timeoutCounter: Counter
) {
  fun getRiskSummary(crn: String): Mono<RiskSummaryResponse> {
    val responseType = object : ParameterizedTypeReference<RiskSummaryResponse>() {}
    return webClient
      .get()
      .uri("/risks/crn/$crn/summary")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "risk summary"
        )
      }
  }

  fun getAssessments(crn: String): Mono<AssessmentsResponse> {
    val responseType = object : ParameterizedTypeReference<AssessmentsResponse>() {}
    return webClient
      .get()
      .uri("/assessments/crn/$crn/offence")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "assessments"
        )
      }
  }

  fun getContingencyPlan(crn: String): Mono<ContingencyPlanResponse> {
    val responseType = object : ParameterizedTypeReference<ContingencyPlanResponse>() {}
    return webClient
      .get()
      .uri("/assessments/risk-management-plans/$crn/ALLOW")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "risk contingency plan"
        )
      }
  }

  fun getRiskScores(crn: String): Mono<List<RiskScoreResponse>> {
    val responseType = object : ParameterizedTypeReference<List<RiskScoreResponse>>() {}
    return webClient
      .get()
      .uri("/risks/crn/$crn/predictors/all")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "risk scores"
        )
      }
  }

  fun getRiskManagementPlan(crn: String): Mono<RiskManagementResponse> {
    val responseType = object : ParameterizedTypeReference<RiskManagementResponse>() {}
    return webClient
      .get()
      .uri("/risks/crn/$crn/risk-management-plan")
      .retrieve()
      .bodyToMono(responseType)
      .timeout(Duration.ofSeconds(arnClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "risk management plan"
        )
      }
  }

  private fun handleTimeoutException(
    exception: Throwable?,
    endPoint: String
  ) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()

        throw ClientTimeoutException(
          "ARN API Client - $endPoint endpoint",
          "No response within $arnClientTimeout seconds"
        )
      }
    }
  }
}
