package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

fun updateRecommendationRequestWithClearedValues(status: Status = Status.DRAFT) = """
{
  "hasVictimsInContactScheme": {
    "selected": "NO",
    "allOptions": [
      { "value": "YES", "text": "Yes" },
      { "value": "NO", "text": "No" },
      { "value": "NOT_APPLICABLE", "text": "N/A" }
    ]
  },
  "dateVloInformed": null,
  "hasArrestIssues": {
    "selected": false,
    "details": null
  },
  "hasContrabandRisk": {
    "selected": false,
    "details": null
  }
}
""".trimIndent()
