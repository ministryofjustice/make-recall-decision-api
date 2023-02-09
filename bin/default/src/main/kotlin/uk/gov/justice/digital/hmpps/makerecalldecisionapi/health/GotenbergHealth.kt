package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("gotenberg")
class GotenbergHealth(
  webClientNoAuthNoMetrics: WebClient,
  @Value("gotenberg") componentName: String,
  @Value("\${gotenberg.endpoint.url}") endpointUrl: String
) : PingHealthCheck(webClientNoAuthNoMetrics, componentName, "$endpointUrl/health")
