package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

internal data class RecommendationRequest(
  val recommendation: String?,
  val alternateActions: String?
)
