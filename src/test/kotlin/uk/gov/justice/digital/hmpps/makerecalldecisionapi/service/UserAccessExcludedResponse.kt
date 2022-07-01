package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

fun excludedResponse() = """
{
  "userRestricted": false,
  "userExcluded": true,
  "exclusionMessage": "I am an exclusion message"
}
""".trimIndent()
