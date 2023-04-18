package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation

data class ContactHistoryResponse(
  val userAccessResponse: UserAccess? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val contactSummary: List<ContactSummaryResponse>? = null,
  val contactTypeGroups: List<ContactGroupResponse?>? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
