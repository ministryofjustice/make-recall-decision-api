package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendationstatus

// Taken from an enum defined in UI (in recommendationStatusCheck.ts at the time of writing)
enum class RecommendationStatus {
  SPO_CONSIDER_RECALL,
  SPO_RECORDED_RATIONALE,
  PO_RECALL_CONSULT_SPO,
  SPO_SIGNATURE_REQUESTED,
  SPO_SIGNED,
  ACO_SIGNATURE_REQUESTED,
  ACO_SIGNED,
  RECALL_DECIDED,
  NO_RECALL_DECIDED,
  DELETED,
  PP_DOCUMENT_CREATED,
  SENT_TO_PPCS,
  BOOKED_TO_PPUD,
  BOOKING_ON_STARTED,
  REC_CLOSED,
  PO_START_RECALL,
  REC_DELETED,
  AP_RECORDED_RATIONALE,
  AP_COLLECTED_RATIONALE,
}
