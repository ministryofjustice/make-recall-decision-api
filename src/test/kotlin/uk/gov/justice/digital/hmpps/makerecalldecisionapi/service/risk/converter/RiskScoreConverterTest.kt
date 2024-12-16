package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.risk.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.*
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.*
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.SCORE_NOT_APPLICABLE
import java.time.LocalDateTime

class RiskScoreConverterTest {

  private val converter = RiskScoreConverter()

  @Test
  fun `converts RiskScoreResponse to PredictorScore`() {
    val riskScoreResponse = riskScoreResponse()
    val expectedPredictorScore = expectedPredictorScoreFrom(riskScoreResponse)

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  @Test
  fun `converts RiskScoreResponse with null date and score fields to null PredictorScore`() {
    val riskScoreResponse = RiskScoreResponse(null, null, null, null, null, null)

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isNull()
  }

  @Test
  fun `converts RiskScoreResponse with non-null date and null score fields to null PredictorScore`() {
    val riskScoreResponse = riskScoreResponseWithNullScoreFields()

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isNull()
  }

  @Test
  fun `converts RiskScoreResponse with non-null date and null and zero, non-applicable scores to null PredictorScore`() {
    val riskScoreResponse = riskScoreResponseWithNullScoreFields().copy(
      sexualPredictorScore = sexualPredictorScore(
        ospContactPercentageScore = "0",
        ospContactScoreLevel = SCORE_NOT_APPLICABLE,
        ospIndecentPercentageScore = "0",
        ospIndecentScoreLevel = SCORE_NOT_APPLICABLE,
      ),
    )

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isNull()
  }

  @Test
  fun `converts RiskScoreResponse with some non-null values to non-null PredictorScore`() {
    val riskScoreResponse = riskScoreResponse(
      riskOfSeriousRecidivismScore = null,
      generalPredictorScore = null,
    )
    val intermediatePredictorScore = expectedPredictorScoreFrom(riskScoreResponse)
    val expectedPredictorScore = PredictorScore(
      date = intermediatePredictorScore.date,
      scores = Scores(
        rsr = null,
        ogp = null,
        ospc = intermediatePredictorScore.scores?.ospc,
        ospi = intermediatePredictorScore.scores?.ospi,
        ogrs = intermediatePredictorScore.scores?.ogrs,
        ovp = intermediatePredictorScore.scores?.ovp,
      ),
    )


    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  @Test
  fun `converts RiskScoreResponse with null date to PredictorScore with null date`() {
    val riskScoreResponse = riskScoreResponse(completedDate = null)
    val expectedPredictorScore = expectedPredictorScoreFrom(riskScoreResponse)

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  @Test
  fun `converts multiple RiskScoreResponses to PredictorScore`() {
    val latestDate = randomLocalDateTime()
    val latestRiskScoreResponse = riskScoreResponse(completedDate = latestDate.toString())
    val nullDateRiskScoreResponse = riskScoreResponse(completedDate = null)
    val yearOldRiskScoreResponse = riskScoreResponse(completedDate = latestDate.minusYears(1).toString())
    val riskScoreResponseWithNulls = RiskScoreResponse(null, null, null, null, null, null)
    val twoYearOldRiskScoreResponse = riskScoreResponse(completedDate = latestDate.minusYears(2).toString())
    val anotherNullDateRiskScoreResponse = riskScoreResponse(completedDate = null)
    val riskScoreResponseList = listOf(
      latestRiskScoreResponse,
      yearOldRiskScoreResponse,
      riskScoreResponseWithNulls, // this one should disappear from the final result
      twoYearOldRiskScoreResponse,
      nullDateRiskScoreResponse,
      anotherNullDateRiskScoreResponse,
    )

    val expectedScoresWithNonNullDates = listOf(
      expectedPredictorScoreFrom(latestRiskScoreResponse),
      expectedPredictorScoreFrom(yearOldRiskScoreResponse),
      expectedPredictorScoreFrom(twoYearOldRiskScoreResponse),
    )
    val expectedScoresWithNullDates = setOf(
      expectedPredictorScoreFrom(nullDateRiskScoreResponse),
      expectedPredictorScoreFrom(anotherNullDateRiskScoreResponse),
    )

    val actualPredictorScores = converter.convert(riskScoreResponseList)

    assertThat(actualPredictorScores.current).isEqualTo(expectedPredictorScoreFrom(latestRiskScoreResponse))
    assertThat(actualPredictorScores.historical).hasSize(5)
    assertThat(actualPredictorScores.historical?.subList(0, 3)).isEqualTo(expectedScoresWithNonNullDates)
    // the scores with null dates should be at the end, but we don't care in what order, so we compare as sets
    assertThat(actualPredictorScores.historical?.subList(3, 5)?.toSet()).isEqualTo(expectedScoresWithNullDates)
  }

  @Test
  fun `converts empty list of RiskScoreResponses to empty PredictorScores`() {
    val expectedPredictorScore = PredictorScores(
      current = null,
      historical = emptyList(),
    )

    val actualPredictorScore = converter.convert(emptyList())

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  @Test
  fun `converts list with RiskScoreResponse with null fields to empty PredictorScores`() {
    val riskScoreResponse = RiskScoreResponse(null, null, null, null, null, null)
    val expectedPredictorScore = PredictorScores(
      current = null,
      historical = emptyList(),
    )

    val actualPredictorScore = converter.convert(listOf(riskScoreResponse))

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  @Test
  fun `converts list with RiskScoreResponses with null score fields to empty PredictorScores`() {
    val expectedPredictorScore = PredictorScores(
      current = null,
      historical = emptyList(),
    )

    val actualPredictorScore =
      converter.convert(listOf(riskScoreResponseWithNullScoreFields(), riskScoreResponseWithNullScoreFields()))

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  private fun expectedPredictorScoreFrom(riskScoreResponse: RiskScoreResponse) =
    PredictorScore(
      date = riskScoreResponse.completedDate?.let { LocalDateTime.parse(it).toLocalDate().toString() },
      scores = Scores(
        rsr = LevelWithScore(
          level = riskScoreResponse.riskOfSeriousRecidivismScore?.scoreLevel,
          type = "RSR",
          score = riskScoreResponse.riskOfSeriousRecidivismScore?.percentageScore,
        ),
        ospc = LevelWithScore(
          level = riskScoreResponse.sexualPredictorScore?.ospContactScoreLevel,
          type = "OSP/C",
          score = null,
        ),
        ospi = LevelWithScore(
          level = riskScoreResponse.sexualPredictorScore?.ospIndecentScoreLevel,
          type = "OSP/I",
          score = null,
        ),
        ogrs = LevelWithTwoYearScores(
          level = riskScoreResponse.groupReconvictionScore?.scoreLevel,
          type = "OGRS",
          oneYear = riskScoreResponse.groupReconvictionScore?.oneYear,
          twoYears = riskScoreResponse.groupReconvictionScore?.twoYears,
        ),
        ogp = LevelWithTwoYearScores(
          level = riskScoreResponse.generalPredictorScore?.ogpRisk,
          type = "OGP",
          oneYear = riskScoreResponse.generalPredictorScore?.ogp1Year,
          twoYears = riskScoreResponse.generalPredictorScore?.ogp2Year,
        ),
        ovp = LevelWithTwoYearScores(
          level = riskScoreResponse.violencePredictorScore?.ovpRisk,
          type = "OVP",
          oneYear = riskScoreResponse.violencePredictorScore?.oneYear,
          twoYears = riskScoreResponse.violencePredictorScore?.twoYears,
        ),
      ),
    )

  private fun riskScoreResponseWithNullScoreFields() = riskScoreResponse(
    riskOfSeriousRecidivismScore = riskOfSeriousRecidivismScore(
      percentageScore = null,
      scoreLevel = null,
    ),
    sexualPredictorScore = sexualPredictorScore(
      ospContactPercentageScore = null,
      ospContactScoreLevel = null,
      ospIndecentPercentageScore = null,
      ospIndecentScoreLevel = null,
    ),
    groupReconvictionScore = groupReconvictionScore(
      oneYear = null,
      twoYears = null,
      scoreLevel = null,
    ),
    generalPredictorScore = generalPredictorScore(
      ogpRisk = null,
      ogp1Year = null,
      ogp2Year = null,
    ),
    violencePredictorScore = violencePredictorScore(
      ovpRisk = null,
      oneYear = null,
      twoYears = null,
    ),
  )
}