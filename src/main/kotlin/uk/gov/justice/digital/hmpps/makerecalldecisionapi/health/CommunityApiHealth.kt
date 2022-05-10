package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("communityApi")
class CommunityApiHealth(
  webClientNoAuthNoMetrics: WebClient,
  @Value("communityApi") componentName: String,
  @Value("\${community.api.endpoint.url}") endpointUrl: String
) : PingHealthCheck(webClientNoAuthNoMetrics, componentName, "$endpointUrl/ping")
