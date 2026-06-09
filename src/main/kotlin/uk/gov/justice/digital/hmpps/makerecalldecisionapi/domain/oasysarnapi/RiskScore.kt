package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import com.fasterxml.jackson.annotation.JsonProperty

data class RiskScore(
  @param:JsonProperty("VERY_HIGH")
  val veryHigh: List<String?>?,
  @param:JsonProperty("HIGH")
  val high: List<String?>?,
  @param:JsonProperty("MEDIUM")
  val medium: List<String?>?,
  @param:JsonProperty("LOW")
  val low: List<String?>?,
)
