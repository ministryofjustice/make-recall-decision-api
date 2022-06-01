package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses

fun releaseSummaryResponse() = """
{
    "lastRelease": {
        "date": "2017-09-15",
        "notes": "I am a note",
        "institution": {
            "institutionId": 361,
            "isEstablishment": true,
            "code": "XXX005",
            "description": "Addiewell",
            "institutionName": "Addiewell"
        },
        "reason": {
            "code": "ADL",
            "description": "Adult Licence"
        }
    },
    "lastRecall": {
      "date": "2020-10-15",
      "notes": "I am a second note",
      "reason": {
        "code": "ABC123",
        "description": "another reason description"
      }
    }
}
""".trimIndent()
