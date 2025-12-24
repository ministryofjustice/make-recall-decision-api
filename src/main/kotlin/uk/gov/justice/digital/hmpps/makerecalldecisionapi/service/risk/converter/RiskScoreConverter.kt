package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.risk.converter

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithTwoYearScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Scores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
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
  fun convert(riskScoreResponses: List<AssessmentScores>): PredictorScores {
    val historicalScores = riskScoreResponses
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
  fun convert(riskScoreResponse: AssessmentScores): PredictorScore? {
    val scores = createScores(riskScoreResponse)
    return if (scores?.ogp == null &&
      scores?.ogrs == null &&
      scores?.ovp == null &&
      scores?.rsr == null &&
      scores?.ospc == null &&
      scores?.ospi == null
    ) {
      null
    } else {
      PredictorScore(
        date = convertDateTimeStringToDateString(riskScoreResponse.completedDate),
        scores = scores,
      )
    }
  }

  private fun convertDateTimeStringToDateString(dateTime: String?): String? = if (dateTime != null) {
    LocalDateTime.parse(dateTime).toLocalDate().toString()
  } else {
    null
  }

  private fun createScores(riskScoreResponse: AssessmentScores): Scores? {
    val ospdc = buildLevelWithScore(
      riskScoreResponse.sexualPredictorScore?.ospDirectContactScoreLevel,
      riskScoreResponse.sexualPredictorScore?.ospDirectContactPercentageScore,
      OSPDC.printName,
    )
    val ospiic = buildLevelWithScore(
      riskScoreResponse.sexualPredictorScore?.ospIndirectImageScoreLevel,
      riskScoreResponse.sexualPredictorScore?.ospIndirectImagePercentageScore,
      OSPIIC.printName,
    )

    val ospc =
      if (ospdc != null) {
        null
      } else {
        buildLevelWithScore(
          riskScoreResponse.sexualPredictorScore?.ospContactScoreLevel,
          riskScoreResponse.sexualPredictorScore?.ospContactPercentageScore,
          OSPC.printName,
        )
      }
    val ospi =
      if (ospiic != null) {
        null
      } else {
        buildLevelWithScore(
          riskScoreResponse.sexualPredictorScore?.ospIndecentScoreLevel,
          riskScoreResponse.sexualPredictorScore?.ospIndecentPercentageScore,
          OSPI.printName,
        )
      }

    return Scores(
      rsr = rsrLevelWithScore(riskScoreResponse),
      ospc = ospc,
      ospi = ospi,
      ospdc = ospdc,
      ospiic = ospiic,
      ogrs = buildTwoYearScore(
        riskScoreResponse.groupReconvictionScore?.scoreLevel,
        riskScoreResponse.groupReconvictionScore?.oneYear,
        riskScoreResponse.groupReconvictionScore?.twoYears,
        OGRS.printName,
      ),
      ogp = buildTwoYearScore(
        riskScoreResponse.generalPredictorScore?.ogpRisk,
        riskScoreResponse.generalPredictorScore?.ogp1Year,
        riskScoreResponse.generalPredictorScore?.ogp2Year,
        OGP.printName,
      ),
      ovp = buildTwoYearScore(
        riskScoreResponse.violencePredictorScore?.ovpRisk,
        riskScoreResponse.violencePredictorScore?.oneYear,
        riskScoreResponse.violencePredictorScore?.twoYears,
        OVP.printName,
      ),
    )
  }

  private fun rsrLevelWithScore(riskScoreResponse: AssessmentScores?): LevelWithScore? {
    val rsrScore = riskScoreResponse?.riskOfSeriousRecidivismScore
    return if (rsrScore?.scoreLevel == null && rsrScore?.percentageScore == null) {
      null
    } else {
      LevelWithScore(
        level = rsrScore.scoreLevel,
        score = rsrScore.percentageScore,
        type = RSR.printName,
      )
    }
  }

  private fun buildLevelWithScore(
    level: String?,
    percentageScore: String?,
    type: String?,
  ): LevelWithScore? {
    val scoreIsNull = level == null && percentageScore == null
    val notApplicableWithZeroPercentScorePresent =
      level.equals(SCORE_NOT_APPLICABLE, ignoreCase = true) && percentageScore == "0"
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
