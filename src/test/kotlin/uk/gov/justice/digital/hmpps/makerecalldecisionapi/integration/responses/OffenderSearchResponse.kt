package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses

fun offenderSearchResponse(crn: String) = """
{
  "content": [
    {
      "firstName": "Pontius",
      "surname": "Pilate",
      "dateOfBirth": "2000-11-09"
    }
  ]
}
""".trimIndent()
