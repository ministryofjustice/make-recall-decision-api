package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class AssessmentInfo(
  val lastUpdatedDate: String?,
  val offenceDataFromLatestCompleteAssessment: Boolean,
  val offenceCodesMatch: Boolean,
  val offenceDescription: String?
)
