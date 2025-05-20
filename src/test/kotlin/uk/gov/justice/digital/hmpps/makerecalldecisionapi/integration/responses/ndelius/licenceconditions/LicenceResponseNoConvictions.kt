package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions

fun licenceResponseNoConvictions() = """
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
  "activeConvictions": []
}
""".trimIndent()
