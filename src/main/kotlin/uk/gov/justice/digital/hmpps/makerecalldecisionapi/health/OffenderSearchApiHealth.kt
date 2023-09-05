package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("offenderSearchApi")
class OffenderSearchApiHealth(
  webClientNoAuthNoMetrics: WebClient,
  @Value("offenderSearchApi") componentName: String,
  @Value("\${offender.search.endpoint.url}") endpointUrl: String,
) : PingHealthCheck(webClientNoAuthNoMetrics, componentName, "$endpointUrl/health/ping")
