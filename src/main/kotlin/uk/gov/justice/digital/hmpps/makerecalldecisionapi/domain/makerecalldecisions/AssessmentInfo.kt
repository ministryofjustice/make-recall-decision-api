package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class AssessmentInfo(
  val error: String? = null,
  val lastUpdatedDate: String? = null,
  val offenceDataFromLatestCompleteAssessment: Boolean? = null,
  val offencesMatch: Boolean? = null,
  val offenceDescription: String? = null
)
