package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun allRiskScoresEmptyResponse() = """
[
  {
    "completedDate": "2021-06-16T11:40:54.243",
    "status": "COMPLETE",
    "outputVersion": "1",
    "output": {
      "groupReconvictionScore": null,
      "violencePredictorScore": null,
      "generalPredictorScore": null,
      "riskOfSeriousRecidivismScore": null,
      "sexualPredictorScore": null
    }
  },
   {
    "completedDate": "2020-06-16T11:40:54.243",
    "status": "COMPLETE",
    "outputVersion": "1",
    "output": {
      "groupReconvictionScore": null,
      "violencePredictorScore": null,
      "generalPredictorScore": null,
      "riskOfSeriousRecidivismScore": null,
      "sexualPredictorScore": null
    }
  }
]
""".trimIndent()
