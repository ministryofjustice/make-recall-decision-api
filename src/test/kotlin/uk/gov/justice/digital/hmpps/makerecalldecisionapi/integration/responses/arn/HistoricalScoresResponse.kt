package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun historicalRiskScoresResponse() = """
[
  {
    "rsrPercentageScore": 18,
    "rsrScoreLevel": "HIGH",
    "ospcPercentageScore": 6.8,
    "ospcScoreLevel": "LOW",
    "ospiPercentageScore": 8.1,
    "ospiScoreLevel": "MEDIUM",
    "calculatedDate": "2018-09-12T12:00:00.000Z",
    "completedDate": "2018-09-12T12:00:00.000Z",
    "signedDate": "2018-09-12T12:00:00.000Z"
  }
]
""".trimIndent()
