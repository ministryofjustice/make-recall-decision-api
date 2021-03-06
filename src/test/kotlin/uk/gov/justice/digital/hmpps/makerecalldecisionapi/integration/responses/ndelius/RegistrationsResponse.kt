package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun registrationsDeliusResponse() = """
{
  "registrations": [
    {
      "active": true,
      "deregisteringNotes": "string",
      "deregisteringOfficer": {
        "code": "AN001A",
        "forenames": "Sheila Linda",
        "surname": "Hancock",
        "unallocated": true
      },
      "deregisteringProbationArea": {
        "code": "ABC123",
        "description": "Some description"
      },
      "deregisteringTeam": {
        "code": "ABC123",
        "description": "Some description"
      },
      "endDate": "2021-01-30",
      "nextReviewDate": "2021-01-30",
      "notes": "string",
      "numberOfPreviousDeregistrations": 0,
      "offenderId": 2500343964,
      "register": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registerCategory": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registerLevel": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registeringOfficer": {
        "code": "AN001A",
        "forenames": "Sheila Linda",
        "surname": "Hancock",
        "unallocated": true
      },
      "registeringProbationArea": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registeringTeam": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registrationId": 2500064995,
      "registrationReviews": [
        {
          "completed": true,
          "notes": "string",
          "reviewDate": "2022-05-09",
          "reviewDateDue": "2022-05-09",
          "reviewingOfficer": {
            "code": "AN001A",
            "forenames": "Sheila Linda",
            "surname": "Hancock",
            "unallocated": true
          },
          "reviewingTeam": {
            "code": "ABC123",
            "description": "Some description"
          }
        }
      ],
      "reviewPeriodMonths": 6,
      "riskColour": "Amber",
      "startDate": "2021-01-30",
      "type": {
        "code": "ABC123",
        "description": "Victim contact"
      },
      "warnUser": true
    },
     {
      "active": false,
      "deregisteringNotes": "string",
      "deregisteringOfficer": {
        "code": "AN001A",
        "forenames": "Sheila Linda",
        "surname": "Hancock",
        "unallocated": true
      },
      "deregisteringProbationArea": {
        "code": "ABC123",
        "description": "Some description"
      },
      "deregisteringTeam": {
        "code": "ABC123",
        "description": "Some description"
      },
      "endDate": "2021-01-30",
      "nextReviewDate": "2021-01-30",
      "notes": "string",
      "numberOfPreviousDeregistrations": 0,
      "offenderId": 2500343964,
      "register": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registerCategory": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registerLevel": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registeringOfficer": {
        "code": "AN001A",
        "forenames": "Sheila Linda",
        "surname": "Hancock",
        "unallocated": true
      },
      "registeringProbationArea": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registeringTeam": {
        "code": "ABC123",
        "description": "Some description"
      },
      "registrationId": 2500064995,
      "registrationReviews": [
        {
          "completed": true,
          "notes": "string",
          "reviewDate": "2022-05-09",
          "reviewDateDue": "2022-05-09",
          "reviewingOfficer": {
            "code": "AN001A",
            "forenames": "Sheila Linda",
            "surname": "Hancock",
            "unallocated": true
          },
          "reviewingTeam": {
            "code": "ABC123",
            "description": "Some description"
          }
        }
      ],
      "reviewPeriodMonths": 6,
      "riskColour": "Amber",
      "startDate": "2021-01-30",
      "type": {
        "code": "ABC124",
        "description": "Mental health issues"
      },
      "warnUser": true
    }
  ]
}
""".trimIndent()
