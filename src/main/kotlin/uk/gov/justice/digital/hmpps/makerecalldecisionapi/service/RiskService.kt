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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AssessmentStatus.COMPLETE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AssessmentStatus.INCOMPLETE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskManagementPlan
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.Assessment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ContingencyPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.CurrentScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.HistoricalScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.SexualPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertUtcDateTimeStringToIso8601Date
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.ExceptionCodeHelper.Helper.extractErrorCode
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
      RiskResponse(userAccessResponse = userAccessResponse, mappa = null)
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
      val contingencyPlan = fetchContingencyPlan(crn)
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
        activeRecommendation = recommendationDetails,
        assessmentStatus = getAssessmentStatus(crn)
      )
    }
  }

  suspend fun getAssessmentStatus(crn: String): String {
    val assessmentsResponse = fetchAssessments(crn)
    val latestAssessment = assessmentsResponse.assessments?.maxByOrNull { LocalDateTime.parse(it.initiationDate).toLocalDate() }
    return getStatusFromSuperStatus(latestAssessment?.superStatus)
  }

  suspend fun getStatusFromSuperStatus(superStatus: String?): String {
    return if (superStatus == COMPLETE.name) COMPLETE.name else INCOMPLETE.name
  }

  suspend fun fetchIndexOffenceDetails(crn: String): String? {
    val activeConvictionsFromDelius = getValueAndHandleWrappedException(communityApiClient.getActiveConvictions(crn))
    val assessmentsResponse = fetchAssessments(crn)
    val latestAssessment = assessmentsResponse.assessments?.maxByOrNull { LocalDateTime.parse(it.dateCompleted).toLocalDate() }
    val oasysAssessmentCompleted = latestAssessment?.assessmentStatus == "COMPLETE" && latestAssessment.superStatus == "COMPLETE"

    return activeConvictionsFromDelius
      ?.filter { oasysAssessmentCompleted }
      ?.filter { isOnlyOneActiveCustodialConvictionPresent(activeConvictionsFromDelius) }
      ?.filter { it.isCustodial && it.active == true }
      ?.flatMap(this::extractMainOffences)
      ?.filter { datesMatch(latestAssessment, it) }
      ?.filter { currentOffenceCodesMatch(latestAssessment, it) }
      ?.filter { isLatestAssessment(latestAssessment) }
      ?.map { latestAssessment?.offence }
      ?.firstOrNull()
  }

  private fun isOnlyOneActiveCustodialConvictionPresent(activeConvictionsFromDelius: List<Conviction>) =
    activeConvictionsFromDelius.count { it.isCustodial } == 1

  private fun isLatestAssessment(it: Assessment?) = (it?.laterCompleteAssessmentExists == false && it.laterWIPAssessmentExists == false && it.laterPartCompSignedAssessmentExists == false && it.laterSignLockAssessmentExists == false && it.laterPartCompUnsignedAssessmentExists == false)

  private fun datesMatch(
    latestAssessment: Assessment?,
    mainOffence: Offence?
  ) = latestAssessment?.offenceDetails?.any { LocalDateTime.parse(it.offenceDate).toLocalDate() == mainOffence?.offenceDate } == true

  private fun currentOffenceCodesMatch(it: Assessment?, offence: Offence) =
    it?.offenceDetails?.any { ((it.offenceCode + it.offenceSubCode) == offence.detail?.code) && (it.type == "CURRENT") } == true

  private suspend fun fetchContingencyPlan(crn: String): ContingencyPlan {
    val contingencyPlanResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getContingencyPlan(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No contingency plan available for CRN: $crn - ${e.message}")
      ContingencyPlanResponse(assessments = emptyList())
    } catch (e: WebClientResponseException.InternalServerError) {
      log.info("No contingency plan available for CRN: $crn - ${e.message} :: ${e.responseBodyAsString}")
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

  private fun fetchAssessments(crn: String): AssessmentsResponse {
    val assessmentsResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getAssessments(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No assessments available for CRN: $crn - ${e.message}")
      AssessmentsResponse(crn = null, limitedAccessOffender = null, assessments = null)
    } catch (e: WebClientResponseException.InternalServerError) {
      log.info("No assessments scores available for CRN: $crn - ${e.message} :: ${e.responseBodyAsString}")
      AssessmentsResponse(crn = null, limitedAccessOffender = null, assessments = null)
    }
    return assessmentsResponse
  }

  private fun extractMainOffences(it: Conviction): List<Offence> {
    return it.offences
      ?.filter { it.mainOffence == true } ?: emptyList()
  }

  private suspend fun fetchCurrentScores(crn: String): Scores {
    val currentScoresResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getCurrentScores(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No current scores available for CRN: $crn - ${e.message}")
      listOf(
        emptyCurrentScoreResponse()
      )
    } catch (e: WebClientResponseException.InternalServerError) {
      log.info("No current scores available for CRN: $crn - ${e.message} :: ${e.responseBodyAsString}")
      listOf(
        emptyCurrentScoreResponse()
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

  private fun emptyCurrentScoreResponse() = CurrentScoreResponse(
    completedDate = "",
    generalPredictorScore = GeneralPredictorScore(ogpStaticWeightedScore = "", ogpDynamicWeightedScore = "", ogpTotalWeightedScore = "", ogpRisk = ""),
    riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "", scoreLevel = ""),
    sexualPredictorScore = SexualPredictorScore(ospIndecentPercentageScore = "", ospContactPercentageScore = "", ospIndecentScoreLevel = "", ospContactScoreLevel = "")
  )

  private suspend fun fetchHistoricalScores(crn: String): List<HistoricalScore> {
    val historicalScoresResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getHistoricalScores(crn))!!
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No historical scores available for CRN: $crn - ${e.message}")
      emptyHistoricalScoresResponse()
    } catch (e: WebClientResponseException.InternalServerError) {
      log.info("No historical scores available for CRN: $crn - ${e.message} :: ${e.responseBodyAsString}")
      emptyHistoricalScoresResponse()
    }
    return historicalScoresResponse
      .map {
        HistoricalScore(
          date = it.calculatedDate?.let { it1 -> formatDateTimeStamp(it1) } ?: "",
          scores = Scores(
            rsr = RSR(level = it.rsrScoreLevel ?: "", score = it.rsrPercentageScore ?: "", type = "RSR"),
            ospc = OSPC(level = it.ospcScoreLevel ?: "", score = it.ospcPercentageScore ?: "", type = "OSP/C"),
            ospi = OSPI(level = it.ospiScoreLevel ?: "", score = it.ospiPercentageScore ?: "", type = "OSP/I"),
            ogrs = OGRS(level = "", score = "", type = "OGRS") // TODO not available from rsr/history - TBD
          )
        )
      }
  }

  private fun emptyHistoricalScoresResponse() = listOf(
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
    } catch (e: WebClientResponseException.InternalServerError) {
      log.info("No Risk Summary available for CRN: $crn - ${e.message} :: ${e.responseBodyAsString}")
      null
    }
  }

  private suspend fun handleFetchMappaApiCall(crn: String): Mappa? {
    return try {
      fetchMappa(crn)
    } catch (e: WebClientResponseException.NotFound) {
      log.info("No MAPPA details available for CRN: $crn - ${e.message}")
      Mappa(level = null, isNominal = true, lastUpdated = "", category = null)
    } catch (e: WebClientResponseException) {
      null
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
      level = mappaResponse?.level,
      isNominal = true,
      lastUpdated = reviewDate ?: "",
      category = mappaResponse?.category
    )
  }

  suspend fun getLatestRiskManagementPlan(crn: String): RiskManagementPlan {
    val riskManagementResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getRiskManagementPlan(crn))!!
    } catch (ex: Exception) {
      return RiskManagementPlan(error = extractErrorCode(ex, "risk management plan", crn))
    }

    val latestPlan = riskManagementResponse.riskManagementPlan?.maxByOrNull { LocalDateTime.parse(it.initiationDate).toLocalDate() }
    val assessmentStatusComplete = getStatusFromSuperStatus(latestPlan?.superStatus) == COMPLETE.name
    val lastUpdatedDate = latestPlan?.dateCompleted ?: latestPlan?.initiationDate

    return RiskManagementPlan(
      assessmentStatusComplete = assessmentStatusComplete,
      lastUpdatedDate = convertUtcDateTimeStringToIso8601Date(lastUpdatedDate),
      contingencyPlans = latestPlan?.contingencyPlans
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
