package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

internal fun createPartARequest() = """
  {
    "userEmail": "some.user@email.com"
  }
""".trimIndent()
