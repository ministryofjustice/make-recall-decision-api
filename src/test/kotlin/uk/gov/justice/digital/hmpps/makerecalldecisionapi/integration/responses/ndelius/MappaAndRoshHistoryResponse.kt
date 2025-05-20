package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun deliusMappaAndRoshHistoryResponse(mappaLevel: Int = 1, mappaCategory: Int = 0) = """
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
  "mappa": {
    "level": $mappaLevel,
    "category": $mappaCategory,
    "startDate": "2021-02-10"
  },
  "roshHistory": [
    {
      "active": true,
      "type": "RVHR",
      "typeDescription": "Very High RoSH",
      "notes": "Notes on Very High RoSH case",
      "startDate": "2021-01-30"
    }
  ]
}
""".trimIndent()

fun deliusRoshHistoryOnlyResponse(mappaLevel: Int = 1, mappaCategory: Int = 0) = """
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
  "roshHistory": [
    {
      "active": true,
      "type": "RVHR",
      "typeDescription": "Very High RoSH",
      "notes": "Notes on Very High RoSH case",
      "startDate": "2021-01-30"
    }
  ]
}
""".trimIndent()

fun deliusNoMappaOrRoshHistoryResponse() = """
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
  "roshHistory": []
}
""".trimIndent()
