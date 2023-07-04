package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun userResponse(username: String) = """
{
  "username": "$username",
  "email": "test@digital.justice.gov.uk",
  "staffCode": "TEST01",
  "name": {
    "forename": "John",
    "middleName": null,
    "surname": "Smith"
  }
}
""".trimIndent()
