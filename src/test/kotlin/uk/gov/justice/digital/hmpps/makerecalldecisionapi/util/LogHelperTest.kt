package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.LogHelper.Helper.redact

class LogHelperTest {

  @Test
  fun `given null value when redact is invoked then null as a string is returned`() {
    val redacted = redact(null)
    assertThat(redacted).isEqualTo("null")
  }

  @ParameterizedTest
  @CsvSource(
    "a, a",
    "B, B",
    "ba, b*",
    "Ba, B*",
    "asd, a**",
    "TextToRedact, T***********"
  )
  fun `given string value when redact is invoked then first letter followed by asterisks is returned`(
    value: String,
    expected: String
  ) {
    val redacted = redact(value)
    assertThat(redacted).isEqualTo(expected)
  }
}
