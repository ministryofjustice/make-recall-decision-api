package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun providerResponse(code: String, name: String) = """
{
  "code": "$code",
  "name": "$name"
}
""".trimIndent()
