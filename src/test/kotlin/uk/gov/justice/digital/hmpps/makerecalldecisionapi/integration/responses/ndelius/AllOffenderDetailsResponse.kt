package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun personalDetailsResponse(district: String? = "Sheffield City Centre", firstName: String? = "John") = """
{
  "personalDetails": {
    "name": {
      "forename": "$firstName",
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
  "mainAddress": {
    "buildingName": "HMPPS Digital Studio",
    "addressNumber": "33",
    "streetName": "Scotland Street",
    "district": "$district",
    "town": "Sheffield",
    "county": "South Yorkshire",
    "postcode": "S3 7BS",
    "noFixedAbode": false
  },
  "communityManager": {
    "staffCode": "AN001A",
    "name": {
      "forename": "Sheila",
      "middleName": "Linda",
      "surname": "Hancock"
    },
    "provider": {
      "code": "N01",
      "name": "NPS North West"
    },
    "team": {
      "code": "C01T04",
      "name": "OMU A",
      "localAdminUnit": "Some description",
      "telephone": "09056714321",
      "email": "first.last@digital.justice.gov.uk"
    }
  }
}
""".trimIndent()
