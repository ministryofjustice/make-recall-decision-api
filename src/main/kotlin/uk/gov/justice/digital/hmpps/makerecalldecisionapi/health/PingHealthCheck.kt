package uk.gov.justice.digital.hmpps.makerecalldecisionapi.health

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration

abstract class PingHealthCheck(
  private val webClient: WebClient,
  private val componentName: String,
  private val healthUrl: String,
  private val timeout: Duration = Duration.ofSeconds(1)
) : HealthIndicator {

  @Autowired
  private val meterRegistry: MeterRegistry? = null

  override fun health(): Health? {
    val result = try {
      webClient.get()
        .uri(healthUrl)
        .retrieve()
        .toEntity(String::class.java)
        .flatMap { upWithStatus(it) }
        .onErrorResume(WebClientResponseException::class.java) { downWithResponseBody(it) }
        .onErrorResume(Exception::class.java) { downWithException(it) }
        .block(timeout)
    } catch (ex: Exception) {
      Health.unknown().withBody(ex.toString()).build()
    }

    recordHealthMetric(result)

    return result
  }

  private fun recordHealthMetric(result: Health?) {
    var gaugeVal = 0

    if (result?.status == Status.UP) {
      gaugeVal = 1
    }

    meterRegistry?.gauge("upstream_healthcheck", Tags.of("service", componentName), gaugeVal)
  }

  private fun downWithException(it: Exception) = Mono.just(Health.down(it).build())

  private fun downWithResponseBody(it: WebClientResponseException) =
    Mono.just(
      Health.down(it).withBody(it.responseBodyAsString).withHttpStatus(it.statusCode).build()
    )

  private fun upWithStatus(it: ResponseEntity<String>): Mono<Health> =
    Mono.just(Health.up().withHttpStatus(it.statusCode).build())

  private fun Health.Builder.withHttpStatus(status: HttpStatus) = this.withDetail("status", status)

  private fun Health.Builder.withBody(body: String) = this.withDetail("body", body)
}
