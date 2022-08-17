package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

fun riskSummaryUnavailableResponse() = """
{
  "status": 500,
  "developerMessage": "Failed to retrieve Rosh sections for last year completed Assessment for crn P999999"
}
""".trimIndent()
