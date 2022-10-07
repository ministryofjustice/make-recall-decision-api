package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun riskManagementResponse(crn: String?) = """
{
  "crn": "$crn",
  "limitedAccessOffender": true,
  "riskManagementPlan": [
    {
      "assessmentId": 0,
      "dateCompleted": "2022-10-01T14:20:27",
      "partcompStatus": "Part comp status",
      "initiationDate": "2022-10-02T14:20:27",
      "assessmentStatus": "COMPLETE",
      "assessmentType": "LAYER1",
      "superStatus": "COMPLETE",
      "keyInformationCurrentSituation": "patternOfOffending",
      "furtherConsiderationsCurrentSituation": "string",
      "supervision": "string",
      "monitoringAndControl": "string",
      "interventionsAndTreatment": "string",
      "victimSafetyPlanning": "string",
      "contingencyPlans": "I am the contingency plan text",
      "laterWIPAssessmentExists": true,
      "latestWIPDate": "2022-10-03T14:20:27",
      "laterSignLockAssessmentExists": true,
      "latestSignLockDate": "2022-10-04T14:20:27",
      "laterPartCompUnsignedAssessmentExists": true,
      "latestPartCompUnsignedDate": "2022-10-05T14:20:27",
      "laterPartCompSignedAssessmentExists": true,
      "latestPartCompSignedDate": "2022-10-06T14:20:27",
      "laterCompleteAssessmentExists": true,
      "latestCompleteDate": "2022-10-07T14:20:27"
    }
  ]
}
""".trimIndent()
