package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun roSH404NoOffenderFoundResponse(crn: String) = """
{
    "status": 404,
    "developerMessage": "Offender not found for CRN, $crn"
}
""".trimIndent()
