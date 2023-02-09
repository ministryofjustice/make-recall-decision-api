package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class ContactHistoryResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonDetails? = null,
  val contactSummary: List<ContactSummaryResponse>? = null,
  val contactTypeGroups: List<ContactGroupResponse?>? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
