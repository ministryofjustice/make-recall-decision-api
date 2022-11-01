package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

internal fun documentRequestQuery(documentRequestQuery: String) = """
  {
    "format": "$documentRequestQuery"
  }
""".trimIndent()
