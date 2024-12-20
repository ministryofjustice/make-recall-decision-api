package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspcScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.OspiScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RsrScoreLevel

fun historicalRiskScoresResponse() = """
[
  {
    "rsrPercentageScore": 18,
    "rsrScoreLevel": "${RsrScoreLevel.HIGH}",
    "ospcPercentageScore": 6.2,
    "ospcScoreLevel": "${OspcScoreLevel.LOW}",
    "ospiPercentageScore": 8.1,
    "ospiScoreLevel": "${OspiScoreLevel.MEDIUM}",
    "calculatedDate": "2018-09-12T12:00:00.000",
    "completedDate": "2018-09-12T12:00:00.000",
    "signedDate": "2018-09-12T12:00:00.000"
  }
]
""".trimIndent()
