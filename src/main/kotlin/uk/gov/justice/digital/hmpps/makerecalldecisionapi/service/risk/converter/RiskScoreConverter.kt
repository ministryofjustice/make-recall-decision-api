package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.risk.converter

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.*
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.DEFAULT_DATE_TIME_FOR_NULL_VALUE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.SCORE_NOT_APPLICABLE
import java.time.LocalDateTime

@Service
class RiskScoreConverter {

  /**
   * Convert a list of RiskScoreResponses to a PredictorScores object
   */
  fun convert(riskScoreResponses: List<RiskScoreResponse>): PredictorScores {
    val historicalScores = riskScoreResponses
      .sortedBy { it.completedDate ?: DEFAULT_DATE_TIME_FOR_NULL_VALUE }
      .reversed()
    val latestScore = historicalScores.firstOrNull()
    return PredictorScores(
      current = latestScore?.let { convert(it) },
      historical = historicalScores.mapNotNull { convert(it) },
    )
  }

  /**
   * Convert a RiskScoreResponse to a PredictorScore object
   */
  fun convert(riskScoreResponse: RiskScoreResponse): PredictorScore? {
    val scores = createScores(riskScoreResponse)
    return if (scores?.ogp == null
      && scores?.ogrs == null
      && scores?.ovp == null
      && scores?.rsr == null
      && scores?.ospc == null
      && scores?.ospi == null
    ) {
      null
    } else {
      PredictorScore(
        date = convertDateTimeStringToDateString(riskScoreResponse.completedDate),
        scores = scores,
      )
    }
  }

  private fun convertDateTimeStringToDateString(dateTime: String?): String? {
    return if (dateTime != null) {
      LocalDateTime.parse(dateTime).toLocalDate().toString()
    } else {
      null
    }
  }

  private fun createScores(riskScoreResponse: RiskScoreResponse): Scores? {
    val ospdc = buildLevelWithScore(
      riskScoreResponse.sexualPredictorScore?.ospDirectContactScoreLevel,
      riskScoreResponse.sexualPredictorScore?.ospDirectContactPercentageScore,
      "OSP/DC",
    )
    val ospiic = buildLevelWithScore(
      riskScoreResponse.sexualPredictorScore?.ospIndirectImageScoreLevel,
      riskScoreResponse.sexualPredictorScore?.ospIndirectImagePercentageScore,
      "OSP/IIC",
    )

    val ospc =
      if (ospdc != null) null
      else buildLevelWithScore(
        riskScoreResponse.sexualPredictorScore?.ospContactScoreLevel,
        riskScoreResponse.sexualPredictorScore?.ospContactPercentageScore,
        "OSP/C",
      )
    val ospi =
      if (ospiic != null) null
      else buildLevelWithScore(
        riskScoreResponse.sexualPredictorScore?.ospIndecentScoreLevel,
        riskScoreResponse.sexualPredictorScore?.ospIndecentPercentageScore,
        "OSP/I",
      )

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
        "OGRS",
      ),
      ogp = buildTwoYearScore(
        riskScoreResponse.generalPredictorScore?.ogpRisk,
        riskScoreResponse.generalPredictorScore?.ogp1Year,
        riskScoreResponse.generalPredictorScore?.ogp2Year,
        "OGP",
      ),
      ovp = buildTwoYearScore(
        riskScoreResponse.violencePredictorScore?.ovpRisk,
        riskScoreResponse.violencePredictorScore?.oneYear,
        riskScoreResponse.violencePredictorScore?.twoYears,
        "OVP",
      ),
    )
  }

  private fun rsrLevelWithScore(riskScoreResponse: RiskScoreResponse?): LevelWithScore? {
    val rsr = riskScoreResponse?.riskOfSeriousRecidivismScore
    val rsrScore = riskScoreResponse?.riskOfSeriousRecidivismScore
    return if (rsrScore?.scoreLevel == null && rsrScore?.percentageScore == null) {
      null
    } else {
      LevelWithScore(
        level = rsr?.scoreLevel,
        score = rsr?.percentageScore,
        type = "RSR",
      )
    }
  }

  private fun buildLevelWithScore(
    level: String?, percentageScore: String?, type: String?,
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
        score = if (arrayOf("OSP/I", "OSP/C", "OSP/IIC", "OSP/DC").contains(type)) null else percentageScore,
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