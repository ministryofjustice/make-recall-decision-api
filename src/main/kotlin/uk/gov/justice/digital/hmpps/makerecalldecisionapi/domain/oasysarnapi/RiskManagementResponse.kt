package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class RiskManagementResponse(
  val crn: String,
  val limitedAccessOffender: Boolean?,
  val riskManagementPlan: List<RiskManagementPlanResponse>?,
)

data class RiskManagementPlanResponse(
  val assessmentId: Long? = null,
  val dateCompleted: String? = null,
  val partcompStatus: String? = null,
  val initiationDate: String? = null,
  val assessmentStatus: String? = null,
  val assessmentType: String? = null,
  val superStatus: String? = null,
  val keyInformationCurrentSituation: String? = null,
  val furtherConsiderationsCurrentSituation: String? = null,
  val supervision: String? = null,
  val monitoringAndControl: String? = null,
  val interventionsAndTreatment: String? = null,
  val victimSafetyPlanning: String? = null,
  val contingencyPlans: String? = null,
  val laterWIPAssessmentExists: Boolean? = null,
  val latestWIPDate: String? = null,
  val laterSignLockAssessmentExists: Boolean? = null,
  val latestSignLockDate: String? = null,
  val laterPartCompUnsignedAssessmentExists: Boolean? = null,
  val latestPartCompUnsignedDate: String? = null,
  val laterPartCompSignedAssessmentExists: Boolean? = null,
  val latestPartCompSignedDate: String? = null,
  val laterCompleteAssessmentExists: Boolean? = null,
  val latestCompleteDate: String? = null,
)
