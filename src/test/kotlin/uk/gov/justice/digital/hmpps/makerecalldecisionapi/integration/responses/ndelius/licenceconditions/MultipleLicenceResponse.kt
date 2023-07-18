package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions

fun multipleLicenceResponse() = """
{
  "personalDetails": {
    "name": {
      "forename": "John",
      "middleName": "Homer Bart",
      "surname": "Smith"
    },
    "identifiers": {
      "pncNumber": "2004/0712343H",
      "croNumber": "123456/04A",
      "nomsNumber": "A1234CR",
      "bookingNumber": "G12345"
    },
    "dateOfBirth": "1982-10-24",
    "gender": "Male",
    "ethnicity": "Ainu",
    "primaryLanguage": "English"
  },
  "activeConvictions": [
    {
      "number": "1",
      "mainOffence": {
        "code": "1234",
        "description": "Robbery (other than armed robbery)",
        "date": "2022-04-24"
      },
      "additionalOffences": [],
      "sentence": {
        "description": "Extended Determinate Sentence",
        "length": 12,
        "lengthUnits": "days",
        "isCustodial": true,
        "custodialStatusCode": "ABC123",
        "licenceExpiryDate": "2020-06-25",
        "sentenceExpiryDate": "2020-06-28"
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
          "mainCategory": {
            "code": "NLC9",
            "description": "Another main condition"
          },
          "subCategory": {
            "code": "NSTT9",
            "description": "Do not attend Hull city center after 8pm"
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
