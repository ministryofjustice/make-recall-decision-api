package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun limitedAccessOffenderSearchResponse(crn: String) = """
{
  "content": [
    {
      "offenderId": 123456753,
      "otherIds": {
        "crn": "$crn"
      },
      "accessDenied": true
    }
]
}
""".trimIndent()
