package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
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

    fun dateTimeWithDaylightSavingFromString(utcDateTimeString: String?): LocalDateTime = dateTimeUTCZonedFromString(utcDateTimeString).withZoneSameInstant(ZoneId.of("Europe/London")).toLocalDateTime()

    fun dateTimeUTCZonedFromString(utcDateTimeString: String?): ZonedDateTime = if (utcDateTimeString.isNullOrBlank()) {
      ZonedDateTime.of(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC), ZoneId.of("UTC"))
        .minusHours(1)
    } else {
      val utcZonedDateTime = ZonedDateTime.of(
        LocalDateTime.parse(utcDateTimeString.replace("Z", MrdTextConstants.EMPTY_STRING)),
        ZoneId.of("UTC"),
        // Currently, changing from a UTC Date for a Europe/London date results in an offset of +01:00
        // due to the British Standard Time experiment of 1968 - 1971
        // As this method is new we are not concerned with the default not being 1970-01-01T00:00 but this is the
        // expected default for dateTimeWithDaylightSavingFromString above which now uses this method.
        // As such we have decided to account for the offest here until we can fully determine the impact
        // of changing the default value for that method call
      )
      utcZonedDateTime
    }

    fun localNowDateTime(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

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
      val localDateTime = if (input.isNullOrBlank()) {
        LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
      } else {
        LocalDateTime.parse(input)
      }
      val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX", Locale.ENGLISH)
      return localDateTime.atOffset(ZoneOffset.UTC).format(formatter)
    }

    fun convertDateStringToIso8601Date(input: String?): LocalDate? {
      val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
      return input?.let { LocalDate.parse(it, formatter) }
    }

    fun convertToLondonTimezone(localDateTimeInUtc: LocalDateTime): LocalDateTime = localDateTimeInUtc.atOffset(UTC)
      .atZoneSameInstant(ZoneId.of("Europe/London"))
      .toLocalDateTime()
  }
}
