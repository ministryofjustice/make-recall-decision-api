package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Release
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation

data class CaseSummaryOverviewResponse(
  val userAccessResponse: UserAccess? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val activeConvictions: List<Conviction> = emptyList(),
  val lastRelease: Release? = null,
  val risk: Risk? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
