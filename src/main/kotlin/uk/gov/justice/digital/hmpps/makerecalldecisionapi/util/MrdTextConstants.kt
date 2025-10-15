package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

class MrdTextConstants {

  companion object Constants {
    const val NOT_SPECIFIED = "Not specified"
    const val WHITE_SPACE = " "
    const val EMPTY_STRING = ""
    const val NO = "No"
    const val YES = "Yes"
    const val NOT_APPLICABLE = "N/A"
    const val SCORE_NOT_APPLICABLE = "NOT_APPLICABLE"
    const val TICK_CHARACTER = 0x2713.toChar().toString()
    const val NO_NAME_AVAILABLE = "No name available"
    const val HAS_VULNERABILITIES = "Yes, has vulnerabilities or needs"
    const val HAS_NO_VULNERABILITIES = "No concerns about vulnerabilities or needs"
    const val UNKNOWN_VULNERABILITIES = "Do not know about vulnerabilities or needs"

    // We use a very early date to avoid incorrect orderings of entities with null date times.
    // We deliberately don't use LocalDateTime.MIN, as that produces a negative year and there
    // are representations of the '-' (minus) symbol which lexicographically come after numbers
    // do in ASCII and we don't want to risk it resulting in this date being considered the
    // latest date
    const val DEFAULT_DATE_TIME_FOR_NULL_VALUE = "1200-01-01T00:00:00.000"
  }
}
