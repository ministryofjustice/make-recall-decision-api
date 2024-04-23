package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import java.util.stream.Stream

class FeatureFlagProcessorTest {

  @Test
  fun `given valid flag when setFeatureFlags is invoked then FeatureFlags is returned with flag set`() {
    val flags = setFeatureFlags("{ \"flagSendDomainEvent\": true }")
    assertThat(flags).isEqualTo(FeatureFlags(flagSendDomainEvent = true))
  }

  @Test
  fun `given unrecognised flag when setFeatureFlags is invoked then unrecognised value is ignored`() {
    val flags = setFeatureFlags("{ \"flagSendDomainEvent\": true, \"flagUnrecognised\": true }")
    assertThat(flags).isEqualTo(FeatureFlags(flagSendDomainEvent = true))
  }

  @ParameterizedTest
  @MethodSource("nullOrEmptyFlagsString")
  fun `given null or empty feature flags string when setFeatureFlags is invoked then default FeatureFlags is returned`(
    value: String?,
  ) {
    val flags = setFeatureFlags(value)
    assertThat(flags).isEqualTo(FeatureFlags())
  }

  @Test
  fun `given empty json when setFeatureFlags is invoked then default FeatureFlags is returned`() {
    val flags = setFeatureFlags("{}")
    assertThat(flags).isEqualTo(FeatureFlags())
  }

  @Test
  fun `given invalid json when setFeatureFlags is invoked then exception is thrown`() {
    assertThrows<Exception> {
      setFeatureFlags("{ invalid }")
    }
  }

  companion object {
    @JvmStatic
    fun nullOrEmptyFlagsString(): Stream<String?> {
      return Stream.of(
        null,
        "",
        " ",
        "\t",
        "\r\n",
      )
    }
  }
}
