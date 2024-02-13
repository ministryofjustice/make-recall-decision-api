package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation

fun ppudAutomationUpdateReleaseResponse(id: String) = """
{
  "release": {
    "id": "$id"
  }
}
""".trimIndent()
