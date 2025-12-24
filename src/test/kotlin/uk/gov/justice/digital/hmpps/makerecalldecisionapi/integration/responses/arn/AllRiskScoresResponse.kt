package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.FourLevelRiskScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ThreeLevelRiskScoreLevel

fun allRiskScoresResponse() = """
[
{
    "completedDate": "2021-06-16T11:40:54.243",
    "assessmentStatus": "COMPLETE",
    "groupReconvictionScore": {
      "oneYear": 0,
      "twoYears": 0,
      "scoreLevel": "${FourLevelRiskScoreLevel.HIGH}"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 0,
      "ovpDynamicWeightedScore": 0,
      "ovpTotalWeightedScore": 0,
      "oneYear": 0,
      "twoYears": 0,
      "ovpRisk": "${FourLevelRiskScoreLevel.HIGH}"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 0,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 0,
      "ogp1Year": 0,
      "ogp2Year": 0,
      "ogpRisk": "${FourLevelRiskScoreLevel.HIGH}"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 0,
      "staticOrDynamic": "STATIC",
      "source": "ASSESSMENTS_API",
      "algorithmVersion": "string",
      "scoreLevel": "${ThreeLevelRiskScoreLevel.HIGH}"
    },
    "sexualPredictorScore": {
      "ospIndecentPercentageScore": 0,
      "ospContactPercentageScore": 0,
      "ospIndecentScoreLevel": "${ThreeLevelRiskScoreLevel.HIGH}",
      "ospContactScoreLevel": "${FourLevelRiskScoreLevel.HIGH}"
    }
  },
  {
    "completedDate": "2022-04-16T11:40:54.243",
    "assessmentStatus": "COMPLETE",
    "groupReconvictionScore": {
      "oneYear": 0,
      "twoYears": 0,
      "scoreLevel": "${FourLevelRiskScoreLevel.LOW}"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 0,
      "ovpDynamicWeightedScore": 0,
      "ovpTotalWeightedScore": 0,
      "oneYear": 0,
      "twoYears": 0,
      "ovpRisk": "${FourLevelRiskScoreLevel.LOW}"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 0,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 12,
      "ogp1Year": 0,
      "ogp2Year": 0,
      "ogpRisk": "${FourLevelRiskScoreLevel.LOW}"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 23,
      "staticOrDynamic": "STATIC",
      "source": "ASSESSMENTS_API",
      "algorithmVersion": "string",
      "scoreLevel": "${ThreeLevelRiskScoreLevel.HIGH}"
    },
    "sexualPredictorScore": {
      "ospIndirectImagePercentageScore": 5,
      "ospDirectContactPercentageScore": 3.45,
      "ospIndirectImageScoreLevel": "${ThreeLevelRiskScoreLevel.MEDIUM}",
      "ospDirectContactScoreLevel": "${FourLevelRiskScoreLevel.LOW}"
    }
  }
]
""".trimIndent()
