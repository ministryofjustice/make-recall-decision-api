package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse

data class ContactHistoryResponse(
  val personalDetailsOverview: PersonDetails? = null,
  val contactSummary: List<ContactSummaryResponse>? = null,
  val contactTypeGroups: List<ContactGroupResponse?>? = null,
  val releaseSummary: ReleaseSummaryResponse? = null,
)
