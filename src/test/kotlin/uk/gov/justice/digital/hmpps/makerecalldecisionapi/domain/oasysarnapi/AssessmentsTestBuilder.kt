package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime

fun assessmentsTimelineResponse(
  timeline: List<AssessmentsTimelineEntry> = emptyList(),
): AssessmentsTimelineResponse = AssessmentsTimelineResponse(timeline)

fun assessmentsTimelineEntry(
  status: String = listOf("COMPLETE", "OPEN").random(),
) = AssessmentsTimelineEntry(
  initiationDate = randomLocalDateTime().toString(),
  status = status,
)
