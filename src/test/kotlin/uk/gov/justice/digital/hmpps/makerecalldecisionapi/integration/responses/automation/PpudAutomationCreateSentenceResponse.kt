package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation


fun ppudAutomationCreateSentenceResponse(id: String) = """
{
  "sentence": {
    "id": "$id"
  }
}
""".trimIndent()
