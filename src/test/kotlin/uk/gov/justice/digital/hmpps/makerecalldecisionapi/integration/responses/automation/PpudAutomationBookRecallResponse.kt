package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation

fun ppudAutomationBookRecallResponse(id: String) = """
{
  "recall": {
    "id": "$id"
  }
}
""".trimIndent()
