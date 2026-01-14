package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomDouble
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

/**
 * Helper functions for generating instances of classes related to
 * risk score responses with their fields pre-filled with random
 * values. Intended for use in unit tests.
 */

fun assessmentScores(
  completedDate: String = randomLocalDateTime().toString(),
  status: AssessmentStatus = randomEnum<AssessmentStatus>(),
  version: String = listOf("1", "2").random(),
): AssessmentScores = if (version == "1") {
  assessmentScoresV1(
    completedDate = completedDate,
    status = status,
  )
} else {
  assessmentScoresV2(
    completedDate = completedDate,
    status = status,
  )
}

fun assessmentScoresV1(
  completedDate: String? = randomLocalDateTime().toString(),
  status: AssessmentStatus = randomEnum<AssessmentStatus>(),
  output: OutputV1 = outputV1(),
): AssessmentScoresV1 = AssessmentScoresV1(
  completedDate,
  status,
  "1",
  output,
)

fun outputV1(
  generalPredictorScore: GeneralPredictorScore = generalPredictorScore(),
  riskOfSeriousRecidivismScore: RiskOfSeriousRecidivismScore = riskOfSeriousRecidivismScore(),
  sexualPredictorScore: SexualPredictorScore = sexualPredictorScore(),
  groupReconvictionScore: GroupReconvictionScore = groupReconvictionScore(),
  violencePredictorScore: ViolencePredictorScore = violencePredictorScore(),
): OutputV1 = OutputV1(
  groupReconvictionScore,
  violencePredictorScore,
  generalPredictorScore,
  riskOfSeriousRecidivismScore,
  sexualPredictorScore,
)

fun groupReconvictionScore(
  oneYear: Int? = randomInt(),
  twoYears: Int? = randomInt(),
  scoreLevel: FourLevelRiskScoreLevel? = randomEnum<FourLevelRiskScoreLevel>(),
): GroupReconvictionScore = GroupReconvictionScore(
  oneYear,
  twoYears,
  scoreLevel,
)

fun violencePredictorScore(
  ovpStaticWeightedScore: Double = randomDouble(),
  ovpDynamicWeightedScore: Double = randomDouble(),
  ovpTotalWeightedScore: Double = randomDouble(),
  oneYear: Int? = randomInt(),
  twoYears: Int? = randomInt(),
  ovpRisk: FourLevelRiskScoreLevel? = randomEnum<FourLevelRiskScoreLevel>(),
): ViolencePredictorScore = ViolencePredictorScore(
  ovpStaticWeightedScore,
  ovpDynamicWeightedScore,
  ovpTotalWeightedScore,
  oneYear,
  twoYears,
  ovpRisk,
)

fun generalPredictorScore(
  ogpStaticWeightedScore: Double? = randomDouble(),
  ogpDynamicWeightedScore: Double? = randomDouble(),
  ogpTotalWeightedScore: Double? = randomDouble(),
  ogp1Year: Int? = randomInt(),
  ogp2Year: Int? = randomInt(),
  ogpRisk: FourLevelRiskScoreLevel? = randomEnum<FourLevelRiskScoreLevel>(),
): GeneralPredictorScore = GeneralPredictorScore(
  ogpStaticWeightedScore,
  ogpDynamicWeightedScore,
  ogpTotalWeightedScore,
  ogp1Year,
  ogp2Year,
  ogpRisk,
)

fun riskOfSeriousRecidivismScore(
  percentageScore: Double? = randomDouble(),
  scoreLevel: ThreeLevelRiskScoreLevel? = randomEnum<ThreeLevelRiskScoreLevel>(),
): RiskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(
  percentageScore,
  scoreLevel,
)

fun sexualPredictorScore(
  ospIndecentPercentageScore: Double? = randomDouble(),
  ospContactPercentageScore: Double? = randomDouble(),
  ospIndecentScoreLevel: ThreeLevelRiskScoreLevel? = randomEnum<ThreeLevelRiskScoreLevel>(),
  ospContactScoreLevel: FourLevelRiskScoreLevel? = randomEnum<FourLevelRiskScoreLevel>(),
  ospIndirectImagePercentageScore: Double? = randomDouble(),
  ospDirectContactPercentageScore: Double? = randomDouble(),
  ospIndirectImageScoreLevel: ThreeLevelRiskScoreLevel? = randomEnum<ThreeLevelRiskScoreLevel>(),
  ospDirectContactScoreLevel: FourLevelRiskScoreLevel? = randomEnum<FourLevelRiskScoreLevel>(),
): SexualPredictorScore = SexualPredictorScore(
  ospIndecentPercentageScore,
  ospContactPercentageScore,
  ospIndecentScoreLevel,
  ospContactScoreLevel,
  ospIndirectImagePercentageScore,
  ospDirectContactPercentageScore,
  ospIndirectImageScoreLevel,
  ospDirectContactScoreLevel,
)

fun assessmentScoresV2(
  completedDate: String = randomLocalDateTime().toString(),
  status: AssessmentStatus = randomEnum<AssessmentStatus>(),
  output: OutputV2 = outputV2(),
): AssessmentScoresV2 = AssessmentScoresV2(
  completedDate,
  status,
  "2",
  output,
)

fun outputV2(
  allReoffendingPredictor: StaticOrDynamicPredictor = staticOrDynamicPredictor(),
  violentReoffendingPredictor: StaticOrDynamicPredictor = staticOrDynamicPredictor(),
  seriousViolentReoffendingPredictor: StaticOrDynamicPredictor = staticOrDynamicPredictor(),
  directContactSexualReoffendingPredictor: StaticOrDynamicPredictor = staticOrDynamicPredictor(),
  indirectImageContactSexualReoffendingPredictor: StaticOrDynamicPredictor = staticOrDynamicPredictor(),
  combinedSeriousReoffendingPredictor: CombinedPredictor = combinedPredictor(),
): OutputV2 = OutputV2(
  allReoffendingPredictor,
  violentReoffendingPredictor,
  seriousViolentReoffendingPredictor,
  directContactSexualReoffendingPredictor,
  indirectImageContactSexualReoffendingPredictor,
  combinedSeriousReoffendingPredictor,
)

fun staticOrDynamicPredictor(
  score: Double = randomDouble(),
  band: FourBandRiskScoreBand = randomEnum<FourBandRiskScoreBand>(),
  staticOrDynamic: StaticOrDynamic = randomEnum<StaticOrDynamic>(),
): StaticOrDynamicPredictor = StaticOrDynamicPredictor(
  score,
  band,
  staticOrDynamic,
)

fun combinedPredictor(
  score: Double = randomDouble(),
  band: FourBandRiskScoreBand = randomEnum<FourBandRiskScoreBand>(),
  staticOrDynamic: StaticOrDynamic = randomEnum<StaticOrDynamic>(),
  algorithmVersion: String = randomString(),
): CombinedPredictor = CombinedPredictor(
  score,
  band,
  staticOrDynamic,
  algorithmVersion,
)
