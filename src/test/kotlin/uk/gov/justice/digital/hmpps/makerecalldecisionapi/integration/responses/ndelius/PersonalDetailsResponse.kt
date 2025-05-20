package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun personalDetailsResponse(district: String? = "Sheffield City Centre", firstName: String? = "Joe", nomisId: String? = "A1234CR") = """
{
  "personalDetails": {
    "name": {
      "forename": "$firstName",
      "middleName": "Michael",
      "surname": "Bloggs"
    },
    "identifiers": {
      "crn": "X000001",
      "pncNumber": "2004/0712343H",
      "croNumber": "123456/04A",
      "nomsNumber": "$nomisId",
      "bookingNumber": "G12345"
    },
    "dateOfBirth": "1982-10-24",
    "gender": "Male",
    "ethnicity": "White",
    "primaryLanguage": "English"
  },
  "mainAddress": {
    "buildingName": "HMPPS Digital Studio",
    "addressNumber": "33",
    "streetName": "Scotland Street",
    "district": "$district",
    "town": "Sheffield",
    "county": "South Yorkshire",
    "postcode": "S12 345",
    "noFixedAbode": false
  },
  "communityManager": {
    "staffCode": "AN001A",
    "name": {
      "forename": "Jane",
      "middleName": "Linda",
      "surname": "Bloggs"
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
