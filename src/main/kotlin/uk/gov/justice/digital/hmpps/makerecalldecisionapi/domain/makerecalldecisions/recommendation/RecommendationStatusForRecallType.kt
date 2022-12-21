package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

enum class RecommendationStatusForRecallType {
  CONSIDERING_RECALL, RECOMMENDATION_STARTED, MAKING_DECISION_TO_RECALL, MAKING_DECISION_NOT_TO_RECALL, DECIDED_TO_RECALL, DECIDED_NOT_TO_RECALL, UNKNOWN
}
