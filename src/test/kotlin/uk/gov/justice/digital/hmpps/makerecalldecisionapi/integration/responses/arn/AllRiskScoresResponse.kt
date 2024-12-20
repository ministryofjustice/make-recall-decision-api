package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OgpScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OgrsScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspcScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspdcScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspiScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspiicScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OvpScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RsrScoreLevel

fun allRiskScoresResponse() = """
[
{
    "completedDate": "2021-06-16T11:40:54.243",
    "assessmentStatus": "COMPLETE",
    "groupReconvictionScore": {
      "oneYear": 0,
      "twoYears": 0,
      "scoreLevel": "${OgrsScoreLevel.HIGH}"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 0,
      "ovpDynamicWeightedScore": 0,
      "ovpTotalWeightedScore": 0,
      "oneYear": 0,
      "twoYears": 0,
      "ovpRisk": "${OvpScoreLevel.HIGH}"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 0,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 0,
      "ogp1Year": 0,
      "ogp2Year": 0,
      "ogpRisk": "${OgpScoreLevel.HIGH}"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 0,
      "staticOrDynamic": "STATIC",
      "source": "ASSESSMENTS_API",
      "algorithmVersion": "string",
      "scoreLevel": "${RsrScoreLevel.HIGH}"
    },
    "sexualPredictorScore": {
      "ospIndecentPercentageScore": 0,
      "ospContactPercentageScore": 0,
      "ospIndecentScoreLevel": "${OspiScoreLevel.HIGH}",
      "ospContactScoreLevel": "${OspcScoreLevel.HIGH}"
    }
  },
  {
    "completedDate": "2022-04-16T11:40:54.243",
    "assessmentStatus": "COMPLETE",
    "groupReconvictionScore": {
      "oneYear": 0,
      "twoYears": 0,
      "scoreLevel": "${OgrsScoreLevel.LOW}"
    },
    "violencePredictorScore": {
      "ovpStaticWeightedScore": 0,
      "ovpDynamicWeightedScore": 0,
      "ovpTotalWeightedScore": 0,
      "oneYear": 0,
      "twoYears": 0,
      "ovpRisk": "${OvpScoreLevel.LOW}"
    },
    "generalPredictorScore": {
      "ogpStaticWeightedScore": 0,
      "ogpDynamicWeightedScore": 0,
      "ogpTotalWeightedScore": 12,
      "ogp1Year": 0,
      "ogp2Year": 0,
      "ogpRisk": "${OgpScoreLevel.LOW}"
    },
    "riskOfSeriousRecidivismScore": {
      "percentageScore": 23,
      "staticOrDynamic": "STATIC",
      "source": "ASSESSMENTS_API",
      "algorithmVersion": "string",
      "scoreLevel": "${RsrScoreLevel.HIGH}"
    },
    "sexualPredictorScore": {
      "ospIndirectImagePercentageScore": 5,
      "ospDirectContactPercentageScore": 3.45,
      "ospIndirectImageScoreLevel": "${OspiicScoreLevel.MEDIUM}",
      "ospDirectContactScoreLevel": "${OspdcScoreLevel.LOW}"
    }
  }
]
""".trimIndent()
