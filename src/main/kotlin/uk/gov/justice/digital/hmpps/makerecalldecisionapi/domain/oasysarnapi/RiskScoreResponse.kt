package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

// TODO this is based on the JSON deserialisation example given in
//      https://dsdmoj.atlassian.net/wiki/spaces/ARN/pages/5962203966/OGRS4+ARNS+API+Change+Specification#Kotlin-%26-Jackson-Polymorphic-Parsing-Example
//      but it's unclear if the types are OK this way or if some of them are nullable or what. Our existing code
//      (corresponding to OutputV1, basically) had everything as nullable and handled things accordingly. It also
//      defined some values as doubles where the example uses Doubles. We need to hear back from the ARNS team on this

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
  val oneYear: Int?,
  val twoYears: Int?,
  val scoreLevel: FourLevelRiskScoreLevel?,
)

data class ViolencePredictorScore(
  val ovpStaticWeightedScore: Double?,
  val ovpDynamicWeightedScore: Double?,
  val ovpTotalWeightedScore: Double?,
  val oneYear: Int?,
  val twoYears: Int?,
  val ovpRisk: FourLevelRiskScoreLevel?,
)

data class GeneralPredictorScore(
  val ogpStaticWeightedScore: Double?,
  val ogpDynamicWeightedScore: Double?,
  val ogpTotalWeightedScore: Double?,
  val ogp1Year: Int?,
  val ogp2Year: Int?,
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
  val allReoffendingPredictor: StaticOrDynamicPredictor?,
  val violentReoffendingPredictor: StaticOrDynamicPredictor?,
  val seriousViolentReoffendingPredictor: StaticOrDynamicPredictor?,
  val directContactSexualReoffendingPredictor: Predictor?,
  val indirectImageContactSexualReoffendingPredictor: Predictor?,
  val combinedSeriousReoffendingPredictor: CombinedPredictor?,
)

data class Predictor(
  val score: Double?,
  val band: FourBandRiskScoreBand?,
)

data class StaticOrDynamicPredictor(
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
}

enum class ThreeLevelRiskScoreLevel {
  LOW,
  MEDIUM,
  HIGH,
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
