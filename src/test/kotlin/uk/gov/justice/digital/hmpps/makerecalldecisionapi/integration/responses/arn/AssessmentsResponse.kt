package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun assessmentsResponse(crn: String?, laterCompleteAssessmentExists: Boolean? = false) = """
{
  "crn": "$crn",
  "limitedAccessOffender": true,
  "assessments": [
    {
      "assessmentId": 0,
      "assessmentType": "string",
      "partcompStatus": "string",
      "dateCompleted": "2022-04-24T15:00:08.286Z",
      "initiationDate": "2022-09-12T15:00:08.286Z",
      "assessorSignedDate": "2022-09-12T15:00:08.286Z",
      "assessmentStatus": "COMPLETED",
      "superStatus": "COMPLETED",
      "offence": "Juicy offence details.",
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
          "type": "string",
          "offenceDate": "2022-04-24T20:39:47.778Z",
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
      "latestWIPDate": "2022-09-12T15:00:08.286Z",
      "laterSignLockAssessmentExists": false,
      "latestSignLockDate": "2022-09-12T15:00:08.286Z",
      "laterPartCompUnsignedAssessmentExists": false,
      "latestPartCompUnsignedDate": "2022-09-12T15:00:08.286Z",
      "laterPartCompSignedAssessmentExists": false,
      "latestPartCompSignedDate": "2022-09-12T15:00:08.286Z",
      "laterCompleteAssessmentExists": $laterCompleteAssessmentExists,
      "latestCompleteDate": "2022-09-12T15:00:08.286Z"
    },
    {
      "assessmentId": 1,
      "assessmentType": "string",
      "partcompStatus": "string",
      "dateCompleted": "2022-04-23T15:00:08.286Z",
      "initiationDate": "2022-09-12T15:00:08.286Z",
      "assessorSignedDate": "2022-09-12T15:00:08.286Z",
      "assessmentStatus": "COMPLETED",
      "superStatus": "COMPLETED",
      "offence": "Not so juicy offence details.",
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
          "type": "string",
          "offenceDate": "2022-04-24T20:39:47.778Z",
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
      "latestWIPDate": "2022-09-12T15:00:08.286Z",
      "laterSignLockAssessmentExists": false,
      "latestSignLockDate": "2022-09-12T15:00:08.286Z",
      "laterPartCompUnsignedAssessmentExists": false,
      "latestPartCompUnsignedDate": "2022-09-12T15:00:08.286Z",
      "laterPartCompSignedAssessmentExists": false,
      "latestPartCompSignedDate": "2022-09-12T15:00:08.286Z",
      "laterCompleteAssessmentExists": $laterCompleteAssessmentExists,
      "latestCompleteDate": "2022-09-12T15:00:08.286Z"
    }
  ]
}
""".trimIndent()
