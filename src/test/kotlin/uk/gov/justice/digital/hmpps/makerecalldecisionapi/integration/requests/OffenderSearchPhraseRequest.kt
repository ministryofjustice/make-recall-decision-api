package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests

fun offenderSearchByPhraseRequest(phrase: String) = """
  {
    "matchAllTerms": false,
    "phrase": "$phrase"
  }
""".trimIndent()
