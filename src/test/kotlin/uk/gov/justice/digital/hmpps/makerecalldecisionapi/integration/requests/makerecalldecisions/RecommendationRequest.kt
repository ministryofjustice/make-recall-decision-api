package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun recommendationRequest(crn: String) = """
  {
    "crn": "$crn"
  }
""".trimIndent()
