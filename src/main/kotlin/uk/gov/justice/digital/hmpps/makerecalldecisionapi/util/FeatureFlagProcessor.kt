package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
fun setFeatureFlags(featureFlags: String?): FeatureFlags {
  val mapper = jacksonObjectMapper()
  val flags: FeatureFlags = if (!featureFlags.isNullOrBlank()) {
    mapper.readValue(featureFlags)
  } else {
    FeatureFlags()
  }
  return flags
}
