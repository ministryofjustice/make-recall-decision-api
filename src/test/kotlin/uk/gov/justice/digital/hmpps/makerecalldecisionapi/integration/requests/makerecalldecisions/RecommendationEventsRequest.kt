package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

internal fun recommendationEventsRequest(crn: String) = """
  {
    "crn": "$crn",
    "userId": "Bill",
    "timeStamp": "2022-09-12T15:00:08",
    "eventType": "SEARCH_RESULT_CLICKED"
  }
""".trimIndent()
