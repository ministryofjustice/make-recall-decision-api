package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation

data class LicenceConditionsCvlResponse(
  val userAccessResponse: UserAccess? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val licenceConditions: List<LicenceConditionResponse?>? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
