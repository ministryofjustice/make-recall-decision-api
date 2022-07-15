package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests

fun recommendationRequest(crn: String) = """
  {
    "crn": "$crn"
  }
""".trimIndent()
