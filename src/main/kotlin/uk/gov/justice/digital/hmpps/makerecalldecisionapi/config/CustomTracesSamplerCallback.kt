package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import io.sentry.SamplingContext
import io.sentry.SentryOptions.TracesSamplerCallback
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

const val DEFAULT_SAMPLE_RATE = 0.05
const val NO_SAMPLE_RATE = 0.0

@Component
class CustomTracesSamplerCallback : TracesSamplerCallback {
  override fun sample(context: SamplingContext): Double = context.customSamplingContext?.let { customSamplingContext ->
    val request = customSamplingContext["request"] as HttpServletRequest
    when (request.requestURI) {
      // The health check endpoints are just noise - drop all transactions
      "/health/liveness" -> NO_SAMPLE_RATE
      "/health/readiness" -> NO_SAMPLE_RATE
      else -> DEFAULT_SAMPLE_RATE
    }
  } ?: DEFAULT_SAMPLE_RATE
}
