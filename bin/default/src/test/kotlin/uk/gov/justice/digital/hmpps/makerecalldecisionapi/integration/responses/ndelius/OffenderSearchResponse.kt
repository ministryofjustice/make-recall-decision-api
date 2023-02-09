package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun offenderSearchDeliusResponse(crn: String?) = """
{
  "content": [
    {
      "firstName": "Pontius",
      "surname": "Pilate",
      "dateOfBirth": "2000-11-09",
      "otherIds": { "crn": "$crn"}
    }
  ]
}
""".trimIndent()
