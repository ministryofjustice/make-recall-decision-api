package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class CaseSummaryOverviewResponse(
  val personalDetailsOverview: PersonDetails?,
  val offences: List<Offence>?,
  val risk: Risk?,
)
