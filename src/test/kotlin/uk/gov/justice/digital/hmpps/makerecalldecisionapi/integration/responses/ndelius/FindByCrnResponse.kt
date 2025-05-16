package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun findByCrnResponse(
  crn: String? = "X123456",
  firstName: String? = "Pontius",
  surname: String? = "Pilate",
  dateOfBirth: String? = "2000-11-30",
  // language=json
) = """
{
  "name": {
    "forename": "$firstName",
    "surname": "$surname"
  },
  "dateOfBirth": "$dateOfBirth",
  "gender": "Male",
  "identifiers": {
    "crn": "$crn"
  }
}
""".trimIndent()
