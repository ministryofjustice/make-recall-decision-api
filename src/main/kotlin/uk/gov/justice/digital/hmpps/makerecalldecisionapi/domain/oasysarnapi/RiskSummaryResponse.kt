package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class RiskSummaryResponse(
  val whoIsAtRisk: String?,
  val natureOfRisk: String?,
  val riskImminence: String?,
  val riskIncreaseFactors: String?,
  val riskMitigationFactors: String?,
  val riskInCommunity: RiskInCommunity?,
  val riskInCustody: RiskInCustody?,
  val assessedOn: LocalDateTime?,
  val overallRiskLevel: String?
)

data class RiskInCommunity(
  @JsonProperty("VERY_HIGH")
  val veryHigh: List<String?>?,
  @JsonProperty("HIGH")
  val high: List<String?>?,
  @JsonProperty("MEDIUM")
  val medium: List<String?>?,
  @JsonProperty("LOW")
  val low: List<String?>?
)

data class RiskInCustody(
  @JsonProperty("VERY_HIGH")
  val veryHigh: List<String?>?,
  @JsonProperty("HIGH")
  val high: List<String?>?,
  @JsonProperty("MEDIUM")
  val medium: List<String?>?,
  @JsonProperty("LOW")
  val low: List<String?>?
)
