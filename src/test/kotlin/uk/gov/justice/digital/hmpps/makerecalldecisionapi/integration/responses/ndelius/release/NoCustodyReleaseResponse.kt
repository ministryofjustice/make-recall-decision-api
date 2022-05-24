package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses

fun noCustodyReleaseSummaryResponse() = """
{
    "status": 400,
    "developerMessage": "Expected offender 2500268412 to have a single custody related event but found 0 events"
}
""".trimIndent()
