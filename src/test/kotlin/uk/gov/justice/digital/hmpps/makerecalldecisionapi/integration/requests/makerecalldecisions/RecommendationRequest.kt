package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests

fun recommendationRequest() = """
  {
    "recommendation": "NOT_RECALL",
    "alternateActions": "increase reporting"
  }
""".trimIndent()
