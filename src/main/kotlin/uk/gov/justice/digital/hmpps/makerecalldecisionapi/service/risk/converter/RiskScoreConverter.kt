package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.risk.converter

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithTwoYearScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Scores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentScoresV1
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentScoresV2
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OGP
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OGRS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPDC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPI
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPIIC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OVP
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.RSR
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.DEFAULT_DATE_TIME_FOR_NULL_VALUE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.SCORE_NOT_APPLICABLE
import java.time.LocalDateTime

// TODO see (and answer) questions in RIskScoreResponse.kt and RiskResponse.kt before changing this class

@Service
class RiskScoreConverter {

  companion object {
    private val OSP_NAMES =
      arrayOf(OSPIIC, OSPDC, OSPC, OSPI).map { riskScoreType -> riskScoreType.printName }
  }

  /**
   * Convert a list of RiskScoreResponses to a PredictorScores object
   */
  fun convert(assessmentScores: List<AssessmentScores>): PredictorScores {
    val historicalScores = assessmentScores
      .sortedByDescending { it.completedDate ?: DEFAULT_DATE_TIME_FOR_NULL_VALUE }
    val latestScore = historicalScores.firstOrNull()
    return PredictorScores(
      current = latestScore?.let { convert(it) },
      historical = historicalScores.mapNotNull { convert(it) },
    )
  }

  /**
   * Convert a RiskScoreResponse to a PredictorScore object
   */
  fun convert(assessmentScores: AssessmentScores): PredictorScore? {
    val scores = createScores(assessmentScores)
    val date = convertDateTimeStringToDateString(assessmentScores.completedDate)

    // V1 rule: all V1 fields empty → null
    if (assessmentScores is AssessmentScoresV1 && scores?.isEmptyV1() == true) return null

    if (scores == null && date == null) return null

    return PredictorScore(
      date = date,
      scores = scores,
    )
  }

  private fun Scores.isEmptyV1(): Boolean = ogrs == null &&
    ogp == null &&
    ovp == null &&
    rsr == null &&
    ospc == null &&
    ospi == null

  private fun convertDateTimeStringToDateString(dateTime: String?): String? = if (dateTime != null) {
    LocalDateTime.parse(dateTime).toLocalDate().toString()
  } else {
    null
  }

  private fun createScores(assessmentScores: AssessmentScores): Scores? = when (assessmentScores) {
    is AssessmentScoresV1 -> createScoresFromV1(assessmentScores)
    is AssessmentScoresV2 -> createScoresFromV2(assessmentScores)
  }

  private fun createScoresFromV2(v2: AssessmentScoresV2): Scores? {
    val o = v2.output ?: return null

    return Scores(
      // V1 fields
      rsr = null,
      ospc = null,
      ospi = null,
      ospdc = null,
      ospiic = null,
      ogrs = null,
      ogp = null,
      ovp = null,

      // V2 fields
      allReoffendingPredictor = o.allReoffendingPredictor,
      violentReoffendingPredictor = o.violentReoffendingPredictor,
      seriousViolentReoffendingPredictor = o.seriousViolentReoffendingPredictor,
      directContactSexualReoffendingPredictor = o.directContactSexualReoffendingPredictor,
      indirectImageContactSexualReoffendingPredictor = o.indirectImageContactSexualReoffendingPredictor,
      combinedSeriousReoffendingPredictor = o.combinedSeriousReoffendingPredictor,
    )
  }

  private fun createScoresFromV1(v1: AssessmentScoresV1): Scores? {
    val output = v1.output ?: return null

    val sexual = output.sexualPredictorScore
    val ogr = output.groupReconvictionScore
    val ogp = output.generalPredictorScore
    val ovp = output.violencePredictorScore

    val ospdc = buildLevelWithScore(
      sexual?.ospDirectContactScoreLevel?.name,
      sexual?.ospDirectContactPercentageScore?.toString(),
      OSPDC.printName,
    )

    val ospiic = buildLevelWithScore(
      sexual?.ospIndirectImageScoreLevel?.name,
      sexual?.ospIndirectImagePercentageScore?.toString(),
      OSPIIC.printName,
    )

    val ospc =
      if (ospdc == null) {
        buildLevelWithScore(
          sexual?.ospContactScoreLevel?.name,
          sexual?.ospContactPercentageScore?.toString(),
          OSPC.printName,
        )
      } else {
        null
      }

    val ospi =
      if (ospiic == null) {
        buildLevelWithScore(
          sexual?.ospIndecentScoreLevel?.name,
          sexual?.ospIndecentPercentageScore?.toString(),
          OSPI.printName,
        )
      } else {
        null
      }

    return Scores(
      rsr = rsrLevelWithScore(v1),

      ospc = ospc,
      ospi = ospi,
      ospdc = ospdc,
      ospiic = ospiic,

      ogrs = buildTwoYearScore(
        ogr?.scoreLevel?.name,
        ogr?.oneYear?.toString(),
        ogr?.twoYears?.toString(),
        OGRS.printName,
      ),

      ogp = buildTwoYearScore(
        ogp?.ogpRisk?.name,
        ogp?.ogp1Year?.toString(),
        ogp?.ogp2Year?.toString(),
        OGP.printName,
      ),

      ovp = buildTwoYearScore(
        ovp?.ovpRisk?.name,
        ovp?.oneYear?.toString(),
        ovp?.twoYears?.toString(),
        OVP.printName,
      ),

      // V2 fields
      allReoffendingPredictor = null,
      violentReoffendingPredictor = null,
      seriousViolentReoffendingPredictor = null,
      directContactSexualReoffendingPredictor = null,
      indirectImageContactSexualReoffendingPredictor = null,
      combinedSeriousReoffendingPredictor = null,
    )
  }

  private fun rsrLevelWithScore(
    assessmentScores: AssessmentScores?,
  ): LevelWithScore? {
    val rsr = (assessmentScores as? AssessmentScoresV1)
      ?.output
      ?.riskOfSeriousRecidivismScore
      ?: return null

    if (rsr.scoreLevel == null && rsr.percentageScore == null) {
      return null
    }

    return LevelWithScore(
      level = rsr.scoreLevel?.name,
      score = rsr.percentageScore?.toString(),
      type = RSR.printName,
    )
  }

  private fun buildLevelWithScore(
    level: String?,
    percentageScore: String?,
    type: String?,
  ): LevelWithScore? {
    val scoreIsNull = level == null && percentageScore == null
    // TODO: level can never be SCORE_NOT_APPLICABLE anymore ???
    val notApplicableWithZeroPercentScorePresent =
      level.equals(SCORE_NOT_APPLICABLE, ignoreCase = true) && percentageScore == "0.0"
    val noScore = scoreIsNull || notApplicableWithZeroPercentScorePresent
    return if (noScore) {
      null
    } else {
      LevelWithScore(
        level = level,
        score = if (OSP_NAMES.contains(type)) null else percentageScore,
        type = type,
      )
    }
  }

  private fun buildTwoYearScore(
    level: String?,
    oneYear: String?,
    twoYears: String?,
    type: String?,
  ): LevelWithTwoYearScores? {
    val scoreEmpty = level == null && twoYears == null && oneYear == null
    return if (scoreEmpty) {
      null
    } else {
      LevelWithTwoYearScores(
        level = level,
        oneYear = oneYear,
        twoYears = twoYears,
        type = type,
      )
    }
  }
}
