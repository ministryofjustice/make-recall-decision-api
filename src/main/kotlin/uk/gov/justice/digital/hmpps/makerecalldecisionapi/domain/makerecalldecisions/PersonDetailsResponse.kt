package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class PersonDetailsResponse(
  val personalDetailsOverview: PersonDetails?,
  val currentAddress: CurrentAddress?,
  val offenderManager: OffenderManager?
)
