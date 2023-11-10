package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.prison

fun prisonResponse(description: String) = """
{
  "locationDescription": "$description"
}
""".trimIndent()
