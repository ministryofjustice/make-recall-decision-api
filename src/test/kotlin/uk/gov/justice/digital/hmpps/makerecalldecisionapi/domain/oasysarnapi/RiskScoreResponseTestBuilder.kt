package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomDouble
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum
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
  ogpStaticWeightedScore: String? = randomDouble().toString(),
  ogpDynamicWeightedScore: String? = randomDouble().toString(),
  ogpTotalWeightedScore: String? = randomDouble().toString(),
  ogpRisk: String? = randomEnum<OgpScoreLevel>().toString(),
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
  percentageScore: String? = randomDouble().toString(),
  scoreLevel: String? = randomEnum<RsrScoreLevel>().toString(),
): RiskOfSeriousRecidivismScore {
  return RiskOfSeriousRecidivismScore(
    percentageScore,
    scoreLevel,
  )
}

fun sexualPredictorScore(
  ospIndecentPercentageScore: String? = randomDouble().toString(),
  ospContactPercentageScore: String? = randomDouble().toString(),
  ospIndecentScoreLevel: String? = randomEnum<OspiScoreLevel>().toString(),
  ospContactScoreLevel: String? = randomEnum<OspcScoreLevel>().toString(),
  ospIndirectImagePercentageScore: String? = randomDouble().toString(),
  ospDirectContactPercentageScore: String? = randomDouble().toString(),
  ospIndirectImageScoreLevel: String? = randomEnum<OspiicScoreLevel>().toString(),
  ospDirectContactScoreLevel: String? = randomEnum<OspdcScoreLevel>().toString(),
): SexualPredictorScore {
  return SexualPredictorScore(
    ospIndecentPercentageScore,
    ospContactPercentageScore,
    ospIndecentScoreLevel,
    ospContactScoreLevel,
    ospIndirectImagePercentageScore,
    ospDirectContactPercentageScore,
    ospIndirectImageScoreLevel,
    ospDirectContactScoreLevel,
  )
}

fun groupReconvictionScore(
  oneYear: String? = randomInt().toString(),
  twoYears: String? = randomInt().toString(),
  scoreLevel: String? = randomEnum<OgrsScoreLevel>().toString(),
): GroupReconvictionScore {
  return GroupReconvictionScore(
    oneYear,
    twoYears,
    scoreLevel,
  )
}

fun violencePredictorScore(
  ovpStaticWeightedScore: String? = randomDouble().toString(),
  ovpDynamicWeightedScore: String? = randomDouble().toString(),
  ovpTotalWeightedScore: String? = randomDouble().toString(),
  oneYear: String? = randomInt().toString(),
  twoYears: String? = randomInt().toString(),
  ovpRisk: String? = randomEnum<OvpScoreLevel>().toString(),
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
