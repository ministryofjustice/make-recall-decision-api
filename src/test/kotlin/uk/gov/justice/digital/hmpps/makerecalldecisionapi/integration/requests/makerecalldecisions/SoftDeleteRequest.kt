package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun softDeleteRequest() = """
{
  "status": "DELETED"
}
""".trimIndent()
