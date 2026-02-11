package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.featureflag

import io.flipt.client.FliptClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FeatureFlagService(
  private val client: FliptClient?,
) {
  companion object {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun variant(key: String, context: Map<String, String>): String? = try {
    if (client == null) {
      log.error("Flipt client not configured, returning null")
      null
    } else {
      // we don't use the entity ID in our namespace on the Flipt server, but it cannot be null
      client
        .evaluateVariant(key, "entityId", context)
        .variantKey
    }
  } catch (e: Exception) {
    throw FeatureFlagException(key, e)
  }

  class FeatureFlagException(key: String, e: Exception) : RuntimeException("Unable to retrieve '$key' flag", e)
}
