package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class LicenceConditionsResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val convictions: List<ConvictionResponse>? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
