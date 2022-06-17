package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

data class CurrentScoreResponse(
  val completedDate: String?,
  val generalPredictorScore: GeneralPredictorScore?,
  val riskOfSeriousRecidivismScore: RiskOfSeriousRecidivismScore?,
  val sexualPredictorScore: SexualPredictorScore?
)

data class SexualPredictorScore(
  val ospIndecentPercentageScore: String?,
  val ospContactPercentageScore: String?,
  val ospIndecentScoreLevel: String?,
  val ospContactScoreLevel: String?
)

data class RiskOfSeriousRecidivismScore(
  val percentageScore: String?,
  val staticOrDynamic: String?,
  val source: String?,
  val algorithmVersion: String?,
  val scoreLevel: String?
)

data class GeneralPredictorScore(
  val ogpStaticWeightedScore: String?,
  val ogpDynamicWeightedScore: String?,
  val ogpTotalWeightedScore: String?,
  val ogp1Year: String?,
  val ogp2Year: String?,
  val ogpRisk: String?
)

data class ViolencePredictorScore(
  val ovpStaticWeightedScore: String?,
  val ovpDynamicWeightedScore: String?,
  val ovpTotalWeightedScore: String?,
  val oneYear: String?,
  val twoYears: String?,
  val ovpRisk: String?
)

data class GroupReconvictionScore(
  val oneYear: String?,
  val twoYears: String?,
  val scoreLevel: String?
)
