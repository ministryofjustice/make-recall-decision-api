package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "outputVersion",
  visible = true,
)
@JsonSubTypes(
  JsonSubTypes.Type(value = AssessmentScoresV1::class, name = "1"),
  JsonSubTypes.Type(value = AssessmentScoresV2::class, name = "2"),
)
sealed class AssessmentScores {
  abstract val completedDate: String?
  abstract val status: AssessmentStatus?
  abstract val outputVersion: String?
}

data class AssessmentScoresV1(
  override val completedDate: String?,
  override val status: AssessmentStatus?,
  override val outputVersion: String?,
  val output: OutputV1?,
) : AssessmentScores()

data class AssessmentScoresV2(
  override val completedDate: String?,
  override val status: AssessmentStatus?,
  override val outputVersion: String?,
  val output: OutputV2?,
) : AssessmentScores()

data class OutputV1(
  val groupReconvictionScore: GroupReconvictionScore?,
  val violencePredictorScore: ViolencePredictorScore?,
  val generalPredictorScore: GeneralPredictorScore?,
  val riskOfSeriousRecidivismScore: RiskOfSeriousRecidivismScore?,
  val sexualPredictorScore: SexualPredictorScore?,
)

data class GroupReconvictionScore(
  val oneYear: Double?,
  val twoYears: Double?,
  val scoreLevel: FourLevelRiskScoreLevel?,
)

data class ViolencePredictorScore(
  val ovpStaticWeightedScore: Double?,
  val ovpDynamicWeightedScore: Double?,
  val ovpTotalWeightedScore: Double?,
  val oneYear: Double?,
  val twoYears: Double?,
  val ovpRisk: FourLevelRiskScoreLevel?,
)

data class GeneralPredictorScore(
  val ogpStaticWeightedScore: Double?,
  val ogpDynamicWeightedScore: Double?,
  val ogpTotalWeightedScore: Double?,
  val ogp1Year: Double?,
  val ogp2Year: Double?,
  val ogpRisk: FourLevelRiskScoreLevel?,
)

data class RiskOfSeriousRecidivismScore(
  val percentageScore: Double?,
  // The 3 fields commented out below weren't there before these changes. Probably
  // OK to leave them out, since we don't seem to be using them anywhere (should we?)
//  val staticOrDynamic: StaticOrDynamic,
//  val source: String,
//  val algorithmVersion: String,
  val scoreLevel: ThreeLevelRiskScoreLevel?,
)

data class SexualPredictorScore(
  val ospIndecentPercentageScore: Double?,
  val ospContactPercentageScore: Double?,
  val ospIndecentScoreLevel: ThreeLevelRiskScoreLevel?,
  val ospContactScoreLevel: FourLevelRiskScoreLevel?,
  val ospIndirectImagePercentageScore: Double?,
  val ospDirectContactPercentageScore: Double?,
  val ospIndirectImageScoreLevel: ThreeLevelRiskScoreLevel?,
  val ospDirectContactScoreLevel: FourLevelRiskScoreLevel?,
)

data class OutputV2(
  val allReoffendingPredictor: FourBandStaticOrDynamicPredictor?,
  val violentReoffendingPredictor: FourBandStaticOrDynamicPredictor?,
  val seriousViolentReoffendingPredictor: FourBandStaticOrDynamicPredictor?,
  val directContactSexualReoffendingPredictor: FourBandStaticOrDynamicPredictor?,
  val indirectImageContactSexualReoffendingPredictor: ThreeBandStaticOrDynamicPredictor?,
  val combinedSeriousReoffendingPredictor: CombinedPredictor?,
)

data class ThreeBandStaticOrDynamicPredictor(
  val score: Double?,
  val band: ThreeBandRiskScoreBand?,
  val staticOrDynamic: StaticOrDynamic?,
)

data class FourBandStaticOrDynamicPredictor(
  val score: Double?,
  val band: FourBandRiskScoreBand?,
  val staticOrDynamic: StaticOrDynamic?,
)

data class CombinedPredictor(
  val score: Double?,
  val band: FourBandRiskScoreBand?,
  val staticOrDynamic: StaticOrDynamic?,
  val algorithmVersion: String?,
)

enum class StaticOrDynamic {
  STATIC,
  DYNAMIC,
}

enum class AssessmentStatus {
  COMPLETE,
  LOCKED_INCOMPLETE,
}

enum class FourLevelRiskScoreLevel {
  LOW,
  MEDIUM,
  HIGH,
  VERY_HIGH,
  NOT_APPLICABLE,
}

enum class ThreeLevelRiskScoreLevel {
  LOW,
  MEDIUM,
  HIGH,
  NOT_APPLICABLE,
}

enum class FourBandRiskScoreBand {
  LOW,
  MEDIUM,
  HIGH,
  VERY_HIGH,
}

enum class ThreeBandRiskScoreBand {
  LOW,
  MEDIUM,
  HIGH,
}
