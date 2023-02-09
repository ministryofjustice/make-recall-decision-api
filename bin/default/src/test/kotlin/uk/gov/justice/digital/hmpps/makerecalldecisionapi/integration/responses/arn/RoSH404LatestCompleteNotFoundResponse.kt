package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun roSH404LatestCompleteNotFoundResponse(crn: String) = """
{
    "status": 404,
    "developerMessage": "Latest COMPLETE with types [LAYER_1, LAYER_3] type not found for crn, $crn"
}
""".trimIndent()
