package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.FourLevelRiskScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ThreeLevelRiskScoreLevel

fun historicalRiskScoresResponse() = """
[
  {
    "rsrPercentageScore": 18,
    "rsrScoreLevel": "${ThreeLevelRiskScoreLevel.HIGH}",
    "ospcPercentageScore": 6.2,
    "ospcScoreLevel": "${FourLevelRiskScoreLevel.LOW}",
    "ospiPercentageScore": 8.1,
    "ospiScoreLevel": "${ThreeLevelRiskScoreLevel.MEDIUM}",
    "calculatedDate": "2018-09-12T12:00:00.000",
    "completedDate": "2018-09-12T12:00:00.000",
    "signedDate": "2018-09-12T12:00:00.000"
  }
]
""".trimIndent()
