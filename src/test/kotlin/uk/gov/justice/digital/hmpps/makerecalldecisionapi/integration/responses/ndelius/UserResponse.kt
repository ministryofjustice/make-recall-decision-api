package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun userResponse(username: String, email: String) = """
{
  "username": "$username",
  "email": "$email",
  "staffCode": "TEST01",
  "name": {
    "forename": "John",
    "middleName": null,
    "surname": "Smith"
  }
}
""".trimIndent()
