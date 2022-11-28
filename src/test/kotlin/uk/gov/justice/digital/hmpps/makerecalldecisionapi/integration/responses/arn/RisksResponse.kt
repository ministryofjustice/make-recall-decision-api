package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun risksDataResponse() = """
{
  "riskToSelf": {
    "suicide": {
      "risk": "Yes",
      "previous": "Yes",
      "previousConcernsText": "Previous risk of suicide concerns due to ...",
      "current": "Yes",
      "currentConcernsText": "Risk of suicide concerns due to ..."
    },
    "selfHarm": {
      "risk": "Yes",
      "previous": "Yes",
      "previousConcernsText": "Previous risk of self harm concerns due to ...",
      "current": "Yes",
      "currentConcernsText": "Risk of self harm concerns due to ..."
    },
    "custody": {
      "risk": "Yes",
      "previous": "Yes",
      "previousConcernsText": "Previous risk of custody concerns due to ...",
      "current": "Yes",
      "currentConcernsText": "Risk of custody concerns due to ..."
    },
    "hostelSetting": {
      "risk": "Yes",
      "previous": "Yes",
      "previousConcernsText": "Previous risk of hostel setting concerns due to ...",
      "current": "Yes",
      "currentConcernsText": "Risk of hostel setting concerns due to ..."
    },
    "vulnerability": {
      "risk": "Yes",
      "previous": "Yes",
      "previousConcernsText": "Previous risk of vulnerability concerns due to ...",
      "current": "Yes",
      "currentConcernsText": "Risk of vulnerability concerns due to ..."
    }
  },
  "otherRisks": {
    "escapeOrAbscond": "YES",
    "controlIssuesDisruptiveBehaviour": "YES",
    "breachOfTrust": "YES",
    "riskToOtherPrisoners": "YES"
  },
  "summary": {
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
      ],
      "LOW": [
        "Prisoners"
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
    "overallRiskLevel": "HIGH"
  },
  "assessedOn": "2022-11-23T00:01:50"
}
""".trimIndent()
