package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses

fun offenderSearchResponse(crn: String) = """
[
  {
    "name": "Paula Smith",
    "dateOfBirth": "2000-11-09",
    "crn": "A12345"
  }
]
""".trimIndent()
