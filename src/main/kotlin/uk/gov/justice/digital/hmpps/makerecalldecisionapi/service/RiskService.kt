package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.Assessment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.convertUtcDateTimeStringToIso8601Date
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.ExceptionCodeHelper.Helper.extractErrorCode
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.SCORE_NOT_APPLICABLE
import java.time.LocalDateTime

@Service
internal class RiskService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  @Qualifier("assessRisksNeedsApiClientUserEnhanced") private val arnApiClient: ArnApiClient,
  private val userAccessValidator: UserAccessValidator,
  private val recommendationService: RecommendationService,
  private val personDetailsService: PersonDetailsService
) {

  suspend fun getRisk(crn: String): RiskResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      RiskResponse(userAccessResponse = userAccessResponse, mappa = null)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val roshSummary = getRoshSummary(crn)
      val mappa = getMappa(crn)
      val predictorScores = fetchPredictorScores(crn)
      val recommendationDetails = recommendationService.getDraftRecommendationForCrn(crn)

      return RiskResponse(
        personalDetailsOverview = personalDetailsOverview,
        mappa = mappa,
        predictorScores = predictorScores,
        roshSummary = roshSummary,
        activeRecommendation = recommendationDetails,
        assessmentStatus = getAssessmentStatus(crn)
      )
    }
  }

  suspend fun getAssessmentStatus(crn: String): String {
    val assessmentsResponse = fetchAssessments(crn)
    val latestAssessment =
      assessmentsResponse.assessments?.sortedBy { LocalDateTime.parse(it.initiationDate).toLocalDate() }?.reversed()
        ?.firstOrNull()
    return getStatusFromSuperStatus(latestAssessment?.superStatus)
  }

  suspend fun getStatusFromSuperStatus(superStatus: String?): String {
    return if (superStatus == COMPLETE.name) COMPLETE.name else INCOMPLETE.name
  }

  suspend fun fetchAssessmentInfo(crn: String, hideOffenceDetailsWhenNoMatch: Boolean): AssessmentInfo? {
    val convictionsFromDelius = try {
      getValueAndHandleWrappedException(communityApiClient.getActiveConvictions(crn, activeOnly = false))
    } catch (ex: Exception) {
      return AssessmentInfo(error = extractErrorCode(ex, "convictions", crn))
    }
    val assessmentsResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getAssessments(crn))!!
    } catch (ex: Exception) {
      log.info("No assessments available for CRN: $crn - ${ex.message}")
      return AssessmentInfo(error = extractErrorCode(ex, "risk assessments", crn))
    }
    val latestCompleteAssessment = getLatestCompleteAssessment(assessmentsResponse)
    val offences = getOffencesFromLatestCompleteAssessment(convictionsFromDelius, latestCompleteAssessment, hideOffenceDetailsWhenNoMatch)
    return buildAssessmentInfo(
      crn = crn,
      activeConvictionPresent = convictionsFromDelius?.any { it.active == true } == true,
      offences = offences,
      latestCompleteAssessment = latestCompleteAssessment,
      hideOffenceDetailsWhenNoMatch = hideOffenceDetailsWhenNoMatch
    )
  }

  private fun buildAssessmentInfo(
    crn: String?,
    activeConvictionPresent: Boolean,
    offences: List<Offence>?,
    latestCompleteAssessment: Assessment?,
    hideOffenceDetailsWhenNoMatch: Boolean?
  ): AssessmentInfo {
    val offenceCodesMatch = offences?.any {
      currentOffenceCodesMatch(
        latestCompleteAssessment, it
      )
    }
    val datesMatch = datesMatch(
      latestCompleteAssessment,
      offences?.find { it.mainOffence == true }
    )
    val lastUpdatedDate = latestCompleteAssessment?.dateCompleted?.let { convertUtcDateTimeStringToIso8601Date(it) }
    val offenceDescription = if (hideOffenceDetailsWhenNoMatch == true) {
      getOffenceDescriptionWhenOffencesDoNotMatch(
        offences,
        latestCompleteAssessment
      )
    } else {
      latestCompleteAssessment?.offence
    }

    val errorOnBlankOffenceDescription = if (hideOffenceDetailsWhenNoMatch == false && offenceDescription == null) {
      log.info("No offence description available for CRN: $crn ")
      "NOT_FOUND"
    } else null

    return AssessmentInfo(
      error = errorOnBlankOffenceDescription,
      offenceDescription = offenceDescription,
      offencesMatch = offenceCodesMatch == true && datesMatch && activeConvictionPresent,
      lastUpdatedDate = lastUpdatedDate,
      offenceDataFromLatestCompleteAssessment = isLatestAssessment(latestCompleteAssessment) &&
        (latestCompleteAssessment?.assessmentStatus == "COMPLETE" && latestCompleteAssessment.superStatus == "COMPLETE")
    )
  }

  private fun getOffenceDescriptionWhenOffencesDoNotMatch(
    mainActiveCustodialOffenceFromLatestCompleteAssessment: List<Offence>?,
    latestAssessment: Assessment?
  ) = mainActiveCustodialOffenceFromLatestCompleteAssessment
    ?.filter { currentOffenceCodesMatch(latestAssessment, it) }
    ?.filter { isLatestAssessment(latestAssessment) }
    ?.map { latestAssessment?.offence }
    ?.firstOrNull()

  private fun getOffencesFromLatestCompleteAssessment(
    convictionsFromDelius: List<Conviction>?,
    latestAssessment: Assessment?,
    hideOffenceDetailsWhenNoMatch: Boolean?
  ): List<Offence>? {
    return filterOffencesToShow(convictionsFromDelius, hideOffenceDetailsWhenNoMatch)
      ?.filter { it.active == true }
      ?.flatMap(this::extractMainOffences)
      ?.filter { datesMatch(latestAssessment, it) }
  }

  private fun filterOffencesToShow(convictionsFromDelius: List<Conviction>?, hideOffenceDetailsWhenNoMatch: Boolean?): List<Conviction>? {
    val showOffencesForOverview = hideOffenceDetailsWhenNoMatch == false && convictionsFromDelius?.let { isAtLeastOneActiveConvictionPresent(it) } == true
    val showOffencesForRecFlow = hideOffenceDetailsWhenNoMatch == true && convictionsFromDelius?.let { isOnlyOneActiveCustodialConvictionPresent(it) } == true
    return if (showOffencesForOverview) {
      convictionsFromDelius
    } else convictionsFromDelius
      ?.filter {
        showOffencesForRecFlow && it.isCustodial
      }
  }

  private fun getLatestCompleteAssessment(assessmentsResponse: AssessmentsResponse) =
    assessmentsResponse.assessments
      ?.filter { it.dateCompleted != null && it.dateCompleted.isNotEmpty() }
      ?.sortedBy { LocalDateTime.parse(it.dateCompleted).toLocalDate() }
      ?.reversed()
      ?.firstOrNull { it.assessmentStatus == "COMPLETE" && it.superStatus == "COMPLETE" }

  private fun isOnlyOneActiveCustodialConvictionPresent(convictionsFromDelius: List<Conviction>) =
    convictionsFromDelius.filter { it.active == true }.count { it.isCustodial } == 1

  private fun isAtLeastOneActiveConvictionPresent(convictionsFromDelius: List<Conviction>) =
    convictionsFromDelius.any { it.active == true }

  private fun isLatestAssessment(it: Assessment?) =
    (it?.laterCompleteAssessmentExists == false && it.laterWIPAssessmentExists == false && it.laterPartCompSignedAssessmentExists == false && it.laterSignLockAssessmentExists == false && it.laterPartCompUnsignedAssessmentExists == false)

  private fun datesMatch(
    latestAssessment: Assessment?,
    mainOffence: Offence?
  ) = latestAssessment?.offenceDetails?.any {
    it.offenceDate != null && (LocalDateTime.parse(it.offenceDate).toLocalDate() == mainOffence?.offenceDate)
  } == true

  private fun currentOffenceCodesMatch(it: Assessment?, offence: Offence) =
    it?.offenceDetails?.any { ((it.offenceCode + it.offenceSubCode) == offence.detail?.code) && (it.type == "CURRENT") } == true

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

  suspend fun fetchPredictorScores(crn: String): PredictorScores {
    val riskScoresResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getRiskScores(crn))!!
    } catch (ex: Exception) {
      return PredictorScores(
        error = extractErrorCode(ex, "risk management plan", crn),
        current = null,
        historical = null
      )
    }
    val latestScores = riskScoresResponse.sortedBy { LocalDateTime.parse(it.completedDate) }.reversed().firstOrNull()
    val historicalScores = riskScoresResponse.sortedBy { it.completedDate }.reversed()

    return PredictorScores(
      current = latestScores?.let { createTimelineDataPoint(it) },
      historical = historicalScores.mapNotNull { createTimelineDataPoint(it) }
    )
  }

  private fun createTimelineDataPoint(riskScoreResponse: RiskScoreResponse): PredictorScore? {
    val scores = Scores(
      rsr = rsrLevelWithScore(riskScoreResponse),
      ospc = buildLevelWithScore(riskScoreResponse.sexualPredictorScore?.ospContactScoreLevel, riskScoreResponse.sexualPredictorScore?.ospContactPercentageScore, "OSP/C"),
      ospi = buildLevelWithScore(riskScoreResponse.sexualPredictorScore?.ospIndecentScoreLevel, riskScoreResponse.sexualPredictorScore?.ospIndecentPercentageScore, "OSP/I"),
      ogrs = buildTwoYearScore(riskScoreResponse.groupReconvictionScore?.scoreLevel, riskScoreResponse.groupReconvictionScore?.oneYear, riskScoreResponse.groupReconvictionScore?.twoYears, "OGRS"),
      ogp = buildTwoYearScore(riskScoreResponse.generalPredictorScore?.ogpRisk, riskScoreResponse.generalPredictorScore?.ogp1Year, riskScoreResponse.generalPredictorScore?.ogp2Year, "OGP"),
      ovp = buildTwoYearScore(riskScoreResponse.violencePredictorScore?.ovpRisk, riskScoreResponse.violencePredictorScore?.oneYear, riskScoreResponse.violencePredictorScore?.twoYears, "OVP"),
    )
    return if (scores.ogp == null && scores.ogrs == null && scores.ovp == null && scores.rsr == null && scores.ospc == null && scores.ospi == null) null else PredictorScore(
      date = LocalDateTime.parse(riskScoreResponse?.completedDate).toLocalDate().toString(),
      scores = scores
    )
  }

  private fun buildTwoYearScore(level: String?, oneYear: String?, twoYears: String?, type: String?): LevelWithTwoYearScores? {
    val scoreEmpty = level == null && twoYears == null && oneYear == null
    return if (scoreEmpty) null else LevelWithTwoYearScores(
      level = level,
      oneYear = oneYear,
      twoYears = twoYears,
      type = type
    )
  }

  private fun buildLevelWithScore(level: String?, percentageScore: String?, type: String?): LevelWithScore? {
    val scoreIsNull = level == null && percentageScore == null
    val notApplicableWithZeroPercentScorePresent =
      level.equals(SCORE_NOT_APPLICABLE, ignoreCase = true) && percentageScore == "0"
    val noScore = scoreIsNull || notApplicableWithZeroPercentScorePresent
    return if (noScore) null else LevelWithScore(
      level = level,
      score = if (type == "OSP/I" || type == "OSP/C") null else percentageScore,
      type = type
    )
  }

  private fun rsrLevelWithScore(riskScoreResponse: RiskScoreResponse?): LevelWithScore? {
    val rsr = riskScoreResponse?.riskOfSeriousRecidivismScore
    val rsrScore = riskScoreResponse?.riskOfSeriousRecidivismScore
    return if (rsrScore?.scoreLevel == null && rsrScore?.percentageScore == null) null
    else LevelWithScore(
      level = rsr?.scoreLevel,
      score = rsr?.percentageScore,
      type = "RSR"
    )
  }

  private suspend fun extractRiskOfSeriousHarm(riskSummaryResponse: RiskSummaryResponse?): RiskOfSeriousHarm {
    return RiskOfSeriousHarm(
      overallRisk = riskSummaryResponse?.overallRiskLevel ?: "",
      riskInCustody = extractSectionFromRosh(riskSummaryResponse?.riskInCustody),
      riskInCommunity = extractSectionFromRosh(riskSummaryResponse?.riskInCommunity)
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

    val risks = linkedMapOf<String?, String?>(
      "VERY_HIGH" to veryHigh, "HIGH" to high, "MEDIUM" to medium, "LOW" to low
    )

    return risks.asIterable().firstOrNull { it.value != null }?.key
  }

  suspend fun getMappa(crn: String): Mappa {
    val mappaResponse = try {
      getValueAndHandleWrappedException(communityApiClient.getAllMappaDetails(crn))!!
    } catch (ex: Exception) {
      return Mappa(error = extractErrorCode(ex, "mappa", crn))
    }
    val startDate = mappaResponse.startDate?.toString()

    return Mappa(
      level = mappaResponse.level,
      lastUpdatedDate = startDate ?: "",
      category = mappaResponse.category
    )
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
      lastUpdatedDate = riskSummaryResponse?.assessedOn?.let { convertUtcDateTimeStringToIso8601Date(it) }
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
      riskManagementResponse.riskManagementPlan?.sortedBy { LocalDateTime.parse(it.initiationDate).toLocalDate() }
        ?.reversed()
        ?.get(0)
    val assessmentStatusComplete = getStatusFromSuperStatus(latestPlan?.superStatus) == COMPLETE.name
    val lastUpdatedDate = latestPlan?.dateCompleted ?: latestPlan?.initiationDate

    return RiskManagementPlan(
      assessmentStatusComplete = assessmentStatusComplete,
      latestDateCompleted = latestPlan?.latestCompleteDate?.let { convertUtcDateTimeStringToIso8601Date(it) },
      initiationDate = latestPlan?.initiationDate?.let { convertUtcDateTimeStringToIso8601Date(it) },
      lastUpdatedDate = lastUpdatedDate?.let { convertUtcDateTimeStringToIso8601Date(it) },
      contingencyPlans = latestPlan?.contingencyPlans
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
