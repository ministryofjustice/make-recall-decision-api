package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation

fun ppudAutomationCreateRecallResponse(id: String) = """
{
  "recall": {
    "id": "$id"
  }
}
""".trimIndent()
