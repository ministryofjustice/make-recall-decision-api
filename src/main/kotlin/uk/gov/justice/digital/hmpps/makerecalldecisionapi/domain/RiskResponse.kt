package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import java.time.LocalDate

data class RiskResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: RiskPersonalDetails? = null,
  val riskOfSeriousHarm: RiskOfSeriousHarm? = null,
  val mappa: Mappa? = null,
  val predictorScores: PredictorScores? = null,
  val natureOfRisk: NatureOfRisk? = null,
  val contingencyPlan: ContingencyPlan? = null,
  val whoIsAtRisk: WhoIsAtRisk? = null,
  val circumstancesIncreaseRisk: CircumstancesIncreaseRisk? = null,
  val factorsToReduceRisk: FactorsToReduceRisk? = null,
  val whenRiskHighest: WhenRiskHighest? = null,
  val activeRecommendation: ActiveRecommendation? = null,
  val assessmentStatus: String? = null
)

data class RiskPersonalDetails(
  val name: String?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val dateOfBirth: LocalDate?,
  val age: Int?,
  val gender: String?,
  val crn: String?
)

data class RiskOfSeriousHarm(
  val overallRisk: String?,
  val riskToChildren: String?,
  val riskToPublic: String?,
  val riskToKnownAdult: String?,
  val riskToStaff: String?,
  val lastUpdated: String?
)

data class Mappa(
  val level: Int?,
  val isNominal: Boolean?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val lastUpdated: String?,
  val category: Int?,
)

data class PredictorScores(
  val current: Scores?,
  val historical: List<HistoricalScore>?
)

data class Scores(
  @JsonProperty("RSR")
  val rsr: RSR?,
  @JsonProperty("OSPC")
  val ospc: OSPC?,
  @JsonProperty("OSPI")
  val ospi: OSPI?,
  @JsonProperty("OGRS")
  val ogrs: OGRS?
)

data class RSR(
  val level: String?,
  val score: String?,
  val type: String?
)

data class OSPC(
  val level: String?,
  val score: String?,
  val type: String?
)

data class OSPI(
  val level: String?,
  val score: String?,
  val type: String?
)

data class OGRS(
  val level: String?,
  val score: String?,
  val type: String?
)

data class HistoricalScore(
  val date: String?,
  val scores: Scores?
)

data class NatureOfRisk(
  val description: String?,
  val oasysHeading: OasysHeading?
)

data class ContingencyPlan(
  val description: String?,
  val oasysHeading: OasysHeading?
)

data class WhoIsAtRisk(
  val description: String?,
  val oasysHeading: OasysHeading?
)

data class CircumstancesIncreaseRisk(
  val description: String?,
  val oasysHeading: OasysHeading?
)

data class FactorsToReduceRisk(
  val description: String?,
  val oasysHeading: OasysHeading?
)

data class WhenRiskHighest(
  val description: String?,
  val oasysHeading: OasysHeading?
)

data class OasysHeading(
  val number: String?,
  val description: String?
)
