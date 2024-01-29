package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation

fun ppudAutomationCreateOffenderResponse(id: String) = """
{
  "offender": {
    "id": "$id"
  }
}
""".trimIndent()
