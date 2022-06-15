package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class HistoricalScoresResponse(
  val historicalScores: List<HistoricalScore>
)

data class HistoricalScore(
  val rsrPercentageScore: String,
  val rsrScoreLevel: String,
  val ospcPercentageScore: String,
  val ospcScoreLevel: String,
  val ospiPercentageScore: String,
  val ospiScoreLevel: String,
  val calculatedDate: String,
  val completedDate: String,
  val signedDate: String
)
