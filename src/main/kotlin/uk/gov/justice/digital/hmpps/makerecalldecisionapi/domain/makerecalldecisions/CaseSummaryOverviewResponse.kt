package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class CaseSummaryOverviewResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonDetails? = null,
  val convictions: List<OverviewConvictionResponse>? = null,
  val releaseSummary: ReleaseSummaryResponse? = null,
  val risk: Risk? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
