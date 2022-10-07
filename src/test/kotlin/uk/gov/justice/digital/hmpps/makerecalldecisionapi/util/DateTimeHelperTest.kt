package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertUtcDateTimeStringToIso8601Date

class DateTimeHelperTest {

  @Test
  fun `given UTC date time string then convert to ISO8601 date`() {
    val result = convertUtcDateTimeStringToIso8601Date("2022-10-02T14:20:27")
    assertThat(result).isEqualTo("2022-10-02T14:20:27.000Z")
  }

  @Test
  fun `given empty string then return null`() {
    val result = convertUtcDateTimeStringToIso8601Date(null)
    assertThat(result).isEqualTo(null)
  }
}
