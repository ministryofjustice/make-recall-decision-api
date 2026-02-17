package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.config

import io.flipt.client.FliptClient
import io.flipt.client.models.VariantEvaluationResponse
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.anyMap
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.PartATemplateVersion
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.featureflag.FeatureFlag

@TestConfiguration
class TestConfig {
  // We don't know and have no control over the exact calls the FliptClient will make to retrieve
  // the flag values, so instead of trying to wiremock the responses we mock the FliptClient itself
  @Bean
  @Primary
  fun fliptApiClientOverride(): FliptClient = mock(FliptClient::class.java)

  companion object {
    fun mockPartATemplateVersionFlag(fliptApiClient: FliptClient, flagVariantKey: String? = null) {
      val variantResponse = mock(VariantEvaluationResponse::class.java)
      whenever(variantResponse.variantKey).thenReturn(
        flagVariantKey ?: PartATemplateVersion.entries.last().flagVariantKey,
      )
      whenever(
        fliptApiClient.evaluateVariant(
          eq(FeatureFlag.PART_A_TEMPLATE_VERSION.flagId),
          eq("entityId"),
          anyMap(),
        ),
      ).thenReturn(variantResponse)
    }
  }
}
