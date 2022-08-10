package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

fun updateRecommendationRequest(status: Status = Status.DRAFT) = """
{
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
  "custodyStatus": {
    "value": "YES_PRISON",
    "options": [
      { "value": "YES_PRISON", "text": "Yes, prison custody" },
      { "value": "YES_POLICE", "text": "Yes, police custody" },
      { "value": "NO", "text": "No" }
    ]
  },
  "status": "$status"
}
""".trimIndent()
