package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class RecommendationsTabResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonDetails? = null,
  val recommendations: List<RecommendationTab>? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
