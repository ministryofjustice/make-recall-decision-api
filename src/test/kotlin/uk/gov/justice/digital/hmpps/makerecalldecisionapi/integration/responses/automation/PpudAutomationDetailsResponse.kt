package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation

fun ppudAutomationDetailsResponse(id: String) = """
{
  "offender": {
    "id": "$id",
    "croOtherNumber": "string",
    "dateOfBirth": "2024-01-05",
    "ethnicity": "string",
    "familyName": "string",
    "firstNames": "string",
    "gender": "string",
    "immigrationStatus": "string",
    "nomsId": "string",
    "prisonerCategory": "string",
    "prisonNumber": "string",
    "sentences": [
      {
        "dateOfSentence": "2024-01-05",
        "custodyType": "string",
        "mappaLevel": "string"
      }
    ],
    "status": "string",
    "youngOffender": "string"
  }
}
""".trimIndent()
