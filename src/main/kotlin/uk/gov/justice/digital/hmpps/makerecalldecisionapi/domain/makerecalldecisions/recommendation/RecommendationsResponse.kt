package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonalDetailsOverview
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class RecommendationsResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val recommendations: List<RecommendationsListItem>? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
