package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime

fun assessmentsTimelineResponse(
  timeline: List<AssessmentsTimelineEntry> = emptyList(),
): AssessmentsTimelineResponse = AssessmentsTimelineResponse(timeline)

fun assessmentsTimelineEntry(
  status: AssessmentsTimelineEntryStatus = randomEnum<AssessmentsTimelineEntryStatus>(),
) = AssessmentsTimelineEntry(
  initiationDate = randomLocalDateTime().toString(),
  status = status,
)
