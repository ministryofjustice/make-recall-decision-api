package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun documentRequestQuery(documentRequestQuery: String) = """
  {
    "format": "$documentRequestQuery"
  }
""".trimIndent()
