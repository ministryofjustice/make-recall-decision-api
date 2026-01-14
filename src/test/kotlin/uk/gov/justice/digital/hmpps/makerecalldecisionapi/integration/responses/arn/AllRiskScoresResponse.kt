package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

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
      "ogpTotalWeightedScore": 12,
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
      "ospIndirectImagePercentageScore": 5,
      "ospDirectContactPercentageScore": 3.45,
      "ospIndirectImageScoreLevel":	"MEDIUM",
      "ospDirectContactScoreLevel":	"LOW"
      }
    }
  },
  {
    "completedDate": "2020-04-16T11:40:54.243",
    "status": "COMPLETE",
    "outputVersion": "2",
    "output": {
      "allReoffendingPredictor": {
        "score": 12.5,
        "band": "MEDIUM",
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
        "band": "MEDIUM",
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
