package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class OffenderManager(
  val name: String?,
  val phoneNumber: String?,
  val email: String?,
  val probationTeam: ProbationTeam?
)
