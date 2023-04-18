package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonalDetailsOverview

data class RecommendationsResponse(
  val userAccessResponse: UserAccess? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val recommendations: List<RecommendationsListItem>? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
