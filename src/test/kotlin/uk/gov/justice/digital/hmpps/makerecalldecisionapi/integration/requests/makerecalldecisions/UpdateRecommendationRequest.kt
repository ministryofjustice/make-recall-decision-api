package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

fun updateRecommendationRequest(status: Status = Status.DRAFT) = """
{
  "custodyStatus": {
    "selected": "YES_PRISON",
    "allOptions": [
      { "value": "YES_PRISON", "text": "Yes, prison custody" },
      { "value": "YES_POLICE", "text": "Yes, police custody" },
      { "value": "NO", "text": "No" }
    ]
  },
  "recallType": {
    "selected": {
      "value": "FIXED_TERM",
      "details": "My details"
    },
    "allOptions": [
      { "value": "FIXED_TERM", "text": "Fixed term" },
      { "value": "STANDARD", "text": "Standard" },
      { "value": "NO_RECALL", "text": "No recall" }
    ]
  },
  "responseToProbation": "They have not responded well",
  "isThisAnEmergencyRecall": true,
  "hasVictimsInContactScheme": {
    "selected": "YES",
    "allOptions": [
      { "value": "YES", "text": "Yes" },
      { "value": "NO", "text": "No" },
      { "value": "NOT_APPLICABLE", "text": "N/A" }
    ]
  },
  "dateVloInformed": "2022-08-01",
  "status": "$status"
}
""".trimIndent()
