package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ReleaseSummaryResponse

data class LicenceHistoryResponse(
  val personalDetailsOverview: PersonDetails? = null,
  val contactSummary: List<ContactSummaryResponse>? = null,
  val releaseSummary: ReleaseSummaryResponse? = null,
)
