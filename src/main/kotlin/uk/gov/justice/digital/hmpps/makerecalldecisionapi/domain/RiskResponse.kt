package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class RiskResponse(
  val personalDetailsOverview: RiskPersonalDetails?,
  val riskOfSeriousHarm: RiskOfSeriousHarm?,
  val mappa: Mappa?,
  val predictorScores: PredictorScores?,
  val natureOfRisk: NatureOfRisk?,
  val contingencyPlan: ContingencyPlan?,
  val whoIsAtRisk: WhoIsAtRisk?,
  val circumstancesIncreaseRisk: CircumstancesIncreaseRisk?,
  val factorsToReduceRisk: FactorsToReduceRisk?,
  val whenRiskHighest: WhenRiskHighest?
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
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val lastUpdated: LocalDate?
)

data class Mappa(
  val level: String?,
  val isNominal: Boolean?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val lastUpdated: String?
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
  val score: Int?,
  val type: String?
)

data class OSPC(
  val level: String?,
  val score: Int?,
  val type: String?
)

data class OSPI(
  val level: String?,
  val score: Int?,
  val type: String?
)

data class OGRS(
  val level: String?,
  val score: Int?,
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
