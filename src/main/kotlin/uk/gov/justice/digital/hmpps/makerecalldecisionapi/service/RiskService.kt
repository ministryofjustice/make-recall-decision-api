package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AssessmentStatus.COMPLETE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AssessmentStatus.INCOMPLETE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LevelWithTwoYearScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScores
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskManagementPlan
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskOfSeriousHarm
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskPersonalDetails
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
import java.time.LocalDate
import java.time.LocalDateTime

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
      assessmentsResponse.assessments?.maxByOrNull { LocalDateTime.parse(it.initiationDate).toLocalDate() }
    return getStatusFromSuperStatus(latestAssessment?.superStatus)
  }

  suspend fun getStatusFromSuperStatus(superStatus: String?): String {
    return if (superStatus == COMPLETE.name) COMPLETE.name else INCOMPLETE.name
  }

  suspend fun fetchIndexOffenceDetails(crn: String): String? {
    val activeConvictionsFromDelius = getValueAndHandleWrappedException(communityApiClient.getActiveConvictions(crn))
    val assessmentsResponse = fetchAssessments(crn)
    val latestAssessment =
      assessmentsResponse.assessments?.maxByOrNull { LocalDateTime.parse(it.dateCompleted).toLocalDate() }
    val oasysAssessmentCompleted =
      latestAssessment?.assessmentStatus == "COMPLETE" && latestAssessment.superStatus == "COMPLETE"

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

  private fun isLatestAssessment(it: Assessment?) =
    (it?.laterCompleteAssessmentExists == false && it.laterWIPAssessmentExists == false && it.laterPartCompSignedAssessmentExists == false && it.laterSignLockAssessmentExists == false && it.laterPartCompUnsignedAssessmentExists == false)

  private fun datesMatch(
    latestAssessment: Assessment?,
    mainOffence: Offence?
  ) = latestAssessment?.offenceDetails?.any {
    LocalDateTime.parse(it.offenceDate).toLocalDate() == mainOffence?.offenceDate
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
    val latestScores = riskScoresResponse.maxByOrNull { LocalDateTime.parse(it.completedDate) }
    val historicalScores = riskScoresResponse.filter { it.completedDate != latestScores?.completedDate }

    return PredictorScores(
      current = createTimeLineDataPoint(latestScores),
      historical = historicalScores.map { createTimeLineDataPoint(it) }
    )
  }

  private fun createTimeLineDataPoint(riskScoreResponse: RiskScoreResponse?): PredictorScore {
    val rsr = riskScoreResponse?.riskOfSeriousRecidivismScore
    val osp = riskScoreResponse?.sexualPredictorScore
    val ogp = riskScoreResponse?.generalPredictorScore
    val ogrs = riskScoreResponse?.groupReconvictionScore
    val ovp = riskScoreResponse?.violencePredictorScore
    return PredictorScore(
      date = LocalDateTime.parse(riskScoreResponse?.completedDate).toLocalDate().toString(),
      scores = Scores(
        rsr = if (rsr == null) null else LevelWithScore(level = rsr.scoreLevel, score = rsr.percentageScore, type = "RSR"),
        ospc = if (osp == null) null else LevelWithScore(
          level = osp.ospContactScoreLevel,
          score = osp.ospContactPercentageScore,
          type = "OSP/C"
        ),
        ospi = if (osp == null) null else LevelWithScore(
          level = osp.ospIndecentScoreLevel,
          score = osp.ospIndecentPercentageScore,
          type = "OSP/I"
        ),
        ogrs = if (ogrs == null) null else LevelWithTwoYearScores(
          level = ogrs.scoreLevel,
          oneYear = ogrs.oneYear,
          twoYears = ogrs.twoYears,
          type = "OGRS"
        ),
        ogp = if (ogp == null) null else LevelWithTwoYearScores(
          level = ogp.ogpRisk,
          oneYear = ogp.ogp1Year,
          twoYears = ogp.ogp2Year,
          type = "OGP"
        ),
        ovp = if (ovp == null) null else LevelWithTwoYearScores(
          level = ovp.ovpRisk,
          oneYear = ovp.oneYear,
          twoYears = ovp.twoYears,
          type = "OVP"
        )
      )
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

  suspend fun getMappa(crn: String): Mappa {
    val mappaResponse = try {
      getValueAndHandleWrappedException(communityApiClient.getAllMappaDetails(crn))!!
    } catch (ex: Exception) {
      return Mappa(error = extractErrorCode(ex, "mappa", crn))
    }
    val reviewDate = mappaResponse.reviewDate?.toString()

    return Mappa(
      level = mappaResponse.level,
      lastUpdatedDate = reviewDate ?: "",
      category = mappaResponse.category
    )
  }

  suspend fun getRoshSummary(crn: String): RoshSummary {
    val riskSummaryResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getRiskSummary(crn))
    } catch (ex: Exception) {
      return RoshSummary(error = extractErrorCode(ex, "risk summary", crn))
    }

    val riskOfSeriousHarm = extractRiskOfSeriousHarm(riskSummaryResponse)

    return RoshSummary(
      riskOfSeriousHarm = riskOfSeriousHarm,
      natureOfRisk = riskSummaryResponse?.natureOfRisk ?: "",
      whoIsAtRisk = riskSummaryResponse?.whoIsAtRisk ?: "",
      riskIncreaseFactors = riskSummaryResponse?.riskIncreaseFactors ?: "",
      riskMitigationFactors = riskSummaryResponse?.riskMitigationFactors ?: "",
      riskImminence = riskSummaryResponse?.riskImminence ?: "",
      lastUpdatedDate = convertUtcDateTimeStringToIso8601Date(riskSummaryResponse?.assessedOn)
    )
  }

  suspend fun getLatestRiskManagementPlan(crn: String): RiskManagementPlan {
    val riskManagementResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getRiskManagementPlan(crn))!!
    } catch (ex: Exception) {
      return RiskManagementPlan(error = extractErrorCode(ex, "risk management plan", crn))
    }

    val latestPlan =
      riskManagementResponse.riskManagementPlan?.maxByOrNull { LocalDateTime.parse(it.initiationDate).toLocalDate() }
    val assessmentStatusComplete = getStatusFromSuperStatus(latestPlan?.superStatus) == COMPLETE.name
    val lastUpdatedDate = latestPlan?.dateCompleted ?: latestPlan?.initiationDate

    return RiskManagementPlan(
      assessmentStatusComplete = assessmentStatusComplete,
      latestDateCompleted = convertUtcDateTimeStringToIso8601Date(latestPlan?.latestCompleteDate),
      initiationDate = convertUtcDateTimeStringToIso8601Date(latestPlan?.initiationDate),
      lastUpdatedDate = convertUtcDateTimeStringToIso8601Date(lastUpdatedDate),
      contingencyPlans = latestPlan?.contingencyPlans
    )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
