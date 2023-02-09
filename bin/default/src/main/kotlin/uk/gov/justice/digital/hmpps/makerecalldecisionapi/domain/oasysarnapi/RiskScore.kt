package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import com.fasterxml.jackson.annotation.JsonProperty

data class RiskScore(
  @JsonProperty("VERY_HIGH")
  val veryHigh: List<String?>?,
  @JsonProperty("HIGH")
  val high: List<String?>?,
  @JsonProperty("MEDIUM")
  val medium: List<String?>?,
  @JsonProperty("LOW")
  val low: List<String?>?
)
