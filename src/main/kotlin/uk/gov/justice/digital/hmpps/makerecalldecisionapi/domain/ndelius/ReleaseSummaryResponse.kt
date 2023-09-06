package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

data class ReleaseSummaryResponse(
  val lastRelease: LastRelease?,
  val lastRecall: LastRecall?,
)
