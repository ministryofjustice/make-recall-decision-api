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
    "establishment": "string",
    "nomsId": "string",
    "prisonerCategory": "string",
    "prisonNumber": "string",
    "sentences": [
      {
        "id": "string",      
        "dateOfSentence": "2024-01-05",
        "custodyType": "string",
        "mappaLevel": "string",
        "sentenceType": "string",
        "offenceDescription": "string",
        "sentenceExpiryDate": "2024-06-09",
        "dateOfSentence": "2024-01-05",                
        "licenceExpiryDate": "2024-01-05",
        "offence": {
          "indexOffence": "string",
          "dateOfIndexOffence": "2024-01-05"
        },
        "releases": [
          {
            "category": "string",
            "dateOfRelease": "2024-01-05",
            "releasedFrom": "string",
            "releasedUnder": "string",
            "releaseType": "string"
          }
        ],
        "sentenceLength": {
          "partYears": 1,
          "partMonths": 2,
          "partDays": 3
        },
        "sentencingCourt": "string"
      }
    ],
    "status": "string",
    "youngOffender": "string"
  }
}
""".trimIndent()
