package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

data class PersonDetailsResponse(
  val personalDetailsOverview: PersonDetails?,
  val currentAddress: CurrentAddress?,
  val risk: Risk?,
  val offenderManager: OffenderManager?
)

data class CurrentAddress(
  val line1: String?,
  val line2: String?,
  val town: String?,
  val postcode: String?
)

data class OffenderManager(
  val name: String?,
  val phoneNumber: String?,
  val email: String?,
  val probationTeam: ProbationTeam?
)

data class Risk(
  val flags: List<String?>?
)
