package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateTimeHelper {

  companion object Helper {
    fun nowDate(): String {
      val formatter = DateTimeFormat.forPattern("ddMMyyyy")
      return formatter.print(DateTime.now()).toString()
    }

    fun nowDateTime(): String {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      return formatter.print(DateTime(DateTimeZone.UTC)).toString()
    }

    fun convertLocalDateToReadableDate(date: LocalDate?): String {
      val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

      return date?.format(formatter).toString()
    }
  }
}
