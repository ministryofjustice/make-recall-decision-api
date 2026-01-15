package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.RoshHistory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.CombinedPredictor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.FourBandStaticOrDynamicPredictor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ThreeBandStaticOrDynamicPredictor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import java.time.LocalDate

data class RiskResponse(
  val userAccessResponse: UserAccess? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val roshSummary: RoshSummary? = null,
  val roshHistory: RoshHistory? = null,
  val mappa: Mappa? = null,
  val predictorScores: PredictorScores? = null,
  val activeRecommendation: ActiveRecommendation? = null,
  val assessmentStatus: String? = null,
)

data class RiskOfSeriousHarm(
  val overallRisk: String?,
  val riskInCustody: RiskTo?,
  val riskInCommunity: RiskTo?,
)

data class RiskTo(
  val riskToChildren: String?,
  val riskToPublic: String?,
  val riskToKnownAdult: String?,
  val riskToStaff: String?,
  val riskToPrisoners: String?,
)

data class Mappa(
  val level: Int? = null,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val lastUpdatedDate: LocalDate? = null,
  val category: Int? = null,
  val error: String? = null,
  val hasBeenReviewed: Boolean? = false,
)

data class PredictorScores(
  val error: String? = EMPTY_STRING,
  val current: PredictorScore?,
  val historical: List<PredictorScore?>?,
)

data class PredictorScore(
  val date: String?,
  val scores: Scores?,
)

data class Scores(
  // V1 assessment scores
  @JsonProperty("RSR")
  val rsr: LevelWithScore?,
  @JsonProperty("OSPC")
  val ospc: LevelWithScore?,
  @JsonProperty("OSPI")
  val ospi: LevelWithScore?,
  @JsonProperty("OSPDC")
  val ospdc: LevelWithScore?,
  @JsonProperty("OSPIIC")
  val ospiic: LevelWithScore?,
  @JsonProperty("OGRS")
  val ogrs: LevelWithTwoYearScores?,
  @JsonProperty("OGP")
  val ogp: LevelWithTwoYearScores?,
  @JsonProperty("OVP")
  val ovp: LevelWithTwoYearScores?,

  // V2 assessment scores
  val allReoffendingPredictor: FourBandStaticOrDynamicPredictor?,
  val violentReoffendingPredictor: FourBandStaticOrDynamicPredictor?,
  val seriousViolentReoffendingPredictor: FourBandStaticOrDynamicPredictor?,
  val directContactSexualReoffendingPredictor: FourBandStaticOrDynamicPredictor?,
  val indirectImageContactSexualReoffendingPredictor: ThreeBandStaticOrDynamicPredictor?,
  val combinedSeriousReoffendingPredictor: CombinedPredictor?,
)

data class LevelWithScore(
  val level: String?,
  val type: String?,
  val score: Double?,
)

data class LevelWithTwoYearScores(
  val level: String?,
  val type: String?,
  val oneYear: String?,
  val twoYears: String?,
)

data class RoshSummary(
  val natureOfRisk: String? = null,
  val whoIsAtRisk: String? = null,
  val riskIncreaseFactors: String? = null,
  val riskMitigationFactors: String? = null,
  val riskImminence: String? = null,
  val riskOfSeriousHarm: RiskOfSeriousHarm? = null,
  val lastUpdatedDate: String? = null,
  val error: String? = null,
)
