package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException

class ExceptionCodeHelper {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  object Helper {
    fun extractErrorCode(exception: Exception, task: String, crn: String): String {
      return when (exception) {
        is ClientTimeoutException -> {
          log.error("Timeout trying to get $task for CRN: $crn - ${exception.message}")
          "TIMEOUT"
        }
        is WebClientResponseException -> {
          if (exception.rawStatusCode == 404) {
            log.info("Not found when trying to get $task for CRN: $crn")
            "NOT_FOUND"
          } else {
            log.error("WebClientResponseException: Server error trying to get $task for CRN: $crn - ${exception.message}")
            "SERVER_ERROR"
          }
        }
        else -> {
          log.error("Generic: Server error trying to get $task for CRN: $crn - ${exception.message}")
          "SERVER_ERROR"
        }
      }
    }
  }
}
