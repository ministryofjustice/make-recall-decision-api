package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class AssessmentsTimelineResponse(
  val timeline: List<AssessmentsTimelineEntry>?,
)

data class AssessmentsTimelineEntry(
  val initiationDate: String? = null,
  val status: String? = null,
)
