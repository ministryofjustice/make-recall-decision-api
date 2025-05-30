package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun assessmentsResponse(crn: String?, laterCompleteAssessmentExists: Boolean? = false, offenceType: String? = "CURRENT", superStatus: String? = "COMPLETE") = """
{
  "crn": "$crn",
  "limitedAccessOffender": true,
  "assessments": [
    {
      "assessmentId": 0,
      "assessmentType": "string",
      "partcompStatus": "string",
      "dateCompleted": "2022-04-24T15:00:08.000",
      "initiationDate": "2022-09-12T15:00:08",
      "assessorSignedDate": "2022-09-12T15:00:08",
      "assessmentStatus": "COMPLETE",
      "superStatus": "$superStatus",
      "offence": "Offence details.",
      "disinhibitors": [
        "string"
      ],
      "patternOfOffending": "string",
      "offenceInvolved": [
        "string"
      ],
      "specificWeapon": "string",
      "victimPerpetratorRelationship": "string",
      "victimOtherInfo": "string",
      "evidencedMotivations": [
        "string"
      ],
      "offenceDetails": [
        {
          "type": "NOT_CURRENT",
          "offenceDate": "2022-04-24T20:39:47",
          "offenceCode": "56",
          "offenceSubCode": "78",
          "offence": "string",
          "subOffence": "string"
        },
        {
          "type": "$offenceType",
          "offenceDate": "2022-04-24T20:39:47",
          "offenceCode": "12",
          "offenceSubCode": "34",
          "offence": "string",
          "subOffence": "string"
        }
      ],
      "victimDetails": [
        {
          "age": "string",
          "gender": "string",
          "ethnicCategory": "string",
          "victimRelation": "string"
        }
      ],
      "laterWIPAssessmentExists": false,
      "latestWIPDate": "2022-09-12T15:00:08",
      "laterSignLockAssessmentExists": false,
      "latestSignLockDate": "2022-09-12T15:00:08",
      "laterPartCompUnsignedAssessmentExists": false,
      "latestPartCompUnsignedDate": "2022-09-12T15:00:08",
      "laterPartCompSignedAssessmentExists": false,
      "latestPartCompSignedDate": "2022-09-12T15:00:08",
      "laterCompleteAssessmentExists": $laterCompleteAssessmentExists,
      "latestCompleteDate": "2022-09-12T15:00:08"
    },
    {
      "assessmentId": 1,
      "assessmentType": "string",
      "partcompStatus": "string",
      "dateCompleted": "2022-04-23T15:00:08.000",
      "initiationDate": "2022-08-12T15:00:08",
      "assessorSignedDate": "2022-09-12T15:00:08",
      "assessmentStatus": "COMPLETE",
      "superStatus": "COMPLETE",
      "offence": "Different offence details.",
      "disinhibitors": [
        "string"
      ],
      "patternOfOffending": "string",
      "offenceInvolved": [
        "string"
      ],
      "specificWeapon": "string",
      "victimPerpetratorRelationship": "string",
      "victimOtherInfo": "string",
      "evidencedMotivations": [
        "string"
      ],
      "offenceDetails": [
        {
          "type": "NOT_CURRENT",
          "offenceDate": "2022-04-24T20:39:47",
          "offenceCode": "78",
          "offenceSubCode": "90",
          "offence": "string",
          "subOffence": "string"
        }
      ],
      "victimDetails": [
        {
          "age": "string",
          "gender": "string",
          "ethnicCategory": "string",
          "victimRelation": "string"
        }
      ],
      "laterWIPAssessmentExists": false,
      "latestWIPDate": "2022-09-12T15:00:08",
      "laterSignLockAssessmentExists": false,
      "latestSignLockDate": "2022-09-12T15:00:08",
      "laterPartCompUnsignedAssessmentExists": false,
      "latestPartCompUnsignedDate": "2022-09-12T15:00:08",
      "laterPartCompSignedAssessmentExists": false,
      "latestPartCompSignedDate": "2022-09-12T15:00:08",
      "laterCompleteAssessmentExists": $laterCompleteAssessmentExists,
      "latestCompleteDate": "2022-09-12T15:00:08"
    }
  ]
}
""".trimIndent()
