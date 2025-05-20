package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess

fun userAccessExcludedResponse() = """
{
    "userRestricted": false,
    "userExcluded": true,
    "exclusionMessage": "You are excluded from viewing this offender record. Please contact OM Joe Bloggs"
}
""".trimIndent()
