package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

internal data class CreateRecommendationRequest(
  val crn: String?,
  val recallConsideredDetail: String? = null,
)
