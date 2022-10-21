package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class LicenceConditionsCvlResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonDetails? = null,
  val licenceConditions: List<LicenceConditionResponse?>? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
