package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.featureflag

import ch.qos.logback.classic.Level
import io.flipt.client.FliptClient
import io.flipt.client.FliptException
import io.flipt.client.models.VariantEvaluationResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.featureflag.FeatureFlagService.FeatureFlagException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
class FeatureFlagServiceTest {

  @InjectMocks
  private lateinit var featureFlagService: FeatureFlagService

  @Mock
  private lateinit var fliptClient: FliptClient

  private val logAppender = findLogAppender(FeatureFlagService::class.java)

  @Test
  fun `returns expected variant key if variant feature flag is set up`() {
    val flag = randomString()
    val context = mapOf("key" to "value")
    val variantKey = randomString()
    withVariantFlag(flag, variantKey, context)
    assertThat(featureFlagService.variant(flag, context)).isEqualTo(variantKey)
  }

  @Test
  fun `throws error if variant feature flag is not defined`() {
    val flag = randomString()
    val context = mapOf("key" to "value")
    whenever(fliptClient.evaluateVariant(flag, "entityId", context)).thenThrow(
      FliptException.EvaluationException("Not Found"),
    )
    assertThrows<FeatureFlagException> { featureFlagService.variant(flag, context) }
  }

  @Test
  fun `variant retrieval logs warning and returns null if flipt client is not configured`() {
    // Simulate null Flipt client
    val featureFlagServiceWithNullClient = FeatureFlagService(null)

    val variantKey = featureFlagServiceWithNullClient.variant("any-flag", emptyMap())

    assertThat(variantKey).isNull()
    assertWarningMessageWasLogged()
  }

  private fun withVariantFlag(key: String, variantKey: String, context: Map<String, String>) {
    whenever(fliptClient.evaluateVariant(key, "entityId", context)).thenReturn(variantFlagResponse(key, variantKey))
  }

  private fun variantFlagResponse(key: String, variantKey: String) = VariantEvaluationResponse
    .builder()
    .variantKey(variantKey)
    .flagKey(key)
    .reason("DEFAULT_EVALUATION_REASON")
    .requestDurationMillis(100F)
    .timestamp(LocalTime.now().toString())
    .build()

  private fun assertWarningMessageWasLogged() {
    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.ERROR)
        assertThat(message).isEqualTo("Flipt client not configured, returning null")
      }
    }
  }
}
