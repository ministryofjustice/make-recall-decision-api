package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses

fun deliusContactHistoryResponse() = """
{
  "personalDetails": {
    "name": {
      "forename": "Joe",
      "middleName": "Michael",
      "surname": "Bloggs"
    },
    "identifiers": {
      "crn": "X000001",
      "pncNumber": "2004/0712343H",
      "croNumber": "123456/04A",
      "nomsNumber": "A1234CR",
      "bookingNumber": "G12345"
    },
    "dateOfBirth": "1982-10-24",
    "gender": "Male",
    "ethnicity": "Caucasian",
    "primaryLanguage": "English"
  },
  "contacts": [
    {
      "startDateTime": "2022-06-03T08:00:00+01:00[Europe/London]",
      "type": {
        "code": "COAP",
        "description": "Registration Review",
        "systemGenerated": true
      },
      "notes": "Comment added by Joe Bloggs on 05/05/2022",
      "documents": [
        {
          "id": "f2943b31-2250-41ab-a04d-004e27a97add",
          "name": "test doc.docx",
          "lastUpdated": "2022-06-21T20:27:23.407+01:00[Europe/London]"
        }
      ]
    },
    {
      "startDateTime": "2022-05-10T11:39:00+01:00",
      "type": {
        "code": "C204",
        "description": "Police Liaison",
        "systemGenerated": false
      },
      "notes": "This is a test",
      "sensitive": true,
      "enforcementAction": "Enforcement Letter Requested",
      "outcome": "Test - Not Clean / Not Acceptable / Unsuitable",
      "description": "This is a contact description",
      "documents": [
        {
          "id": "630ca741-cbb6-4f2e-8e86-73825d8c4d82",
          "name": "a test.pdf",
          "lastUpdated": "2022-06-21T20:29:17.324+01:00[Europe/London]"
        }
      ]
    },
    {
      "startDateTime": "2022-06-10T11:39:00+01:00",
      "type": {
        "code": "C204",
        "description": "Police Liaison",
        "systemGenerated": false
      },
      "notes": "Conviction test",
      "sensitive": true,
      "enforcementAction": "Enforcement Letter Requested",
      "outcome": "Test - Not Clean / Not Acceptable / Unsuitable",
      "description": "This is a conviction contact",
      "documents": [
        {
          "id": "374136ce-f863-48d8-96dc-7581636e461e",
          "name": "ContactDoc.pdf",
          "lastUpdated": "2022-06-07T17:00:29.493+01:00[Europe/London]"
        }
      ]
    }
  ],
  "summary": {
    "types": [
      {
        "code": "C204",
        "description": "Police Liaison",
        "hits": 2
      },
      {
        "code": "COAP",
        "description": "Registration Review",
        "hits": 1
      }
    ],
    "hits": 3,
    "total": 3
  }
}
""".trimIndent()
