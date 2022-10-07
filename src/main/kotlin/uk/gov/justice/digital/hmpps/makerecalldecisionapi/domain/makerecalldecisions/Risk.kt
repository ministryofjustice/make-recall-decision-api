package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class Risk(
  val flags: List<String?>?,
  val riskManagementPlan: RiskManagementPlan?
)

data class RiskManagementPlan(
  val assessmentStatusComplete: Boolean? = null,
  val lastUpdatedDate: String? = null,
  val contingencyPlans: String? = null,
  val error: String? = null
)
