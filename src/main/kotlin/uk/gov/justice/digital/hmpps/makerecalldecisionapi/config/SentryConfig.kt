package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import io.opentelemetry.api.trace.Span
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SentryContextAppender : HandlerInterceptor {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  @Throws(Exception::class)
  override fun preHandle(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any
  ): Boolean {
    val operationId: String = Span.current().spanContext.traceId

    log.info("[preHandle] ${request.method} ${request.requestURI} - operationId: $operationId")

    Sentry.configureScope { scope ->
      scope.setContexts("appInsightsOperationId", operationId)
    }

    return true
  }
}

@Component
class SentryBeforeSendCallback : SentryOptions.BeforeSendCallback {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  override fun execute(event: SentryEvent, hint: Any?): SentryEvent? {
    log.info("event: '{}', throwable: '{}', hint: '{}'", event, event.throwable, hint)

    if (event.throwable is ClientTimeoutException) {
      return null
    }

    return event
  }
}
