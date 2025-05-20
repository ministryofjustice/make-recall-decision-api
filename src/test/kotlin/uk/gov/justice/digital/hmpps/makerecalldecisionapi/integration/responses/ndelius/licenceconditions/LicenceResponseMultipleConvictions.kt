package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions

fun licenceResponseMultipleConvictions() = """
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
    "ethnicity": "White",
    "primaryLanguage": "English"
  },
  "activeConvictions": [
    {
      "number": "1",
      "mainOffence": {
        "code": "789",
        "description": "Robbery (other than armed robbery)",
        "date": "2022-04-26"
      },
      "additionalOffences": [],
      "sentence": {
        "description": "Extended Determinate Sentence",
        "length": 0,
        "lengthUnits": "string",
        "isCustodial": true,
        "custodialStatusCode": "ABC123",
        "licenceExpiryDate": "2020-06-23",
        "sentenceExpiryDate": "2020-06-23"
      },
      "licenceConditions": [
        {
          "startDate": "2022-04-24",
          "notes": "I am a licence condition note",
          "mainCategory": {
            "code": "NLC8",
            "description": "Freedom of movement"
          },
          "subCategory": {
            "code": "NSTT8",
            "description": "To only attend places of worship which have been previously agreed with your supervising officer."
          }
        },
        {
          "startDate": "2022-04-24",
          "notes": "I am a second licence condition note",
          "mainCategory": {
            "code": "NLC7",
            "description": "Inactive test"
          },
          "subCategory": {
            "code": "NSTT7",
            "description": "I am inactive"
          }
        }
      ]
    },
    {
      "number": "2",
      "mainOffence": {
        "code": "123",
        "description": "Arson",
        "date": "2022-04-26"
      },
      "additionalOffences": [{
        "code": "456",
        "description": "Shoplifting",
        "date": "2022-04-27"
      }],
      "sentence": {
        "description": "string",
        "length": 0,
        "lengthUnits": "string",
        "isCustodial": true,
        "custodialStatusCode": "ABC123",
        "licenceExpiryDate": "2020-06-20",
        "sentenceExpiryDate": "2020-06-23"
      },
      "licenceConditions": [
        {
          "startDate": "2022-04-24",
          "notes": "I am a licence condition note",
          "mainCategory": {
            "code": "NLC8",
            "description": "Freedom of movement"
          },
          "subCategory": {
            "code": "NSTT8",
            "description": "To only attend places of worship which have been previously agreed with your supervising officer."
          }
        },
        {
          "startDate": "2022-04-24",
          "notes": "I am a second licence condition note",
          "mainCategory": {
            "code": "NLC7",
            "description": "Inactive test"
          },
          "subCategory": {
            "code": "NSTT7",
            "description": "I am inactive"
          }
        }
      ]
    }
  ]
}
""".trimIndent()
