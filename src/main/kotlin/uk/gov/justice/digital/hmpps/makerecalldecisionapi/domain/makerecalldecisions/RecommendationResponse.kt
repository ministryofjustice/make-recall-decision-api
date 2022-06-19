package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class RecommendationResponse(
  val recommendation: String?,
  val alternateActions: String?
)
