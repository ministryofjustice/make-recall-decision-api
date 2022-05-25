package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class OffenderManager(
  val active: Boolean?,
  val trustOfficer: TrustOfficer?,
  val staff: Staff?,
  val providerEmployee: ProviderEmployee?,
  val team: Team?
)
