package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun managerRecallDecisionRequestWithIsSentToDeliusOnly(decision: String = "NO_RECALL") = """
{
  "managerRecallDecision": {
   "isSentToDelius": false
  }
}
""".trimIndent()
