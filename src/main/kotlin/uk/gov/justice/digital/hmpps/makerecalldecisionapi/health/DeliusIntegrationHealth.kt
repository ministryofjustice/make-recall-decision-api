package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("deliusIntegration")
class DeliusIntegrationHealth(
  webClientNoAuthNoMetrics: WebClient,
  @Value("deliusIntegration") componentName: String,
  @Value("\${delius.integration.endpoint.url}") endpointUrl: String,
) : PingHealthCheck(webClientNoAuthNoMetrics, componentName, "$endpointUrl/health")
