package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class RecommendationRequest(
  val recommendation: String?,
  val alternateActions: String?
)
