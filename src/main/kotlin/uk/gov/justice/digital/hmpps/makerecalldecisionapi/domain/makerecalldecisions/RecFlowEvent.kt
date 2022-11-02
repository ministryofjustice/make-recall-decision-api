package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecFlowEventModel

data class RecFlowEvent(
  val id: String? = null,
  val crn: String?,
  val userId: String?,
  val timeStamp: String?,
  val eventType: EventType?
)
fun RecFlowEvent.toRecFlowEventModel(): RecFlowEventModel {
  return RecFlowEventModel(
    crn = crn,
    userId = userId,
    timeStamp = timeStamp,
    eventType = eventType
  )
}

enum class EventType() {
  SEARCH_RESULT_CLICKED,
  MAKE_RECOMMENDATION_CLICKED,
  UPDATE_RECOMMENDATION_CLICKED,
  DOWNLOAD_PART_A_CLICKED
}
