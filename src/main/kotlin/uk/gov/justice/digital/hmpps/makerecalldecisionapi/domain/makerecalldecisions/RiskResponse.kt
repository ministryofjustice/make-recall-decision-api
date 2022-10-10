package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import java.time.LocalDate

data class RiskResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: RiskPersonalDetails? = null,
  val roshSummary: RoshSummary? = null,
  val mappa: Mappa? = null,
  val predictorScores: PredictorScores? = null,
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
  val error: String? = EMPTY_STRING,
  val current: TimelineDataPoint?,
  val historical: List<TimelineDataPoint?>?
)

data class TimelineDataPoint(
  val date: String?,
  val scores: Scores?
)

data class Scores(
  @JsonProperty("RSR")
  val rsr: RSR?,
  @JsonProperty("OSPC")
  val ospc: OSPC?,
  @JsonProperty("OSPI")
  val ospi: OSPI?,
  @JsonProperty("OGRS")
  val ogrs: OGRS?,
  @JsonProperty("OGP")
  val ogp: OGP?,
  @JsonProperty("OVP")
  val ovp: OVP?
)

data class OGP(
  val level: String?,
  val type: String?,
  val ogp1Year: String?,
  val ogp2Year: String?
)

data class OVP(
  val level: String?,
  val type: String?,
  val oneYear: String?,
  val twoYears: String?
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
  val type: String?,
  val oneYear: String?,
  val twoYears: String?
)

data class RoshSummary(
  val natureOfRisk: String? = null,
  val whoIsAtRisk: String? = null,
  val riskIncreaseFactors: String? = null,
  val riskMitigationFactors: String? = null,
  val riskImminence: String? = null,
  val riskOfSeriousHarm: RiskOfSeriousHarm? = null,
  val error: String? = null,
)
