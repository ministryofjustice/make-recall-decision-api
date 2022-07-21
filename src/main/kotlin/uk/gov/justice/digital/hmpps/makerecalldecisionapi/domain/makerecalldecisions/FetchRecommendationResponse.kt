package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class FetchRecommendationResponse(
  val id: Long? = null,
  val status: String? = null,
  val crn: String? = null,
  val recallType: String? = null,
  val activeRecommendation: ActiveRecommendation? = null
)
