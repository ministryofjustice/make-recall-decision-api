package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    fun convertLocalDateTimeToDateWithSlashes(date: LocalDateTime?): String {
      val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

      return date?.format(formatter) ?: ""
    }

    fun splitDateTime(dateTime: LocalDateTime?): Pair<String?, String?> {
      val formatterDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
      val formatterTime = DateTimeFormatter.ofPattern("HH:mm")
      return Pair(dateTime?.format(formatterDate), dateTime?.format(formatterTime))
    }

    fun convertUtcDateTimeStringToIso8601Date(input: String?): String? {
      val offsetDateTime: OffsetDateTime = LocalDateTime.parse(input).atOffset(ZoneOffset.UTC)
      val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX", Locale.ENGLISH)
      return offsetDateTime.format(formatter)
    }

    fun convertDateStringToIso8601Date(input: String?): LocalDate? {
      val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
      return input?.let { LocalDate.parse(it, formatter) }
    }

    fun localDateTimeFromString(localDateTimeString: String?): LocalDateTime =
      LocalDateTime.parse(
        localDateTimeString?.replace(
          "Z",
          MrdTextConstants.EMPTY_STRING
        )
      )
  }
}
