package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class AssessmentsTimelineResponse(
  val timeline: List<AssessmentTimelineEntry>?,
)

data class AssessmentTimelineEntry(
  val assessmentId: Long? = null,
  val initiationDate: String? = null,
  val completedDate: String? = null,
  val assessmentType: String? = null,
  val status: String? = null,
)
