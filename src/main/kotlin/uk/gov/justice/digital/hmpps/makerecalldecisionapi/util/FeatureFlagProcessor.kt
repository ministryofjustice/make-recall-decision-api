package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
fun setFeatureFlags(featureFlags: String?): FeatureFlags? {
  val mapper = jacksonObjectMapper()
  val flags: FeatureFlags? = if (featureFlags != null) {
    mapper.readValue(featureFlags)
  } else {
    null
  }
  return flags
}
