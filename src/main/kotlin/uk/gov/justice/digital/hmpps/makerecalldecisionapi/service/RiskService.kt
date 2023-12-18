package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AssessmentInfo
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AssessmentStatus.COMPLETE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AssessmentStatus.INCOMPLETE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithTwoYearScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskManagementPlan
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskOfSeriousHarm
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskTo
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RoshSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Scores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toOverview
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CodeDescriptionItem
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Registration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.RoshHistory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.Assessment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertUtcDateTimeStringToIso8601Date
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.ExceptionCodeHelper.Helper.extractErrorCode
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.DEFAULT_DATE_TIME_FOR_NULL_VALUE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.SCORE_NOT_APPLICABLE
import java.time.LocalDate
import java.time.LocalDateTime

@Service
internal class RiskService(
  private val deliusClient: DeliusClient,
  @Qualifier("assessRisksNeedsApiClientUserEnhanced") private val arnApiClient: ArnApiClient,
  private val userAccessValidator: UserAccessValidator,
  @Lazy private val recommendationService: RecommendationService?,
) {

  suspend fun getRisk(crn: String): RiskResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      RiskResponse(userAccessResponse = userAccessResponse, mappa = null)
    } else {
      val (personalDetails, mappa, roshHistory) = deliusClient.getMappaAndRoshHistory(crn)
      val roshSummary = getRoshSummary(crn)
      val predictorScores = fetchPredictorScores(crn)
      val recommendationDetails = recommendationService?.getRecommendationsInProgressForCrn(crn)

      return RiskResponse(
        personalDetailsOverview = personalDetails.toOverview(crn),
        mappa = mappa?.let {
          Mappa(
            level = it.level,
            category = it.category,
            lastUpdatedDate = it.startDate,
          )
        },
        predictorScores = predictorScores,
        roshSummary = roshSummary,
        roshHistory = RoshHistory(
          registrations = roshHistory.map {
            Registration(
              active = it.active,
              type = CodeDescriptionItem(it.type, it.typeDescription),
              startDate = it.startDate,
              notes = it.notes,
            )
          },
        ),
        activeRecommendation = recommendationDetails,
        assessmentStatus = getAssessmentStatus(crn),
      )
    }
  }

  suspend fun getAssessmentStatus(crn: String): String {
    val assessmentsResponse = fetchAssessments(crn)
    val latestAssessment =
      assessmentsResponse.assessments
        ?.sortedBy { it.initiationDate ?: DEFAULT_DATE_TIME_FOR_NULL_VALUE }
        ?.lastOrNull()
    return getStatusFromSuperStatus(latestAssessment?.superStatus)
  }

  suspend fun getStatusFromSuperStatus(superStatus: String?): String {
    return if (superStatus == COMPLETE.name) COMPLETE.name else INCOMPLETE.name
  }

  suspend fun fetchAssessmentInfo(crn: String, convictionsFromDelius: List<Conviction>): AssessmentInfo? {
    val assessmentsResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getAssessments(crn))!!
    } catch (ex: Exception) {
      log.info("No assessments available for CRN: $crn - ${ex.message}")
      return AssessmentInfo(error = extractErrorCode(ex, "risk assessments", crn))
    }

    return buildAssessmentInfo(
      crn = crn,
      convictionsFromDelius = convictionsFromDelius,
      assessmentsResponse = assessmentsResponse,
    )
  }

  private fun buildAssessmentInfo(
    crn: String?,
    convictionsFromDelius: List<Conviction>,
    assessmentsResponse: AssessmentsResponse,
  ): AssessmentInfo {
    val latestCompleteAssessment = getLatestCompleteAssessment(assessmentsResponse)

    val offences = getOffencesFromLatestCompleteAssessment(convictionsFromDelius, latestCompleteAssessment)
    val offencesMatch = offences.any {
      currentOffenceCodesMatch(latestCompleteAssessment, it) && datesMatch(latestCompleteAssessment, it.date)
    } && convictionsFromDelius.isNotEmpty()

    val lastUpdatedDate = latestCompleteAssessment?.dateCompleted?.let { convertUtcDateTimeStringToIso8601Date(it) }

    val offenceDescription = latestCompleteAssessment?.offence

    val errorOnBlankOffenceDescription = if (offenceDescription == null) {
      log.info("No offence description available for CRN: $crn ")
      "NOT_FOUND"
    } else {
      null
    }

    return AssessmentInfo(
      error = errorOnBlankOffenceDescription,
      offenceDescription = offenceDescription,
      offencesMatch = offencesMatch,
      lastUpdatedDate = lastUpdatedDate,
      offenceDataFromLatestCompleteAssessment = isLatestAssessment(latestCompleteAssessment),
    )
  }

  private fun getOffencesFromLatestCompleteAssessment(
    convictionsFromDelius: List<Conviction>,
    latestAssessment: Assessment?,
  ) = convictionsFromDelius
    .map { it.mainOffence }
    .filter { datesMatch(latestAssessment, it.date) }

  private fun getLatestCompleteAssessment(assessmentsResponse: AssessmentsResponse) =
    assessmentsResponse.assessments
      ?.sortedBy { it.dateCompleted ?: DEFAULT_DATE_TIME_FOR_NULL_VALUE }
      ?.lastOrNull() { it.assessmentStatus == "COMPLETE" && it.superStatus == "COMPLETE" }

  private fun isLatestAssessment(it: Assessment?) =
    (it?.laterCompleteAssessmentExists == false && it.laterWIPAssessmentExists == false && it.laterPartCompSignedAssessmentExists == false && it.laterSignLockAssessmentExists == false && it.laterPartCompUnsignedAssessmentExists == false)

  private fun datesMatch(
    latestAssessment: Assessment?,
    mainOffenceDate: LocalDate?,
  ) = latestAssessment
    ?.offenceDetails
    ?.map { it.copy(offenceDate = it.offenceDate ?: DEFAULT_DATE_TIME_FOR_NULL_VALUE) }
    ?.any {
      LocalDateTime.parse(it.offenceDate).toLocalDate() == mainOffenceDate
    } == true

  private fun currentOffenceCodesMatch(it: Assessment?, offence: Offence) =
    it?.offenceDetails?.any { ((it.offenceCode + it.offenceSubCode) == offence.code) && (it.type == "CURRENT") } == true

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

  suspend fun fetchPredictorScores(crn: String): PredictorScores {
    val riskScoresResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getRiskScores(crn))!!
    } catch (ex: Exception) {
      return PredictorScores(
        error = extractErrorCode(ex, "risk management plan", crn),
        current = null,
        historical = null,
      )
    }
    val historicalScores = riskScoresResponse
      .sortedBy { it.completedDate ?: DEFAULT_DATE_TIME_FOR_NULL_VALUE }
      .reversed()
    val latestScore = historicalScores.firstOrNull()
    return PredictorScores(
      current = latestScore?.let { createTimelineDataPoint(it) },
      historical = historicalScores.mapNotNull { createTimelineDataPoint(it) },
    )
  }

  private fun createTimelineDataPoint(riskScoreResponse: RiskScoreResponse): PredictorScore? {
    val scores = Scores(
      rsr = rsrLevelWithScore(riskScoreResponse),
      ospc = buildLevelWithScore(
        riskScoreResponse.sexualPredictorScore?.ospContactScoreLevel,
        riskScoreResponse.sexualPredictorScore?.ospContactPercentageScore,
        "OSP/C",
      ),
      ospi = buildLevelWithScore(
        riskScoreResponse.sexualPredictorScore?.ospIndecentScoreLevel,
        riskScoreResponse.sexualPredictorScore?.ospIndecentPercentageScore,
        "OSP/I",
      ),
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
    return if (scores.ogp == null && scores.ogrs == null && scores.ovp == null && scores.rsr == null && scores.ospc == null && scores.ospi == null) {
      null
    } else {
      PredictorScore(
        date = LocalDateTime.parse(riskScoreResponse.completedDate ?: DEFAULT_DATE_TIME_FOR_NULL_VALUE).toLocalDate().toString(),
        scores = scores,
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

  private fun buildLevelWithScore(level: String?, percentageScore: String?, type: String?): LevelWithScore? {
    val scoreIsNull = level == null && percentageScore == null
    val notApplicableWithZeroPercentScorePresent =
      level.equals(SCORE_NOT_APPLICABLE, ignoreCase = true) && percentageScore == "0"
    val noScore = scoreIsNull || notApplicableWithZeroPercentScorePresent
    return if (noScore) {
      null
    } else {
      LevelWithScore(
        level = level,
        score = if (type == "OSP/I" || type == "OSP/C") null else percentageScore,
        type = type,
      )
    }
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

  private suspend fun extractRiskOfSeriousHarm(riskSummaryResponse: RiskSummaryResponse?): RiskOfSeriousHarm {
    return RiskOfSeriousHarm(
      overallRisk = riskSummaryResponse?.overallRiskLevel ?: "",
      riskInCustody = extractSectionFromRosh(riskSummaryResponse?.riskInCustody),
      riskInCommunity = extractSectionFromRosh(riskSummaryResponse?.riskInCommunity),
    )
  }

  private suspend fun extractSectionFromRosh(riskScore: RiskScore?): RiskTo {
    return RiskTo(
      riskToChildren = getRiskLevel(riskScore, "children") ?: "",
      riskToPublic = getRiskLevel(riskScore, "public") ?: "",
      riskToKnownAdult = getRiskLevel(riskScore, "known adult") ?: "",
      riskToStaff = getRiskLevel(riskScore, "staff") ?: "",
      riskToPrisoners = getRiskLevel(riskScore, "prisoners") ?: "",
    )
  }

  private fun getRiskLevel(riskScore: RiskScore?, key: String): String? {
    val veryHigh = riskScore?.veryHigh
      ?.firstOrNull { it?.lowercase() == key }
    val high = riskScore?.high
      ?.firstOrNull { it?.lowercase() == key }
    val medium = riskScore?.medium
      ?.firstOrNull { it?.lowercase() == key }
    val low = riskScore?.low
      ?.firstOrNull { it?.lowercase() == key }

    val risks = linkedMapOf<String?, String?>("VERY_HIGH" to veryHigh, "HIGH" to high, "MEDIUM" to medium, "LOW" to low)

    return risks.asIterable().firstOrNull { it.value != null }?.key
  }

  suspend fun getRoshSummary(crn: String): RoshSummary {
    val riskSummaryResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getRiskSummary(crn))
    } catch (ex: Exception) {
      return RoshSummary(error = extractErrorCode(ex, "risk summary", crn))
    }

    if (areAllRiskSummaryFieldsEmpty(riskSummaryResponse)) {
      return RoshSummary(error = "MISSING_DATA")
    }

    val riskOfSeriousHarm = extractRiskOfSeriousHarm(riskSummaryResponse)

    return RoshSummary(
      riskOfSeriousHarm = riskOfSeriousHarm,
      natureOfRisk = riskSummaryResponse?.natureOfRisk,
      whoIsAtRisk = riskSummaryResponse?.whoIsAtRisk,
      riskIncreaseFactors = riskSummaryResponse?.riskIncreaseFactors,
      riskMitigationFactors = riskSummaryResponse?.riskMitigationFactors,
      riskImminence = riskSummaryResponse?.riskImminence,
      lastUpdatedDate = riskSummaryResponse?.assessedOn?.let { convertUtcDateTimeStringToIso8601Date(it) },
    )
  }

  suspend fun areAllRiskSummaryFieldsEmpty(riskSummaryResponse: RiskSummaryResponse?): Boolean {
    // There is an issue with the data getting returned by ARN for this. For some cases, it replies with a 200 and just returns an empty object for most fields.
    // In this scenario, check all relevant fields are empty, and if so, return true. This should be followed up with ARN/OASys to understand why this is happening.
    return (
      riskSummaryResponse?.whoIsAtRisk == null &&
        riskSummaryResponse?.natureOfRisk == null &&
        riskSummaryResponse?.riskImminence == null &&
        riskSummaryResponse?.riskIncreaseFactors == null &&
        riskSummaryResponse?.riskMitigationFactors == null &&
        riskSummaryResponse?.riskInCommunity?.veryHigh == null &&
        riskSummaryResponse?.riskInCommunity?.high == null &&
        riskSummaryResponse?.riskInCommunity?.medium == null &&
        riskSummaryResponse?.riskInCommunity?.low == null &&
        riskSummaryResponse?.riskInCustody?.veryHigh == null &&
        riskSummaryResponse?.riskInCustody?.high == null &&
        riskSummaryResponse?.riskInCustody?.medium == null &&
        riskSummaryResponse?.riskInCustody?.low == null &&
        riskSummaryResponse?.overallRiskLevel == null
      )
  }

  suspend fun getLatestRiskManagementPlan(crn: String): RiskManagementPlan {
    val riskManagementResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getRiskManagementPlan(crn))!!
    } catch (ex: Exception) {
      return RiskManagementPlan(error = extractErrorCode(ex, "risk management plan", crn))
    }

    val latestPlan =
      riskManagementResponse.riskManagementPlan?.sortedBy { it.initiationDate }
        ?.reversed()
        ?.get(0)
    val assessmentStatusComplete = getStatusFromSuperStatus(latestPlan?.superStatus) == COMPLETE.name
    val lastUpdatedDate = latestPlan?.dateCompleted ?: latestPlan?.initiationDate

    return RiskManagementPlan(
      assessmentStatusComplete = assessmentStatusComplete,
      latestDateCompleted = latestPlan?.latestCompleteDate?.let { convertUtcDateTimeStringToIso8601Date(it) },
      initiationDate = latestPlan?.initiationDate?.let { convertUtcDateTimeStringToIso8601Date(it) },
      lastUpdatedDate = lastUpdatedDate?.let { convertUtcDateTimeStringToIso8601Date(it) },
      contingencyPlans = latestPlan?.contingencyPlans,
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
