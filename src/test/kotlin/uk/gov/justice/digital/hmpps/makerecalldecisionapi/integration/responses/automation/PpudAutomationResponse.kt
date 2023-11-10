package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation

fun ppudAutomationResponse(nomsId: String, croNumber: String) = """
{
  "results": [
    {
      "id": "4F6666656E64657269643D313731383138G725H664",
      "croNumber": "$croNumber",
      "nomsId": "$nomsId",
      "firstNames": "John",
      "familyName": "Teal",
      "dateOfBirth": "2000-01-01"
    }
  ]
}
""".trimIndent()
