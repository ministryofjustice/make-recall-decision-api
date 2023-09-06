package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonProperty

data class Risk(
  val flags: List<String?>?,
  val riskManagementPlan: RiskManagementPlan?,
  @JsonProperty("assessments")
  val assessmentInfo: AssessmentInfo? = null,
)

data class RiskManagementPlan(
  val assessmentStatusComplete: Boolean? = null,
  val lastUpdatedDate: String? = null,
  val latestDateCompleted: String? = null,
  val initiationDate: String? = null,
  val contingencyPlans: String? = null,
  val error: String? = null,
)
