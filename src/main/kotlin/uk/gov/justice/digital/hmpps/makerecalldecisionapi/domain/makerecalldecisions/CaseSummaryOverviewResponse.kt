package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Release
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class CaseSummaryOverviewResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val activeConvictions: List<Conviction> = emptyList(),
  val lastRelease: Release? = null,
  val risk: Risk? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
