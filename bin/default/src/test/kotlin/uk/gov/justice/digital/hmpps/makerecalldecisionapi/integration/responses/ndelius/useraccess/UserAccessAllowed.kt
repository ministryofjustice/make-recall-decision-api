package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.useraccess

fun userAccessAllowedResponse() = """
{
    "userRestricted": false,
    "userExcluded": false,
    "userNotFound": false
}
""".trimIndent()
