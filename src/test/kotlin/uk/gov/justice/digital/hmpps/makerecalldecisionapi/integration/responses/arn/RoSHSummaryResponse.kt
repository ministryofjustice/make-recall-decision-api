package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun roSHSummaryResponse() = """
{
  "whoIsAtRisk": "X, Y and Z are at risk",
  "natureOfRisk": "The nature of the risk is X",
  "riskImminence": "the risk is imminent and more probably in X situation",
  "riskIncreaseFactors": "If offender in situation X the risk can be higher",
  "riskMitigationFactors": "Giving offender therapy in X will reduce the risk",
  "riskInCommunity": {
    "HIGH": [
      "Children",
      "Public",
      "Known adult"
    ],
    "MEDIUM": [
      "Staff"
    ]
  },
  "riskInCustody": {
    "HIGH": [
      "Known adult"
    ],
    "VERY_HIGH": [
      "Staff",
      "Prisoners"
    ],
    "LOW": [
      "Children",
      "Public"
    ]
  },
  "assessedOn": "2022-05-19T08:26:31",
  "overallRiskLevel": "HIGH"
}
""".trimIndent()
