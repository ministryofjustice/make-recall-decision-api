package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertUtcDateTimeStringToIso8601Date
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.dateTimeWithDaylightSavingFromString
import java.time.LocalDateTime
import java.util.stream.Stream

class DateTimeHelperTest {

  companion object {
    @JvmStatic
    fun utcToLondonTestData(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(LocalDateTime.parse("2024-01-31T08:00:00"), LocalDateTime.parse("2024-01-31T08:00:00")),
        Arguments.of(
          LocalDateTime.of(2024, 1, 1, 0, 0, 0),
          LocalDateTime.of(2024, 1, 1, 0, 0, 0),
        ),
        Arguments.of(LocalDateTime.parse("2024-07-31T08:00:00"), LocalDateTime.parse("2024-07-31T09:00:00")),
        Arguments.of(
          LocalDateTime.of(2024, 5, 31, 23, 0, 0),
          LocalDateTime.of(2024, 6, 1, 0, 0, 0),
        ),
      )
    }
  }

  @ParameterizedTest
  @MethodSource("utcToLondonTestData")
  fun `given local date time when convertToLondonTimezone called then assume time is UTC and return date time in London timezone`(
    original: LocalDateTime,
    expected: LocalDateTime,
  ) {
    val result = DateTimeHelper.convertToLondonTimezone(original)

    assertThat(result).isEqualTo(expected)
  }

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
