package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class AssessmentsResponse(
  val crn: String?,
  val limitedAccessOffender: Boolean?,
  val assessments: List<Assessment>?
)

data class Assessment(
  val offenceDetails: List<AssessmentOffenceDetail> = emptyList(),
  val assessmentStatus: String?,
  val superStatus: String?,
  val dateCompleted: String?,
  val laterWIPAssessmentExists: Boolean?,
  val laterSignLockAssessmentExists: Boolean?,
  val laterPartCompUnsignedAssessmentExists: Boolean?,
  val laterPartCompSignedAssessmentExists: Boolean?,
  val laterCompleteAssessmentExists: Boolean?,
  val offence: String?,
  val keyConsiderationsCurrentSituation: String?,
  val furtherConsiderationsCurrentSituation: String?,
  val supervision: String?,
  val monitoringAndControl: String?,
  val interventionsAndTreatment: String?,
  val victimSafetyPlanning: String?,
  val contingencyPlans: String?
)

data class AssessmentOffenceDetail(
  val type: String?,
  val offenceCode: String?,
  val offenceSubCode: String?,
  val offenceDate: String?
)
