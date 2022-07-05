package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoActiveConvictionsException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ReleaseDetailsNotFoundException

fun <T : Any> getValueAndHandleWrappedException(mono: Mono<T>?): T? {
  return try {
    val value = mono?.block()
    value ?: value
  } catch (wrappedException: RuntimeException) {
    when (wrappedException.cause) {
      is ClientTimeoutException -> throw wrappedException.cause as ClientTimeoutException
      is PersonNotFoundException -> throw wrappedException.cause as PersonNotFoundException
      is NoActiveConvictionsException -> throw wrappedException.cause as NoActiveConvictionsException
      is ReleaseDetailsNotFoundException -> throw wrappedException as ReleaseDetailsNotFoundException
      else -> throw wrappedException
    }
  }
}
