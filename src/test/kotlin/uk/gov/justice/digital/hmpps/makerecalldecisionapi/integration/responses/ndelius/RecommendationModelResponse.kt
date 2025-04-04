package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun deliusRecommendationModelResponse(firstName: String) = """
{
  "personalDetails": {
    "name": {
      "forename": "$firstName",
      "middleName": "Homer Bart",
      "surname": "Smith"
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
    "ethnicity": "Ainu",
    "primaryLanguage": "English"
  },
  "mappa": {
    "level": 1,
    "category": 0,
    "notes": "Please Note - Category 3 offenders require multi-agency management at Level 2 or 3 and should not be recorded at Level 1.\nNote\nnew note",
    "startDate": "2021-02-10"
  },
  "lastRelease": {
    "releaseDate": "2017-09-15",
    "recallDate": "2020-10-15"
  },
  "lastReleasedFromInstitution": {
    "name": "Addiewell"
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
      }
    }
  ],
  "activeCustodialConvictions": [
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
        "startDate": "2022-04-26",
        "length": 12,
        "lengthUnits": "days",
        "secondLength": 19,
        "secondLengthUnits": "days",
        "isCustodial": true,
        "custodialStatusCode": "ABC123",
        "licenceExpiryDate": "2020-06-25",
        "sentenceExpiryDate": "2020-06-28"
      }
    }
  ]
}
""".trimIndent()
