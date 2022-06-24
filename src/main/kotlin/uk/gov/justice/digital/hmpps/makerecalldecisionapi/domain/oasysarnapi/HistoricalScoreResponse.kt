package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class HistoricalScoreResponse(
  val rsrPercentageScore: String?,
  val rsrScoreLevel: String?,
  val ospcPercentageScore: String?,
  val ospcScoreLevel: String?,
  val ospiPercentageScore: String?,
  val ospiScoreLevel: String?,
  val calculatedDate: String?
)
