package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class ContingencyPlanResponse(
  val assessments: List<Assessment?>?
)

data class Assessment(
  val dateCompleted: String?,
  val assessmentStatus: String?,
  val keyConsiderationsCurrentSituation: String?,
  val furtherConsiderationsCurrentSituation: String?,
  val supervision: String?,
  val monitoringAndControl: String?,
  val interventionsAndTreatment: String?,
  val victimSafetyPlanning: String?,
  val contingencyPlans: String?
)
