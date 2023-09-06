package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class RiskSummaryResponse(
  val whoIsAtRisk: String?,
  val natureOfRisk: String?,
  val riskImminence: String?,
  val riskIncreaseFactors: String?,
  val riskMitigationFactors: String?,
  val riskInCommunity: RiskScore?,
  val riskInCustody: RiskScore?,
  val assessedOn: String?,
  val overallRiskLevel: String?,
)
