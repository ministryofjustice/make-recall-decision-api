package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun prisonOffenderSearchRequest(nomsId: String) = """
  {
    "nomsId": "$nomsId"
  }
""".trimIndent()
