package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime

/**
 * Helper functions for generating instances of classes related to
 * risk score responses with their fields pre-filled with random
 * values. Intended for use in unit tests.
 */

fun riskScoreResponse(
  completedDate: String? = randomLocalDateTime().toString(),
  generalPredictorScore: GeneralPredictorScore? = generalPredictorScore(),
  riskOfSeriousRecidivismScore: RiskOfSeriousRecidivismScore? = riskOfSeriousRecidivismScore(),
  sexualPredictorScore: SexualPredictorScore? = sexualPredictorScore(),
  groupReconvictionScore: GroupReconvictionScore? = groupReconvictionScore(),
  violencePredictorScore: ViolencePredictorScore? = violencePredictorScore(),
): RiskScoreResponse {
  return RiskScoreResponse(
    completedDate,
    generalPredictorScore,
    riskOfSeriousRecidivismScore,
    sexualPredictorScore,
    groupReconvictionScore,
    violencePredictorScore,
  )
}

fun generalPredictorScore(
  ogpStaticWeightedScore: String? = randomInt().toString(),
  ogpDynamicWeightedScore: String? = randomInt().toString(),
  ogpTotalWeightedScore: String? = randomInt().toString(),
  ogpRisk: String? = randomScoreLevel(),
  ogp1Year: String? = randomInt().toString(),
  ogp2Year: String? = randomInt().toString(),
): GeneralPredictorScore {
  return GeneralPredictorScore(
    ogpStaticWeightedScore,
    ogpDynamicWeightedScore,
    ogpTotalWeightedScore,
    ogpRisk,
    ogp1Year,
    ogp2Year,
  )
}


fun riskOfSeriousRecidivismScore(
  percentageScore: String? = randomInt().toString(),
  scoreLevel: String? = randomScoreLevel(),
): RiskOfSeriousRecidivismScore {
  return RiskOfSeriousRecidivismScore(
    percentageScore,
    scoreLevel,
  )
}

fun sexualPredictorScore(
  ospIndecentPercentageScore: String? = randomInt().toString(),
  ospContactPercentageScore: String? = randomInt().toString(),
  ospIndecentScoreLevel: String? = randomScoreLevel(),
  ospContactScoreLevel: String? = randomScoreLevel(),
): SexualPredictorScore {
  return SexualPredictorScore(
    ospIndecentPercentageScore,
    ospContactPercentageScore,
    ospIndecentScoreLevel,
    ospContactScoreLevel,
  )
}


fun groupReconvictionScore(
  oneYear: String? = randomInt().toString(),
  twoYears: String? = randomInt().toString(),
  scoreLevel: String? = randomScoreLevel(),
): GroupReconvictionScore {
  return GroupReconvictionScore(
    oneYear,
    twoYears,
    scoreLevel,
  )
}


fun violencePredictorScore(
  ovpStaticWeightedScore: String? = randomInt().toString(),
  ovpDynamicWeightedScore: String? = randomInt().toString(),
  ovpTotalWeightedScore: String? = randomInt().toString(),
  oneYear: String? = randomScoreLevel(),
  twoYears: String? = randomInt().toString(),
  ovpRisk: String? = randomInt().toString(),
): ViolencePredictorScore {
  return ViolencePredictorScore(
    ovpStaticWeightedScore,
    ovpDynamicWeightedScore,
    ovpTotalWeightedScore,
    oneYear,
    twoYears,
    ovpRisk,
  )
}

private fun randomScoreLevel(): String {
  return arrayOf("LOW", "MEDIUM", "HIGH").random()
}
