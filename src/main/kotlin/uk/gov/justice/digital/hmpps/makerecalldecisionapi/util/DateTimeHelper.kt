package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DateTimeHelper {

  companion object Helper {

    fun nowDate(): String {
      val formatter = DateTimeFormat.forPattern("ddMMyyyy")
      return formatter.print(DateTime.now()).toString()
    }

    fun utcNowDateTimeString(): String {
      val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      return formatter.print(DateTime(DateTimeZone.UTC)).toString()
    }

    fun localNowDateTime(): LocalDateTime {
      return LocalDateTime.now()
    }

    fun convertLocalDateToReadableDate(date: LocalDate?): String {
      val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

      return date?.format(formatter) ?: ""
    }

    fun convertLocalDateToDateWithSlashes(date: LocalDate?): String {
      val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

      return date?.format(formatter) ?: ""
    }

    fun splitDateTime(dateTime: LocalDateTime?): Pair<String?, String?> {
      val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
      val formatterTime = DateTimeFormatter.ofPattern("HH:mm")
      return Pair(dateTime?.format(formatterDate), dateTime?.format(formatterTime))
    }

    fun convertUtcDateTimeStringToIso8601Date(utcDateTime: String?): String? {
      return if (utcDateTime != null) {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val localDateTime = LocalDateTime.parse(utcDateTime, inputFormatter)
        val outputFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX")

        localDateTime.atOffset(ZoneOffset.UTC).format(outputFormatter)
      } else null
    }
  }
}
