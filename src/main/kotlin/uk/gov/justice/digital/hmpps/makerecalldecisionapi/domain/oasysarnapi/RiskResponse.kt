package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class RiskResponse(
  val riskToSelf: RiskToSelfResponse?,
  val otherRisks: OtherRisksResponse?,
  val summary: RiskSummaryRiskResponse?,
  val assessedOn: String?,
)

data class RiskToSelfResponse(
  val suicide: RiskVulnerabilityTypeResponse? = null,
  val selfHarm: RiskVulnerabilityTypeResponse? = null,
  val custody: RiskVulnerabilityTypeResponse? = null,
  val hostelSetting: RiskVulnerabilityTypeResponse? = null,
  val vulnerability: RiskVulnerabilityTypeResponse? = null,
)

data class RiskVulnerabilityTypeResponse(
  val risk: String? = null,
  val previous: String? = null,
  val previousConcernsText: String? = null,
  val current: String? = null,
  val currentConcernsText: String? = null,
)

data class OtherRisksResponse(
  val escapeOrAbscond: String? = null,
  val controlIssuesDisruptiveBehaviour: String? = null,
  val breachOfTrust: String? = null,
  val riskToOtherPrisoners: String? = null,
)

data class RiskSummaryRiskResponse(
  val whoIsAtRisk: String?,
  val natureOfRisk: String?,
  val riskImminence: String?,
  val riskIncreaseFactors: String?,
  val riskMitigationFactors: String?,
  val riskInCommunity: RiskScore?,
  val riskInCustody: RiskScore?,
  val overallRiskLevel: String?,
)
