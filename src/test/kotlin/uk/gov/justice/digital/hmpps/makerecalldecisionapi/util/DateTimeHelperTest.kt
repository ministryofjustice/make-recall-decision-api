package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertUtcDateTimeStringToIso8601Date
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.dateTimeWithDaylightSavingFromString
import java.time.LocalDateTime

class DateTimeHelperTest {

  @Test
  fun `given UTC date time string then convert to ISO8601 date`() {
    val result = convertUtcDateTimeStringToIso8601Date("2022-10-02T14:20:27")
    assertThat(result).isEqualTo("2022-10-02T14:20:27.000Z")
  }

  @ParameterizedTest
  @CsvSource(
    "2022-06-02T14:20:27.123Z,2022-06-02T15:20:27.123",
    "2022-12-02T14:20:27.123Z,2022-12-02T14:20:27.123",
    " ,1970-01-01T00:00:00.000",
    ",1970-01-01T00:00:00.000",
  )
  fun `given UTC date time string when dateTimeWithDaylightSavingFromString called then return london time`(
    utc: String?,
    expected: String,
  ) {
    val result = dateTimeWithDaylightSavingFromString(utc)
    assertThat(result).isEqualTo(LocalDateTime.parse(expected))
  }

  @ParameterizedTest
  @CsvSource(
    "2022-06-02T14:20:27.123,2022-06-02T14:20:27.123Z",
    "2022-12-02T14:20:27.123,2022-12-02T14:20:27.123Z",
    " ,1970-01-01T00:00:00.000Z",
    ",1970-01-01T00:00:00.000Z",
  )
  fun `given UTC date time string when convertUtcDateTimeStringToIso8601Date called then return ISO8601 date time string`(
    utc: String?,
    expected: String,
  ) {
    val result = convertUtcDateTimeStringToIso8601Date(utc)
    assertThat(result).isEqualTo(expected)
  }
}
