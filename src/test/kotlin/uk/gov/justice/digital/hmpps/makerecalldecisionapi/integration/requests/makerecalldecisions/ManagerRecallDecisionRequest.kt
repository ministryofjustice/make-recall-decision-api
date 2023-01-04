package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun managerRecallDecisionRequest(decision: String = "NO_RECALL") = """
{
  "managerRecallDecision": {
    "selected": {
      "value": "$decision",
      "details": "details of recall selected"
    },
    "allOptions": [
      {
        "text": "Recall",
        "value": "RECALL"
      },
      {
        "text": "Do not recall",
        "value": "NO_RECALL"
      }
    ]
  }
}
""".trimIndent()
