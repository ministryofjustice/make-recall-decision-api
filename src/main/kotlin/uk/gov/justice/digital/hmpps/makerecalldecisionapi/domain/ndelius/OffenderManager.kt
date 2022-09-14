package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class OffenderManager(
  val active: Boolean?,
  val probationArea: ProbationArea?,
  val trustOfficer: TrustOfficer?,
  val staff: Staff?,
  val providerEmployee: ProviderEmployee?,
  val team: Team?
)
