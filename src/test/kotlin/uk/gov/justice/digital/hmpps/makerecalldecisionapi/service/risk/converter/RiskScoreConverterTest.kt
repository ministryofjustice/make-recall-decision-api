package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.risk.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithTwoYearScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Scores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentScoresV1
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentScoresV2
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.CombinedPredictor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.FourBandPredictor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.FourBandRiskScoreBand
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.FourLevelRiskScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OGP
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OGRS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPDC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPI
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPIIC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OVP
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.RSR
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.StaticOrDynamic
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.StaticOrDynamicPredictor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ThreeBandPredictor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ThreeLevelRiskScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.assessmentScoresV1
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.assessmentScoresV2
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.generalPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.groupReconvictionScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.outputV1
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.riskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.sexualPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.violencePredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime

class RiskScoreConverterTest {

  private val converter = RiskScoreConverter()

  @Test
  fun `converts RiskScoreResponse with non-null ospdc and ospiic to PredictorScore`() {
    val assessmentScore = assessmentScoresV1()
    val expectedPredictorScore = expectedPredictorScoreFrom(assessmentScore)

    val actualPredictorScore = converter.convert(assessmentScore)

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  @Test
  fun `converts RiskScoreResponse with null ospdc and ospiic to PredictorScore`() {
    val riskScoreResponse = assessmentScoresV1(
      output = outputV1(
        sexualPredictorScore = sexualPredictorScore(
          ospIndirectImagePercentageScore = null,
          ospDirectContactPercentageScore = null,
          ospIndirectImageScoreLevel = null,
          ospDirectContactScoreLevel = null,
        ),
      ),
    )
    val expectedPredictorScore = expectedPredictorScoreFrom(riskScoreResponse)

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  @Test
  fun `converts RiskScoreResponse with null date and score fields to null PredictorScore`() {
    val assessmentScore = AssessmentScoresV1(null, null, null, null)

    val actualPredictorScore = converter.convert(assessmentScore)

    assertThat(actualPredictorScore).isNull()
  }

  @Test
  fun `converts RiskScoreResponse with non-null date and null score fields to null PredictorScore`() {
    val riskScoreResponse = riskScoreResponseWithNullScoreFields()

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isNull()
  }

  @Test
  fun `converts RiskScoreResponse with non-null date and null and zero, non-applicable old sexual predictor scores to null PredictorScore`() {
    val riskScore = riskScoreResponseWithNullScoreFields()

    val riskScoreResponse = riskScore.copy(
      output = riskScore.output?.copy(
        sexualPredictorScore = sexualPredictorScore(
          ospDirectContactScoreLevel = null,
          ospDirectContactPercentageScore = null,
          ospIndirectImageScoreLevel = null,
          ospIndirectImagePercentageScore = null,
          ospContactPercentageScore = 0.0,
          ospContactScoreLevel = FourLevelRiskScoreLevel.NOT_APPLICABLE,
          ospIndecentPercentageScore = 0.0,
          ospIndecentScoreLevel = ThreeLevelRiskScoreLevel.NOT_APPLICABLE,
        ),
      ),
    )

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isNull()
  }

  @Test
  fun `converts RiskScoreResponse with non-null date and null and zero, non-applicable new sexual predictor scores to null PredictorScore`() {
    val riskScore = riskScoreResponseWithNullScoreFields()

    val riskScoreResponse = riskScore.copy(
      output = riskScore.output?.copy(
        sexualPredictorScore = sexualPredictorScore(
          ospIndecentPercentageScore = null,
          ospContactPercentageScore = null,
          ospIndecentScoreLevel = null,
          ospContactScoreLevel = null,
          ospDirectContactPercentageScore = 0.0,
          ospDirectContactScoreLevel = FourLevelRiskScoreLevel.NOT_APPLICABLE,
          ospIndirectImagePercentageScore = 0.0,
          ospIndirectImageScoreLevel = ThreeLevelRiskScoreLevel.NOT_APPLICABLE,
        ),
      ),
    )

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isNull()
  }

  @Test
  fun `converts RiskScoreResponse with some non-null values to non-null PredictorScore`() {
    val riskScoreResponse = assessmentScoresV1(
      output = outputV1().copy(
        riskOfSeriousRecidivismScore = null,
        generalPredictorScore = null,
      ),
    )
    val intermediatePredictorScore = expectedPredictorScoreFrom(riskScoreResponse)
    val expectedPredictorScore = PredictorScore(
      date = intermediatePredictorScore.date,
      scores = Scores(
        rsr = null,
        ogp = null,
        ospc = intermediatePredictorScore.scores?.ospc,
        ospi = intermediatePredictorScore.scores?.ospi,
        ospdc = intermediatePredictorScore.scores?.ospdc,
        ospiic = intermediatePredictorScore.scores?.ospiic,
        ogrs = intermediatePredictorScore.scores?.ogrs,
        ovp = intermediatePredictorScore.scores?.ovp,
        allReoffendingPredictor = null,
        violentReoffendingPredictor = null,
        seriousViolentReoffendingPredictor = null,
        directContactSexualReoffendingPredictor = null,
        indirectImageContactSexualReoffendingPredictor = null,
        combinedSeriousReoffendingPredictor = null,
      ),
    )

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  @Test
  fun `converts RiskScoreResponse with null date to PredictorScore with null date`() {
    val riskScoreResponse = assessmentScoresV1(completedDate = null)
    val expectedPredictorScore = expectedPredictorScoreFrom(riskScoreResponse)

    val actualPredictorScore = converter.convert(riskScoreResponse)

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  @Test
  fun `converts multiple RiskScoreResponses to PredictorScore`() {
    val latestDate = randomLocalDateTime()
    val latestRiskScoreResponse = assessmentScoresV1(completedDate = latestDate.toString())
    val nullDateRiskScoreResponse = assessmentScoresV1(completedDate = null)
    val yearOldRiskScoreResponse = assessmentScoresV1(completedDate = latestDate.minusYears(1).toString())
    val riskScoreResponseWithNulls = AssessmentScoresV1(null, null, null, null)
    val twoYearOldRiskScoreResponse = assessmentScoresV1(completedDate = latestDate.minusYears(2).toString())
    val anotherNullDateRiskScoreResponse = assessmentScoresV1(completedDate = null)
    val riskScoreResponseList = listOf(
      latestRiskScoreResponse,
      yearOldRiskScoreResponse,
      // this one with nulls should disappear from the final result
      riskScoreResponseWithNulls,
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
    val riskScoreResponse = AssessmentScoresV1(null, null, null, null)
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

  @Test
  fun `converts RiskScoreResponses with outputVersion 2 correctly`() {
    val assessmentScoresV2 = assessmentScoresV2()
    val expectedPredictorScore = expectedPredictorScoreFrom(assessmentScoresV2)

    val actualPredictorScore =
      converter.convert(assessmentScoresV2)

    assertThat(actualPredictorScore).isEqualTo(expectedPredictorScore)
  }

  private fun expectedPredictorScoreFrom(riskScoreResponse: AssessmentScoresV1): PredictorScore {
    val sexualPredictorScore = riskScoreResponse.output?.sexualPredictorScore
    val riskOfSeriousRecidivismScore = riskScoreResponse.output?.riskOfSeriousRecidivismScore
    val groupReconvictionScore = riskScoreResponse.output?.groupReconvictionScore
    val generalPredictorScore = riskScoreResponse.output?.generalPredictorScore
    val violencePredictorScore = riskScoreResponse.output?.violencePredictorScore
    val hasOspdc = sexualPredictorScore?.ospDirectContactScoreLevel != null

    val ospc: LevelWithScore?
    val ospdc: LevelWithScore?
    if (hasOspdc) {
      ospc = null
      ospdc = LevelWithScore(
        level = sexualPredictorScore.ospDirectContactScoreLevel.toString(),
        type = OSPDC.printName,
        score = null,
      )
    } else {
      ospc = LevelWithScore(
        level = sexualPredictorScore?.ospContactScoreLevel.toString(),
        type = OSPC.printName,
        score = null,
      )
      ospdc = null
    }

    val hasOspiic = sexualPredictorScore?.ospDirectContactScoreLevel != null
    val ospi: LevelWithScore?
    val ospiic: LevelWithScore?
    if (hasOspiic) {
      ospi = null
      ospiic = LevelWithScore(
        level = sexualPredictorScore.ospIndirectImageScoreLevel.toString(),
        type = OSPIIC.printName,
        score = null,
      )
    } else {
      ospi = LevelWithScore(
        level = sexualPredictorScore?.ospIndecentScoreLevel.toString(),
        type = OSPI.printName,
        score = null,
      )
      ospiic = null
    }

    return PredictorScore(
      date = riskScoreResponse.completedDate,
      scores = Scores(
        rsr = LevelWithScore(
          level = riskOfSeriousRecidivismScore?.scoreLevel.toString(),
          type = RSR.printName,
          score = riskOfSeriousRecidivismScore?.percentageScore,
        ),
        ospc = ospc,
        ospi = ospi,
        ospdc = ospdc,
        ospiic = ospiic,
        ogrs = LevelWithTwoYearScores(
          level = groupReconvictionScore?.scoreLevel.toString(),
          type = OGRS.printName,
          oneYear = groupReconvictionScore?.oneYear.toString(),
          twoYears = groupReconvictionScore?.twoYears.toString(),
        ),
        ogp = LevelWithTwoYearScores(
          level = generalPredictorScore?.ogpRisk.toString(),
          type = OGP.printName,
          oneYear = generalPredictorScore?.ogp1Year.toString(),
          twoYears = generalPredictorScore?.ogp2Year.toString(),
        ),
        ovp = LevelWithTwoYearScores(
          level = violencePredictorScore?.ovpRisk.toString(),
          type = OVP.printName,
          oneYear = violencePredictorScore?.oneYear.toString(),
          twoYears = violencePredictorScore?.twoYears.toString(),
        ),
        null,
        null,
        null,
        null,
        null,
        null,
      ),
    )
  }

  private fun expectedPredictorScoreFrom(
    riskScoreResponse: AssessmentScoresV2,
  ): PredictorScore {
    val output = riskScoreResponse.output

    return PredictorScore(
      date = riskScoreResponse.completedDate,

      scores = Scores(
        // V1 fields (all null for V2)
        rsr = null,
        ospc = null,
        ospi = null,
        ospdc = null,
        ospiic = null,
        ogrs = null,
        ogp = null,
        ovp = null,

        // ─────────────────────────────
        // V2 fields
        // ─────────────────────────────
        allReoffendingPredictor =
        output?.allReoffendingPredictor?.let {
          StaticOrDynamicPredictor(
            score = it.score,
            band = it.band,
            staticOrDynamic = it.staticOrDynamic,
          )
        },

        violentReoffendingPredictor =
        output?.violentReoffendingPredictor?.let {
          StaticOrDynamicPredictor(
            score = it.score,
            band = it.band,
            staticOrDynamic = it.staticOrDynamic,
          )
        },

        seriousViolentReoffendingPredictor =
        output?.seriousViolentReoffendingPredictor?.let {
          StaticOrDynamicPredictor(
            score = it.score,
            band = it.band,
            staticOrDynamic = it.staticOrDynamic,
          )
        },

        directContactSexualReoffendingPredictor =
        output?.directContactSexualReoffendingPredictor?.let {
          FourBandPredictor(
            score = it.score,
            band = it.band,
          )
        },

        indirectImageContactSexualReoffendingPredictor =
        output?.indirectImageContactSexualReoffendingPredictor?.let {
          ThreeBandPredictor(
            score = it.score,
            band = it.band,
          )
        },

        combinedSeriousReoffendingPredictor =
        output?.combinedSeriousReoffendingPredictor?.let {
          CombinedPredictor(
            score = it.score ?: 0.0,
            band = it.band ?: FourBandRiskScoreBand.LOW,
            staticOrDynamic = it.staticOrDynamic ?: StaticOrDynamic.STATIC,
            algorithmVersion = it.algorithmVersion ?: "unknown",
          )
        },
      ),
    )
  }

  private fun riskScoreResponseWithNullScoreFields() = assessmentScoresV1(
    output = outputV1(
      riskOfSeriousRecidivismScore = riskOfSeriousRecidivismScore(
        percentageScore = null,
        scoreLevel = null,
      ),
      sexualPredictorScore = sexualPredictorScore(
        ospContactPercentageScore = null,
        ospContactScoreLevel = null,
        ospIndecentPercentageScore = null,
        ospIndecentScoreLevel = null,
        ospIndirectImagePercentageScore = null,
        ospDirectContactPercentageScore = null,
        ospIndirectImageScoreLevel = null,
        ospDirectContactScoreLevel = null,
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
    ),
  )
}
