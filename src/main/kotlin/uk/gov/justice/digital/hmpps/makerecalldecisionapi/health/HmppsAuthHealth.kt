package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("hmppsAuth")
class HmppsAuthHealth(
  webClientNoAuthNoMetrics: WebClient,
  @Value("hmppsAuth") componentName: String,
  @Value("\${oauth.endpoint.url}") hmppsAuthEndpointUrl: String
) : PingHealthCheck(webClientNoAuthNoMetrics, componentName, "$hmppsAuthEndpointUrl/health/ping")
