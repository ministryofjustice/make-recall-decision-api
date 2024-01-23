package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class DeleteRecommendationResponse(
  val notes: String?,
  val sensitive: Boolean? = false,
)
