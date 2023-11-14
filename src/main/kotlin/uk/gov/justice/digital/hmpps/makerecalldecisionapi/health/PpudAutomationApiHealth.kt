package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("ppudAutomationApi")
class PpudAutomationApiHealth(
  webClientNoAuthNoMetrics: WebClient,
  @Value("ppudAutomationApi") componentName: String,
  @Value("\${ppud-automation.api.endpoint.url}") endpointUrl: String,
) : PingHealthCheck(webClientNoAuthNoMetrics, componentName, "$endpointUrl/health/ping", isOptionalComponent = true)
