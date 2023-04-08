package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun recommendationStatusRequest(activate: String, deactivate: String? = null) = """
  {
    "activate": "$activate",
    "deActivate": "$deactivate"
  }
""".trimIndent()
