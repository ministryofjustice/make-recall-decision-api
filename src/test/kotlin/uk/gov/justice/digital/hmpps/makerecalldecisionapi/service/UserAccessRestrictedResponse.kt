package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

fun restrictedResponse() = """
{
  "userRestricted": true,
  "userExcluded": false,
  "exclusionMessage": "I am a restriction message"
}
""".trimIndent()
