package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException

fun <T : Any> getValueAndHandleWrappedException(mono: Mono<T>?): T? {
  return try {
    val value = mono?.block()
    value ?: value
  } catch (wrappedException: RuntimeException) {
    when (wrappedException.cause) {
      is ClientTimeoutException -> throw wrappedException.cause as ClientTimeoutException
      else -> throw wrappedException
    }
  }
}
