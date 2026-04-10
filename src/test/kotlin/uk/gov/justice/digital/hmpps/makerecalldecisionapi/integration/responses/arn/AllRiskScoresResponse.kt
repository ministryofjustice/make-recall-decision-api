package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

// There are 3 risk scores in the response,
// 1st one with date 2021-06-16T11:40:54.243 is V1 response for sexualPredictorScore with OSPC & OSPIC scores
// 2nd one with date 2022-06-16T11:40:54.243 is V1 response for sexualPredictorScore with OSPDC & OSPIIC scores
// 3rd one with date 2023-06-16T11:40:54.243 is V2 response
fun allRiskScoresResponse() = """
[
{
    "completedDate": "2021-06-16T11:40:54.243",
    "status": "COMPLETE",
    "outputVersion": "1",
    "output": {
      "groupReconvictionScore": {
      "oneYear": 0,
      "twoYears": 0,
      "scoreLevel": "LOW"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 0,
      "ovpDynamicWeightedScore": 0,
      "ovpTotalWeightedScore": 0,
      "oneYear": 0,
      "twoYears": 0,
      "ovpRisk": "LOW"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 0,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 0,
      "ogp1Year": 0,
      "ogp2Year": 0,
      "ogpRisk": "LOW"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 23,
      "staticOrDynamic": "STATIC",
      "source": "ASSESSMENTS_API",
      "algorithmVersion": "string",
      "scoreLevel": "HIGH"
    },
    "sexualPredictorScore": {
      "ospIndecentPercentageScore": 0,
      "ospContactPercentageScore": 0,
      "ospIndecentScoreLevel": "HIGH",
      "ospContactScoreLevel": "NOT_APPLICABLE"
      }
    }
  },
  {
    "completedDate": "2022-06-16T11:40:54.243",
    "status": "COMPLETE",
    "outputVersion": "1",
    "output": {
      "groupReconvictionScore": {
      "oneYear": 0,
      "twoYears": 0,
      "scoreLevel": "NOT_APPLICABLE"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 0,
      "ovpDynamicWeightedScore": 0,
      "ovpTotalWeightedScore": 0,
      "oneYear": 0,
      "twoYears": 0,
      "ovpRisk": "LOW"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 0,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 0,
      "ogp1Year": 0,
      "ogp2Year": 0,
      "ogpRisk": "LOW"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 23,
      "staticOrDynamic": "STATIC",
      "source": "ASSESSMENTS_API",
      "algorithmVersion": "string",
      "scoreLevel": "HIGH"
    },
    "sexualPredictorScore": {
      "ospIndirectImagePercentageScore": 0,
      "ospDirectContactPercentageScore": 0,
      "ospIndirectImageScoreLevel":	"MEDIUM",
      "ospDirectContactScoreLevel":	"LOW"
      }
    }
  },
  {
    "completedDate": "2023-06-16T11:40:54.243",
    "status": "COMPLETE",
    "outputVersion": "2",
    "output": {
      "allReoffendingPredictor": {
        "score": 12.5,
        "band": "NOT_APPLICABLE",
        "staticOrDynamic": "STATIC"
      },
      "violentReoffendingPredictor": {
        "score": 8.0,
        "band": "LOW",
        "staticOrDynamic": "DYNAMIC"
      },
      "seriousViolentReoffendingPredictor": {
        "score": 15.2,
        "band": "HIGH",
        "staticOrDynamic": "STATIC"
      },
      "directContactSexualReoffendingPredictor": {
        "score": 6.3,
        "band": "LOW",
        "staticOrDynamic": "STATIC"
      },
      "indirectImageContactSexualReoffendingPredictor": {
        "score": 9.8,
        "band": "NOT_APPLICABLE",
        "staticOrDynamic": "STATIC"
      },
      "combinedSeriousReoffendingPredictor": {
        "score": 18.7,
        "band": "VERY_HIGH",
        "staticOrDynamic": "DYNAMIC",
        "algorithmVersion": "v2.1.0"
      }
    }
  }
]
""".trimIndent()
