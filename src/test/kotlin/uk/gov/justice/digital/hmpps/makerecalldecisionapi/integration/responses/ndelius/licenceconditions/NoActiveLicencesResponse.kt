package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions

fun noActiveOrInactiveLicences() = """
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
      "licenceConditions": []
    }
  ]
}
""".trimIndent()
