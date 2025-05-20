package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun overviewResponse() = """
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
  "registerFlags": ["Victim contact"],
  "lastRelease": {
    "releaseDate": "2017-09-15",
    "recallDate": "2020-10-15"
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
  ]
}
""".trimIndent()

fun overviewResponseNonCustodial() = """
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
  "registerFlags": ["Victim contact"],
  "lastRelease": {
    "releaseDate": "2017-09-15",
    "recallDate": "2020-10-15"
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
        "isCustodial": false,
        "licenceExpiryDate": "2020-06-25",
        "sentenceExpiryDate": "2020-06-28"
      }
    }
  ]
}
""".trimIndent()

fun overviewResponseNoConvictions() = """
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
  "registerFlags": ["Victim contact"],
  "activeConvictions": []
}
""".trimIndent()
