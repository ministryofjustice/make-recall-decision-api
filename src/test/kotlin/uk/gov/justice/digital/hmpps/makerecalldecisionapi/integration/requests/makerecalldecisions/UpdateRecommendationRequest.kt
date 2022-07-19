package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun updateRecommendationRequest() = """
{
  "recallType": {
    "value": "FIXED_TERM",
    "options": [
      { "value": "FIXED_TERM", "text": "Fixed term" },
      { "value": "STANDARD", "text": "Standard" },
      { "value": "NO_RECALL", "text": "No recall" }
    ]
  }
}
""".trimIndent()
