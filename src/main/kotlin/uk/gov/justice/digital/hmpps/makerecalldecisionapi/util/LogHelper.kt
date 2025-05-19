package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

class LogHelper {

  companion object Helper {

    fun redact(value: String?): String = if (value == null) {
      "null"
    } else {
      value.first() + "*".repeat(value.length - 1)
    }
  }
}
