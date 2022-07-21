package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class PersonDetailsResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonDetails? = null,
  val currentAddress: CurrentAddress? = null,
  val offenderManager: OffenderManager? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)
