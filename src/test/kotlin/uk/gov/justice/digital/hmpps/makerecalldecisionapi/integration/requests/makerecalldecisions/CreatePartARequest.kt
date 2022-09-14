package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun createPartARequest() = """
  {
    "userEmail": "some.user@email.com"
  }
""".trimIndent()
