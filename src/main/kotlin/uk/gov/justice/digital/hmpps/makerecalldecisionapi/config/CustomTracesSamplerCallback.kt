package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import io.sentry.SamplingContext
import io.sentry.SentryOptions.TracesSamplerCallback
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

const val defaultSampleRate = 0.05
const val noSampleRate = 0.0

@Component
class CustomTracesSamplerCallback : TracesSamplerCallback {
  override fun sample(context: SamplingContext): Double =
    context.customSamplingContext?.let { customSamplingContext ->
      val request = customSamplingContext["request"] as HttpServletRequest
      when (request.requestURI) {
        // The health check endpoints are just noise - drop all transactions
        "/health/liveness" -> noSampleRate
        "/health/readiness" -> noSampleRate
        else -> defaultSampleRate
      }
    } ?: defaultSampleRate
}
