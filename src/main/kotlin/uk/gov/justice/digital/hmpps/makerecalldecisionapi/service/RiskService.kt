package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.CircumstancesIncreaseRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ContingencyPlan
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.FactorsToReduceRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.HistoricalScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.NatureOfRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OGRS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OSPC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OSPI
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.OasysHeading
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.PredictorScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RSR
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskOfSeriousHarm
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskPersonalDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Scores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.WhenRiskHighest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.WhoIsAtRisk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.Assessment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ContingencyPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.CurrentScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.HistoricalScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.SexualPredictorScore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Service
internal class RiskService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  @Qualifier("assessRisksNeedsApiClientUserEnhanced") private val arnApiClient: ArnApiClient,
  private val userAccessValidator: UserAccessValidator,
  private val recommendationService: RecommendationService

) {
  suspend fun getRisk(crn: String): RiskResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      RiskResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = fetchPersonalDetails(crn)
      val riskSummaryResponse = handleFetchRiskSummary(crn)
      val riskOfSeriousHarm = extractRiskOfSeriousHarm(riskSummaryResponse)
      val natureOfRisk = extractNatureOfRisk(riskSummaryResponse)
      val whoIsAtRisk = extractWhoIsAtRisk(riskSummaryResponse)
      val circumstancesIncreaseRisk = extractCircumstancesIncreaseRisk(riskSummaryResponse)
      val factorsToReduceRisk = extractFactorsToReduceRisk(riskSummaryResponse)
      val whenRiskHighest = extractWhenRiskHighest(riskSummaryResponse)
      val mappa = handleFetchMappaApiCall(crn)
      val predictorScores = PredictorScores(
        current = fetchCurrentScores(crn),
        historical = fetchHistoricalScores(crn)
      )
      val contingencyPlan = null // fetchContingencyPlan(crn) // TODO reintroduce once ARN-1026 is complete
      val recommendationDetails = recommendationService.getDraftRecommendationForCrn(crn)

      return RiskResponse(
        personalDetailsOverview = personalDetailsOverview,
        riskOfSeriousHarm = riskOfSeriousHarm,
        mappa = mappa,
        predictorScores = predictorScores,
        natureOfRisk = natureOfRisk,
        contingencyPlan = contingencyPlan,
        whoIsAtRisk = whoIsAtRisk,
        circumstancesIncreaseRisk = circumstancesIncreaseRisk,
        factorsToReduceRisk = factorsToReduceRisk,
        whenRiskHighest = whenRiskHighest,
        activeRecommendation = recommendationDetails
      )
    }
  }

  private suspend fun fetchContingencyPlan(crn: String): ContingencyPlan {
    val contingencyPlanResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getContingencyPlan(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No contingency plan available for CRN: $crn - ${e.message}")
      ContingencyPlanResponse(assessments = emptyList())
    }
    val latestCompletedAssessment = contingencyPlanResponse.assessments
      ?.filter { it?.assessmentStatus.equals("COMPLETE") }
      ?.maxByOrNull { LocalDateTime.parse(it?.dateCompleted) }
    return ContingencyPlan(
      description = formatContingencyPlanDetails(latestCompletedAssessment),
      oasysHeading = OasysHeading(
        number = "10.1",
        description = "Contingency plan"
      )
    )
  }

  private fun formatContingencyPlanDetails(latestCompletedAssessment: Assessment?): String {
    val keyConsiderationsCurrentSituation = latestCompletedAssessment?.keyConsiderationsCurrentSituation ?: ""
    val furtherConsiderationsCurrentSituation = latestCompletedAssessment?.furtherConsiderationsCurrentSituation ?: ""
    val supervision = latestCompletedAssessment?.supervision ?: ""
    val monitoringAndControl = latestCompletedAssessment?.monitoringAndControl ?: ""
    val interventionsAndTreatment = latestCompletedAssessment?.interventionsAndTreatment ?: ""
    val victimSafetyPlanning = latestCompletedAssessment?.victimSafetyPlanning ?: ""
    val contingencyPlans = latestCompletedAssessment?.contingencyPlans ?: ""
    return keyConsiderationsCurrentSituation + furtherConsiderationsCurrentSituation + supervision + monitoringAndControl + interventionsAndTreatment + victimSafetyPlanning + contingencyPlans
  }

  private suspend fun fetchCurrentScores(crn: String): Scores {
    val currentScoresResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getCurrentScores(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No current scores available for CRN: $crn - ${e.message}")
      listOf(
        CurrentScoreResponse(
          completedDate = "",
          generalPredictorScore = GeneralPredictorScore(ogpStaticWeightedScore = "", ogpDynamicWeightedScore = "", ogpTotalWeightedScore = "", ogpRisk = ""),
          riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "", scoreLevel = ""),
          sexualPredictorScore = SexualPredictorScore(ospIndecentPercentageScore = "", ospContactPercentageScore = "", ospIndecentScoreLevel = "", ospContactScoreLevel = "")
        )
      )
    }
    val latestScores = currentScoresResponse.maxByOrNull { LocalDateTime.parse(it.completedDate) }
    val rsr = latestScores?.riskOfSeriousRecidivismScore
    val osp = latestScores?.sexualPredictorScore
    val osg = latestScores?.generalPredictorScore
    return Scores(
      rsr = RSR(level = rsr?.scoreLevel ?: "", score = rsr?.percentageScore ?: "", type = "RSR"),
      ospc = OSPC(level = osp?.ospContactScoreLevel ?: "", score = osp?.ospContactPercentageScore ?: "", type = "OSP/C"),
      ospi = OSPI(level = osp?.ospIndecentScoreLevel ?: "", score = osp?.ospIndecentPercentageScore ?: "", type = "OSP/I"),
      ogrs = OGRS(level = osg?.ogpRisk ?: "", score = osg?.ogpTotalWeightedScore, type = "OGRS")
    )
  }

  private suspend fun fetchHistoricalScores(crn: String): List<HistoricalScore> {
    val historicalScoresResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getHistoricalScores(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No historical scores available for CRN: $crn - ${e.message}")
      listOf(
        HistoricalScoreResponse(
          rsrPercentageScore = "",
          rsrScoreLevel = "",
          ospcPercentageScore = "",
          ospcScoreLevel = "",
          ospiPercentageScore = "",
          ospiScoreLevel = "",
          calculatedDate = null
        )
      )
    }
    return historicalScoresResponse
      .map {
        HistoricalScore(
          date = it.calculatedDate?.let { it1 -> formatDateTimeStamp(it1) } ?: "",
          scores = Scores(
            rsr = RSR(level = it.rsrScoreLevel ?: "", score = it.rsrPercentageScore ?: "", type = "RSR"),
            ospc = OSPC(level = it.ospcScoreLevel ?: "", score = it.ospcPercentageScore ?: "", type = "OSP/C"),
            ospi = OSPI(level = it.ospiScoreLevel ?: "", score = it.ospiPercentageScore ?: "", type = "OSP/I"),
            ogrs = OGRS(level = "", score = "", type = "OGRS") // TODO ARN team will provide this - WIP
          )
        )
      }
  }

  private fun formatDateTimeStamp(localDateTimeString: String): String {
    return LocalDateTime.parse(localDateTimeString).format(
      DateTimeFormatter.ofPattern("dd MMMM YYYY HH:mm")
        .withLocale(Locale.UK)
    )
  }

  private suspend fun handleFetchRiskSummary(crn: String): RiskSummaryResponse? {
    return try {
      getValueAndHandleWrappedException(arnApiClient.getRiskSummary(crn))
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No Risk Summary available for CRN: $crn - ${e.message}")
      null
    }
  }

  private suspend fun handleFetchMappaApiCall(crn: String): Mappa? {
    return try {
      fetchMappa(crn)
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No MAPPA details available for CRN: $crn - ${e.message}")
      Mappa(level = "", isNominal = true, lastUpdated = "")
    }
  }

  private suspend fun extractNatureOfRisk(riskSummaryResponse: RiskSummaryResponse?): NatureOfRisk {
    return NatureOfRisk(
      description = riskSummaryResponse?.natureOfRisk ?: "",
      oasysHeading = OasysHeading(
        number = "10.2",
        description = "What is the nature of the risk?"
      )
    )
  }

  private suspend fun extractFactorsToReduceRisk(riskSummaryResponse: RiskSummaryResponse?): FactorsToReduceRisk {
    return FactorsToReduceRisk(
      description = riskSummaryResponse?.riskMitigationFactors ?: "",
      oasysHeading = OasysHeading(
        number = "10.5",
        description = "What factors are likely to reduce the risk?"
      )
    )
  }

  private suspend fun extractWhenRiskHighest(riskSummaryResponse: RiskSummaryResponse?): WhenRiskHighest {
    return WhenRiskHighest(
      description = riskSummaryResponse?.riskImminence ?: "",
      oasysHeading = OasysHeading(
        number = "10.3",
        description = "When is the risk likely to be greatest?"
      )
    )
  }

  private suspend fun extractCircumstancesIncreaseRisk(riskSummaryResponse: RiskSummaryResponse?): CircumstancesIncreaseRisk {
    return CircumstancesIncreaseRisk(
      description = riskSummaryResponse?.riskIncreaseFactors ?: "",
      oasysHeading = OasysHeading(
        number = "10.4",
        description = "What circumstances are likely to increase the risk?"
      )
    )
  }

  private suspend fun extractWhoIsAtRisk(riskSummaryResponse: RiskSummaryResponse?): WhoIsAtRisk {
    return WhoIsAtRisk(
      description = riskSummaryResponse?.whoIsAtRisk ?: "",
      oasysHeading = OasysHeading(
        number = "10.1",
        description = "Who is at risk?"
      )
    )
  }

  private suspend fun extractRiskOfSeriousHarm(riskSummaryResponse: RiskSummaryResponse?): RiskOfSeriousHarm {
    val overallRisk = riskSummaryResponse?.overallRiskLevel
    return RiskOfSeriousHarm(
      overallRisk = overallRisk ?: "",
      riskToChildren = getRiskLevel(riskSummaryResponse, "children") ?: "",
      riskToPublic = getRiskLevel(riskSummaryResponse, "public") ?: "",
      riskToKnownAdult = getRiskLevel(riskSummaryResponse, "known adult") ?: "",
      riskToStaff = getRiskLevel(riskSummaryResponse, "staff") ?: "",
      lastUpdated = riskSummaryResponse?.assessedOn?.toLocalDate()?.toString() ?: ""
    )
  }

  private fun getRiskLevel(riskSummaryResponse: RiskSummaryResponse?, key: String): String? {

    val veryHigh = riskSummaryResponse?.riskInCommunity?.veryHigh
      ?.firstOrNull { it?.lowercase() == key }
    val high = riskSummaryResponse?.riskInCommunity?.high
      ?.firstOrNull { it?.lowercase() == key }
    val medium = riskSummaryResponse?.riskInCommunity?.medium
      ?.firstOrNull { it?.lowercase() == key }
    val low = riskSummaryResponse?.riskInCommunity?.low
      ?.firstOrNull { it?.lowercase() == key }

    val risks = linkedMapOf<String?, String?>(
      "VERY_HIGH" to veryHigh, "HIGH" to high, "MEDIUM" to medium, "LOW" to low
    )

    val highestRecordedRiskLevel = risks.asIterable().firstOrNull { it.value != null }?.key

    return highestRecordedRiskLevel
  }

  private suspend fun fetchPersonalDetails(crn: String): RiskPersonalDetails {
    val offenderDetails = getValueAndHandleWrappedException(communityApiClient.getAllOffenderDetails(crn))
    val age = offenderDetails?.dateOfBirth?.until(LocalDate.now())?.years
    val firstName = offenderDetails?.firstName ?: ""
    val surname = offenderDetails?.surname ?: ""
    val name = if (firstName.isEmpty()) {
      surname
    } else "$firstName $surname"

    return RiskPersonalDetails(
      name = name,
      dateOfBirth = offenderDetails?.dateOfBirth,
      age = age,
      gender = offenderDetails?.gender ?: "",
      crn = crn
    )
  }

  private suspend fun fetchMappa(crn: String): Mappa {
    val mappaResponse = try {
      getValueAndHandleWrappedException(communityApiClient.getAllMappaDetails(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No MAPPA details available for CRN: $crn - ${e.message}")
      null
    }
    val reviewDate = mappaResponse?.reviewDate?.format(
      DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.UK)
    )
    return Mappa(
      level = mappaResponse?.levelDescription ?: "",
      isNominal = true,
      lastUpdated = reviewDate ?: ""
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
