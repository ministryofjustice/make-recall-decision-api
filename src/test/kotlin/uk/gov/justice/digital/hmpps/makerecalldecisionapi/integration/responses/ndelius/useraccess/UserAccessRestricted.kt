package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess

fun userAccessRestrictedResponse() = """
{
    "userRestricted": true,
    "userExcluded": false,
    "restrictionMessage": "You are restricted from viewing this offender record. Please contact OM Joe Bloggs"
}
""".trimIndent()
