package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun managerRecallDecisionRequestWithIsSentToDeliusOnly(isSentToDelius: String? = "false") = """
{
  "managerRecallDecision": {
   "isSentToDelius": $isSentToDelius
  }
}
""".trimIndent()
