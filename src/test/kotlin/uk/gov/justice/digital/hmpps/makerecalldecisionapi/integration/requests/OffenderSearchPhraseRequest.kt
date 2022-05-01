package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests

fun offenderSearchByPhraseRequest(phrase: String) = """
  {
    "matchAllTerms": false,
    "phrase": "$phrase",
    "probationAreasFilter": "[\"N01\",\"N02\"]" 
  }
""".trimIndent()
//TODO what is probationAreasFilter??