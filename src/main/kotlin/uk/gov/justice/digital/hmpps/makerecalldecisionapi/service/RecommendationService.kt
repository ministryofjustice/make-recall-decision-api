package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectReader
import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentRequestType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RoshSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallConsidered
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationsListItem
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.toDeliusContactOutcome
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.toPersonOnProbationDto
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toDntrDownloadedEventPayload
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toManagerRecallDecisionMadeEventPayload
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toPersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toRecommendationStartedEventPayload
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationUpdateException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UpdateExceptionTypes.RECOMMENDATION_UPDATE_FAILED
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.toRecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader.CustomMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.localNowDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.nowDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.utcNowDateTimeString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.time.OffsetDateTime
import java.util.Collections
import kotlin.jvm.optionals.getOrNull

@Transactional
@Service
internal class RecommendationService(
  val recommendationRepository: RecommendationRepository,
  @Lazy val personDetailsService: PersonDetailsService,
  val templateReplacementService: TemplateReplacementService,
  private val userAccessValidator: UserAccessValidator,
  private val convictionService: ConvictionService,
  @Lazy private val riskService: RiskService?,
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val mrdEventsEmitter: MrdEventsEmitter?,
  @Value("\${mrd.url}") private val mrdUrl: String? = null
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createRecommendation(
    recommendationRequest: CreateRecommendationRequest,
    userId: String?,
    readableNameOfUser: String?,
    featureFlags: FeatureFlags?
  ): RecommendationResponse? {
    val userAccessResponse = recommendationRequest.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val status = if (featureFlags?.flagConsiderRecall == true) Status.RECALL_CONSIDERED else Status.DRAFT
      val recallConsideredList = if (featureFlags?.flagConsiderRecall == true) listOf(
        RecallConsidered(
          userId = userId,
          createdDate = utcNowDateTimeString(),
          userName = readableNameOfUser,
          recallConsideredDetail = recommendationRequest.recallConsideredDetail
        )
      ) else null

      val sendRecommendationStartedDomainEvent =
        featureFlags?.flagDomainEventRecommendationStarted == true && featureFlags.flagConsiderRecall == false
      if (sendRecommendationStartedDomainEvent) {
        log.info("About to send domain event for ${recommendationRequest.crn} on Recommendation started")
        sendRecommendationStartedEvent(recommendationRequest.crn)
        log.info("Sent domain event for ${recommendationRequest.crn} on Recommendation started asynchronously")
      }

      val existingRecommendation = findInProgressRecommendationEntityForCase(recommendationRequest.crn)
      return if (existingRecommendation != null) {
        existingRecommendation.toRecommendationResponse()
      } else {
        val personDetails = recommendationRequest.crn?.let { personDetailsService.getPersonDetails(it) }
        saveNewRecommendationEntity(
          recommendationRequest,
          userId,
          readableNameOfUser,
          status,
          recallConsideredList,
          StaticRecommendationDataWrapper(
            personDetails?.toPersonOnProbation(),
            personDetails?.offenderManager?.probationAreaDescription,
            personDetails?.offenderManager?.probationTeam?.localDeliveryUnitDescription
          ),
          sendRecommendationStartedDomainEvent
        )?.toRecommendationResponse()
      }
    }
  }

  private fun findInProgressRecommendationEntityForCase(crn: String?): RecommendationEntity? {
    val recommendationEntity = crn?.let { recommendationRepository.findByCrnAndStatus(it, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name)) }
    recommendationEntity?.let(Collections::sort)
    return if (!recommendationEntity.isNullOrEmpty()) {
      when {
        recommendationEntity.size > 1 -> {
          log.error("More than one recommendation found for CRN. Returning the latest.")
          recommendationEntity[0]
        }
        else -> recommendationEntity[0]
      }
    } else null
  }

  fun buildRecallDecisionList(
    username: String?,
    readableUsername: String?,
    recallConsideredDetail: String?
  ): List<RecallConsidered> {
    return listOf(
      RecallConsidered(
        userId = username,
        createdDate = utcNowDateTimeString(),
        userName = readableUsername,
        recallConsideredDetail = recallConsideredDetail
      )
    )
  }

  fun getRecommendation(recommendationId: Long): RecommendationResponse {
    val recommendationResponse = getRecommendationResponseById(recommendationId)
    val userAccessResponse = recommendationResponse.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) RecommendationResponse(userAccessResponse) else recommendationResponse
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun getRecommendationResponseById(recommendationId: Long): RecommendationResponse {
    val recommendationEntity = getRecommendationEntityById(recommendationId)

    return buildRecommendationResponse(recommendationEntity)
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun getRecommendationEntityById(recommendationId: Long): RecommendationEntity {
    return recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
  }

  private fun buildRecommendationResponse(recommendationEntity: RecommendationEntity): RecommendationResponse {
    return RecommendationResponse(
      id = recommendationEntity.id,
      crn = recommendationEntity.data.crn,
      reviewPractitionersConcerns = recommendationEntity.data.reviewPractitionersConcerns,
      reviewOffenderProfile = recommendationEntity.data.reviewOffenderProfile,
      explainTheDecision = recommendationEntity.data.explainTheDecision,
      lastModifiedBy = recommendationEntity.data.lastModifiedBy,
      lastModifiedByUserName = recommendationEntity.data.lastModifiedByUserName,
      lastModifiedDate = recommendationEntity.data.lastModifiedDate,
      managerRecallDecision = recommendationEntity.data.managerRecallDecision,
      recallType = recommendationEntity.data.recallType,
      status = recommendationEntity.data.status,
      custodyStatus = recommendationEntity.data.custodyStatus,
      responseToProbation = recommendationEntity.data.responseToProbation,
      triggerLeadingToRecall = recommendationEntity.data.triggerLeadingToRecall,
      whatLedToRecall = recommendationEntity.data.whatLedToRecall,
      isThisAnEmergencyRecall = recommendationEntity.data.isThisAnEmergencyRecall,
      isIndeterminateSentence = recommendationEntity.data.isIndeterminateSentence,
      isExtendedSentence = recommendationEntity.data.isExtendedSentence,
      activeCustodialConvictionCount = recommendationEntity.data.activeCustodialConvictionCount,
      hasVictimsInContactScheme = recommendationEntity.data.hasVictimsInContactScheme,
      indeterminateSentenceType = recommendationEntity.data.indeterminateSentenceType,
      dateVloInformed = recommendationEntity.data.dateVloInformed,
      hasArrestIssues = recommendationEntity.data.hasArrestIssues,
      hasContrabandRisk = recommendationEntity.data.hasContrabandRisk,
      personOnProbation = recommendationEntity.data.personOnProbation?.toPersonOnProbationDto(),
      alternativesToRecallTried = recommendationEntity.data.alternativesToRecallTried,
      licenceConditionsBreached = recommendationEntity.data.licenceConditionsBreached,
      underIntegratedOffenderManagement = recommendationEntity.data.underIntegratedOffenderManagement,
      localPoliceContact = recommendationEntity.data.localPoliceContact,
      vulnerabilities = recommendationEntity.data.vulnerabilities,
      convictionDetail = recommendationEntity.data.convictionDetail,
      region = recommendationEntity.data.region,
      localDeliveryUnit = recommendationEntity.data.localDeliveryUnit,
      userNamePartACompletedBy = recommendationEntity.data.userNamePartACompletedBy,
      userEmailPartACompletedBy = recommendationEntity.data.userEmailPartACompletedBy,
      lastPartADownloadDateTime = recommendationEntity.data.lastPartADownloadDateTime,
      userNameDntrLetterCompletedBy = recommendationEntity.data.userNameDntrLetterCompletedBy,
      lastDntrLetterDownloadDateTime = recommendationEntity.data.lastDntrLetterADownloadDateTime,
      indexOffenceDetails = recommendationEntity.data.indexOffenceDetails,
      offenceAnalysis = recommendationEntity.data.offenceAnalysis,
      fixedTermAdditionalLicenceConditions = recommendationEntity.data.fixedTermAdditionalLicenceConditions,
      indeterminateOrExtendedSentenceDetails = recommendationEntity.data.indeterminateOrExtendedSentenceDetails,
      mainAddressWherePersonCanBeFound = recommendationEntity.data.mainAddressWherePersonCanBeFound,
      whyConsideredRecall = recommendationEntity.data.whyConsideredRecall,
      reasonsForNoRecall = recommendationEntity.data.reasonsForNoRecall,
      nextAppointment = recommendationEntity.data.nextAppointment,
      previousReleases = recommendationEntity.data.previousReleases,
      previousRecalls = recommendationEntity.data.previousRecalls,
      recallConsideredList = recommendationEntity.data.recallConsideredList,
      currentRoshForPartA = recommendationEntity.data.currentRoshForPartA,
      roshSummary = recommendationEntity.data.roshSummary
    )
  }

  @kotlin.Throws(Exception::class)
  @OptIn(ExperimentalStdlibApi::class)
  suspend fun updateRecommendationWithManagerRecallDecision(
    jsonRequest: JsonNode?,
    recommendationId: Long,
    userId: String?,
    readableUserName: String?,
  ): RecommendationResponse {
    validateManagerRecallDecision(jsonRequest)
    val existingRecommendationEntity = getRecommendationEntityById(recommendationId)
    val userAccessResponse = existingRecommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {

      val updatedRecommendation: RecommendationModel = recommendationFromRequest(existingRecommendationEntity, jsonRequest)
      existingRecommendationEntity.data.managerRecallDecision = updatedRecommendation.managerRecallDecision
        ?.copy(
          createdDate = utcNowDateTimeString(),
          createdBy = readableUserName
        )

      if (existingRecommendationEntity.data.managerRecallDecision?.isSentToDelius == true) {
        log.info("About to send domain event for ${updatedRecommendation.crn} on manager recall decision made for recommendationId $recommendationId")
        try {
          sendManagerRecallDecisionMadeEvent(
            crn = existingRecommendationEntity.data.crn,
            contactOutcome = toDeliusContactOutcome(existingRecommendationEntity.data.managerRecallDecision?.selected?.value).toString(),
            staffcode = getValueAndHandleWrappedException(userId?.let { communityApiClient.getStaffDetails(it) })?.staffCode
          )
        } catch (ex: Exception) {
          log.info("Failed to send domain event for ${updatedRecommendation.crn} on manager recall decision for recommendationId $recommendationId reverting isSentToDelius to false")
          existingRecommendationEntity.data.managerRecallDecision?.isSentToDelius = false
          updateAndSaveRecommendation(existingRecommendationEntity, userId, readableUserName)
          throw ex
        }
        log.info("Sent domain event for ${existingRecommendationEntity.data.crn} on manager recall decision made asynchronously for recommendationId $recommendationId")
      }
      val result = updateAndSaveRecommendation(existingRecommendationEntity, userId, readableUserName)
      log.info("recommendation for ${result.crn} updated with manager recall decision for recommendationId $recommendationId")
      return result
    }
  }

  suspend fun updateRecommendation(
    jsonRequest: JsonNode?,
    recommendationId: Long,
    userId: String?,
    readableUserName: String?,
    userEmail: String?,
    isPartADownloaded: Boolean,
    isDntrDownloaded: Boolean = false,
    pageRefreshIds: List<String>?,
    featureFlags: FeatureFlags?
  ): RecommendationResponse {
    validateRecallType(jsonRequest)
    val existingRecommendationEntity = getRecommendationEntityById(recommendationId)

    val updatedRecommendation: RecommendationModel = recommendationFromRequest(existingRecommendationEntity, jsonRequest)

    existingRecommendationEntity.data.recallConsideredList = updateRecallConsideredList(
      updatedRecommendation,
      existingRecommendationEntity.data,
      userId,
      readableUserName
    )
    existingRecommendationEntity.data = updatePageReviewedValues(updatedRecommendation, existingRecommendationEntity).data
    refreshData(pageRefreshIds, existingRecommendationEntity.data)

    val result = updateAndSaveRecommendation(existingRecommendationEntity, userId, readableUserName)

    log.info("recommendation for ${result.crn} updated for recommendationId $recommendationId")

    val sendRecommendationStartedDomainEvent =
      featureFlags?.flagDomainEventRecommendationStarted == true && featureFlags.flagConsiderRecall == true && existingRecommendationEntity.data.recommendationStartedDomainEventSent != true

    if (sendRecommendationStartedDomainEvent) {
      log.info("About to send domain event for ${existingRecommendationEntity.data.crn} on Recommendation started for recommendation id $recommendationId")
      sendRecommendationStartedEvent(existingRecommendationEntity.data.crn)
      log.info("Sent domain event for ${existingRecommendationEntity.data.crn} on Recommendation started asynchronously for recommendation id $recommendationId")
      existingRecommendationEntity.data.recommendationStartedDomainEventSent = true
    }

    return result
  }

  @OptIn(ExperimentalStdlibApi::class)
  private suspend fun updateAndSaveRecommendation(
    existingRecommendationEntity: RecommendationEntity,
    userId: String?,
    readableUserName: String?
  ): RecommendationResponse {
    val userAccessResponse = existingRecommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val savedRecommendation = saveRecommendation(existingRecommendationEntity, userId, readableUserName)
      return buildRecommendationResponse(savedRecommendation)
    }
  }

  private fun recommendationFromRequest(
    existingRecommendationEntity: RecommendationEntity,
    jsonRequest: JsonNode?
  ): RecommendationModel {
    val readerForUpdating: ObjectReader = CustomMapper.readerForUpdating(existingRecommendationEntity.data)
    return readerForUpdating.readValue(jsonRequest)
  }

  @Throws(RecommendationUpdateException::class)
  private fun saveRecommendation(
    existingRecommendationEntity: RecommendationEntity,
    userId: String?,
    readableUserName: String?
  ): RecommendationEntity {
    existingRecommendationEntity.data.lastModifiedDate = utcNowDateTimeString()
    existingRecommendationEntity.data.lastModifiedBy = userId
    existingRecommendationEntity.data.lastModifiedByUserName = readableUserName
    val savedRecommendation = try {
      recommendationRepository.save(existingRecommendationEntity)
    } catch (ex: Exception) {
      throw RecommendationUpdateException(
        message = "Update failed for recommendation id:: ${existingRecommendationEntity.id}$ex.message",
        error = RECOMMENDATION_UPDATE_FAILED.toString()
      )
    }
    log.info("recommendation for ${existingRecommendationEntity.data.crn} updated")
    return savedRecommendation
  }

  private fun updateDownloadLetterDataForRecommendation(existingRecommendationEntity: RecommendationEntity, readableUserName: String?, userEmail: String?, isPartADownloaded: Boolean) {
    if (isPartADownloaded) {
      existingRecommendationEntity.data.userNamePartACompletedBy = readableUserName
      existingRecommendationEntity.data.userEmailPartACompletedBy = userEmail
      existingRecommendationEntity.data.lastPartADownloadDateTime = localNowDateTime()
    } else {
      existingRecommendationEntity.data.userNameDntrLetterCompletedBy = readableUserName
      existingRecommendationEntity.data.lastDntrLetterADownloadDateTime = localNowDateTime()
    }
    existingRecommendationEntity.data.status = Status.DOCUMENT_DOWNLOADED
  }

  private fun updateRecallConsideredList(updateRecommendationRequest: RecommendationModel, existingRecommendation: RecommendationModel, username: String?, readableUserName: String?): List<RecallConsidered>? {
    if (updateRecommendationRequest.recallConsideredList != null && updateRecommendationRequest.recallConsideredList?.isNotEmpty() == true) {
      return buildRecallDecisionList(username, readableUserName, updateRecommendationRequest.recallConsideredList!![0].recallConsideredDetail)
    }
    return existingRecommendation.recallConsideredList
  }

  private suspend fun refreshData(pageRefreshIds: List<String>?, model: RecommendationModel) {
    model.previousReleases = getPreviousReleaseDetails(pageRefreshIds, model.crn, model.previousReleases)
    model.previousRecalls = getPreviousRecallDetails(pageRefreshIds, model.crn, model.previousRecalls)
    model.personOnProbation = getPersonalDetails(pageRefreshIds, model.crn, model.personOnProbation)
    model.personOnProbation?.mappa = getMappaDetails(pageRefreshIds, model.crn, model.personOnProbation?.mappa)
    model.roshSummary = getRoshSummaryDetails(pageRefreshIds, model.crn, model.roshSummary)
    model.indexOffenceDetails = getIndexOffenceDetails(pageRefreshIds, model.crn, model.indexOffenceDetails)
    model.convictionDetail = getConvictionDetail(pageRefreshIds, model.crn, model.convictionDetail, model.isExtendedSentence)
  }

  private fun getPreviousReleaseDetails(pageRefreshIds: List<String>?, crn: String?, previousReleases: PreviousReleases?): PreviousReleases? {
    if (pageRefreshIds?.any { it == "previousReleases" } == true && crn != null) {

      val releaseSummaryResponse = getValueAndHandleWrappedException(communityApiClient.getReleaseSummary(crn))

      return PreviousReleases(
        lastReleaseDate = releaseSummaryResponse?.lastRelease?.date,
        lastReleasingPrisonOrCustodialEstablishment = releaseSummaryResponse?.lastRelease?.institution?.institutionName,
        hasBeenReleasedPreviously = previousReleases?.hasBeenReleasedPreviously,
        previousReleaseDates = previousReleases?.previousReleaseDates,
      )
    }
    return previousReleases
  }

  private fun getPreviousRecallDetails(pageRefreshIds: List<String>?, crn: String?, previousRecalls: PreviousRecalls?): PreviousRecalls? {
    if (pageRefreshIds?.any { it == "previousRecalls" } == true && crn != null) {

      val releaseSummaryResponse = getValueAndHandleWrappedException(communityApiClient.getReleaseSummary(crn))

      return PreviousRecalls(
        lastRecallDate = releaseSummaryResponse?.lastRecall?.date,
        hasBeenRecalledPreviously = previousRecalls?.hasBeenRecalledPreviously,
        previousRecallDates = previousRecalls?.previousRecallDates,
      )
    }
    return previousRecalls
  }

  private suspend fun getPersonalDetails(pageRefreshIds: List<String>?, crn: String?, personDetails: PersonOnProbation?): PersonOnProbation? {
    if (pageRefreshIds?.any { it == "personOnProbation" } == true && crn != null) {
      var latestPersonDetails = personDetailsService.getPersonDetails(crn).toPersonOnProbation()
      val existingMappa = personDetails?.mappa
      latestPersonDetails = latestPersonDetails.copy(hasBeenReviewed = personDetails?.hasBeenReviewed, mappa = existingMappa)
      return latestPersonDetails
    }
    return personDetails
  }

  private suspend fun getMappaDetails(pageRefreshIds: List<String>?, crn: String?, mappa: Mappa?): Mappa? {
    if (pageRefreshIds?.any { it == "mappa" } == true && crn != null) {
      var latestMappa = riskService?.getMappa(crn)
      latestMappa = latestMappa?.copy(hasBeenReviewed = mappa?.hasBeenReviewed)
      return latestMappa
    }
    return mappa
  }

  private suspend fun getRoshSummaryDetails(pageRefreshIds: List<String>?, crn: String?, roshSummary: RoshSummary?): RoshSummary? {
    if (pageRefreshIds?.any { it == "riskOfSeriousHarm" } == true && crn != null) {
      return riskService?.getRoshSummary(crn)
    }
    return roshSummary
  }

  private suspend fun getIndexOffenceDetails(pageRefreshIds: List<String>?, crn: String?, indexOffenceDetails: String?): String? {
    if (pageRefreshIds?.any { it == "indexOffenceDetails" } == true && crn != null) {
      val latestIndexOffenceDetails = riskService?.fetchAssessmentInfo(crn = crn, hideOffenceDetailsWhenNoMatch = true)
      return latestIndexOffenceDetails?.offenceDescription
    }
    return indexOffenceDetails
  }

  private suspend fun getConvictionDetail(pageRefreshIds: List<String>?, crn: String?, convictionDetail: ConvictionDetail?, isExtendedSentenceInRecommendation: Boolean?): ConvictionDetail? {
    if (pageRefreshIds?.any { it == "convictionDetail" } == true && crn != null) {
      val latestConvictionResponse = convictionService.buildConvictionResponse(crn, false)

      return buildRecommendationConvictionResponse(
        latestConvictionResponse.filter { it.isCustodial == true },
        convictionDetail?.hasBeenReviewed,
        isExtendedSentenceInRecommendation
      )
    }
    return convictionDetail
  }

  private fun updatePageReviewedValues(
    updateRecommendationRequest: RecommendationModel,
    recommendationEntity: RecommendationEntity
  ): RecommendationEntity {
    val data = recommendationEntity.data
    var personOnProbation = data.personOnProbation
    var convictionDetail = data.convictionDetail
    var mappa = data.personOnProbation?.mappa
    if (updateRecommendationRequest.hasBeenReviewed?.personOnProbation == true) {
      personOnProbation = personOnProbation?.copy(hasBeenReviewed = true) ?: PersonOnProbation(hasBeenReviewed = true)
    }
    if (updateRecommendationRequest.hasBeenReviewed?.mappa == true) {
      mappa = mappa?.copy(hasBeenReviewed = true) ?: Mappa(hasBeenReviewed = true)
    }
    if (updateRecommendationRequest.hasBeenReviewed?.personOnProbation == true) {
      personOnProbation = personOnProbation?.copy(
        hasBeenReviewed = true
      ) ?: PersonOnProbation(hasBeenReviewed = true)
    }
    if (updateRecommendationRequest.hasBeenReviewed?.convictionDetail == true) {
      convictionDetail = convictionDetail?.copy(
        hasBeenReviewed = true
      ) ?: ConvictionDetail(hasBeenReviewed = true)
    }

    return recommendationEntity.copy(
      data = data.copy(
        personOnProbation = personOnProbation?.copy(mappa = mappa),
        convictionDetail = convictionDetail,
        hasBeenReviewed = null
      )
    )
  }

  fun getRecommendationsInProgressForCrn(crn: String): ActiveRecommendation? {
    val recommendationEntity = recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name))
    Collections.sort(recommendationEntity)

    if (recommendationEntity.size > 1) {
      log.error("More than one recommendation found for CRN. Returning the latest.")
    }
    return if (recommendationEntity.isNotEmpty()) {
      ActiveRecommendation(
        recommendationId = recommendationEntity[0].id,
        lastModifiedDate = recommendationEntity[0].data.lastModifiedDate,
        lastModifiedBy = recommendationEntity[0].data.lastModifiedBy,
        lastModifiedByName = recommendationEntity[0].data.lastModifiedByUserName,
        recallType = recommendationEntity[0].data.recallType,
        recallConsideredList = recommendationEntity[0].data.recallConsideredList,
        status = recommendationEntity[0].data.status,
        managerRecallDecision = recommendationEntity[0].data.managerRecallDecision,
      )
    } else {
      null
    }
  }

  suspend fun generateDntr(
    recommendationId: Long,
    userId: String?,
    readableUsername: String?,
    documentRequestType: DocumentRequestType?,
    featureFlags: FeatureFlags?
  ): DocumentResponse {
    return if (documentRequestType == DocumentRequestType.DOWNLOAD_DOC_X) {
      val recommendationEntity = getRecommendationEntityById(recommendationId)
      val isFirstDntrDownload = recommendationEntity.data.userNameDntrLetterCompletedBy == null
      val documentResponse = generateDntrDownload(recommendationEntity, userId, readableUsername)

      if (featureFlags?.flagSendDomainEvent == true && isFirstDntrDownload) {
        log.info("Sent domain event for DNTR download asynchronously")
        sendDntrDownloadEvent(recommendationId)
        log.info("Sent domain event for DNTR download asynchronously")
      }
      documentResponse
    } else {
      generateDntrPreview(recommendationId)
    }
  }

  private fun sendMrdEventToEventsEmitter(mrdEvent: MrdEvent) {
    mrdEventsEmitter?.sendEvent(mrdEvent)
  }

  private fun sendManagerRecallDecisionMadeEvent(crn: String?, contactOutcome: String?, staffcode: String?) {
    sendMrdEventToEventsEmitter(
      toManagerRecallDecisionMadeEventPayload(
        crn = crn,
        recommendationUrl = "$mrdUrl/cases/$crn/overview",
        contactOutcome = contactOutcome,
        staffCode = staffcode
      )
    )
  }

  private fun sendRecommendationStartedEvent(crn: String?) {
    sendMrdEventToEventsEmitter(
      toRecommendationStartedEventPayload("$mrdUrl/cases/$crn/overview", crn)
    )
  }

  private fun sendDntrDownloadEvent(recommendationId: Long) {
    val crn = recommendationRepository.findById(recommendationId).map { it.data.crn }.get()
    sendMrdEventToEventsEmitter(
      toDntrDownloadedEventPayload(crn)
    )
  }

  private suspend fun generateDntrDownload(recommendationEntity: RecommendationEntity, userId: String?, readableUsername: String?,): DocumentResponse {

    val recommendationResponse = if (recommendationEntity.data.userNameDntrLetterCompletedBy == null) {
      updateDownloadLetterDataForRecommendation(recommendationEntity, readableUsername, null, false)
      updateAndSaveRecommendation(recommendationEntity, userId, readableUsername)
    } else {
      buildRecommendationResponse(recommendationEntity)
    }

    val userAccessResponse = recommendationResponse.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val fileContents =
        templateReplacementService.generateDocFromRecommendation(recommendationResponse, DocumentType.DNTR_DOCUMENT)
      DocumentResponse(
        fileName = generateDocumentFileName(recommendationResponse, "No_Recall"),
        fileContents = fileContents
      )
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  suspend fun generateDntrPreview(recommendationId: Long): DocumentResponse {
    val recommendationResponse = getRecommendationResponseById(recommendationId)
    val userAccessResponse = recommendationResponse.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val letterContent =
        templateReplacementService.generateLetterContentForPreviewFromRecommendation(recommendationResponse)
      DocumentResponse(
        letterContent = letterContent
      )
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  suspend fun generatePartA(recommendationId: Long, userId: String?, readableUsername: String?, userEmail: String?): DocumentResponse {
    val recommendationEntity = getRecommendationEntityById(recommendationId)

    val recommendationResponse = if (recommendationEntity.data.userNamePartACompletedBy == null) {
      updateDownloadLetterDataForRecommendation(recommendationEntity, readableUsername, userEmail, true)
      updateAndSaveRecommendation(recommendationEntity, userId, readableUsername)
    } else {
      buildRecommendationResponse(recommendationEntity)
    }

    val userAccessResponse = recommendationResponse.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val fileContents =
        templateReplacementService.generateDocFromRecommendation(recommendationResponse, DocumentType.PART_A_DOCUMENT)
      return DocumentResponse(
        fileName = generateDocumentFileName(recommendationResponse, "NAT_Recall_Part_A"),
        fileContents = fileContents
      )
    }
  }

  private fun generateDocumentFileName(recommendation: RecommendationResponse, prefix: String): String {
    val surname = recommendation.personOnProbation?.surname ?: ""
    val firstName =
      if (recommendation.personOnProbation?.firstName != null && recommendation.personOnProbation.firstName.isNotEmpty()) {
        recommendation.personOnProbation.firstName.subSequence(0, 1)
      } else ""
    val crn = recommendation.crn ?: ""

    return "${prefix}_${nowDate()}_${surname}_${firstName}_$crn.docx"
  }

  private fun saveNewRecommendationEntity(
    recommendationRequest: CreateRecommendationRequest,
    userId: String?,
    readableNameOfUser: String?,
    status: Status?,
    recallConsideredList: List<RecallConsidered>?,
    recommendationWrapper: StaticRecommendationDataWrapper?,
    recommendationStartedDomainEventSent: Boolean?
  ): RecommendationEntity? {
    val now = utcNowDateTimeString()
    val recommendationEntity = RecommendationEntity(
      data = RecommendationModel(
        crn = recommendationRequest.crn,
        recallConsideredList = recallConsideredList,
        status = status,
        lastModifiedBy = userId,
        lastModifiedByUserName = readableNameOfUser,
        lastModifiedDate = now,
        createdBy = userId,
        createdDate = now,
        personOnProbation = recommendationWrapper?.personOnProbation,
        region = recommendationWrapper?.region,
        localDeliveryUnit = recommendationWrapper?.localDeliveryUnit,
        recommendationStartedDomainEventSent = recommendationStartedDomainEventSent
      )
    )

    return recommendationRepository.save(recommendationEntity)
  }

  private fun buildRecommendationConvictionResponse(convictionResponse: List<ConvictionResponse>?, hasBeenReviewed: Boolean? = false, isExtendedSentenceInRecommendation: Boolean?): ConvictionDetail? {
    if (convictionResponse?.size == 1) {

      val mainOffence = convictionResponse[0].offences?.filter { it.mainOffence == true }?.get(0)
      val (custodialTerm, extendedTerm) = extendedSentenceDetails(convictionResponse[0], isExtendedSentenceInRecommendation)

      return ConvictionDetail(
        mainOffence?.description,
        mainOffence?.offenceDate,
        convictionResponse[0].sentenceStartDate,
        convictionResponse[0].sentenceOriginalLength,
        convictionResponse[0].sentenceOriginalLengthUnits,
        convictionResponse[0].sentenceDescription,
        convictionResponse[0].licenceExpiryDate,
        convictionResponse[0].sentenceExpiryDate,
        convictionResponse[0].sentenceSecondLength,
        convictionResponse[0].sentenceSecondLengthUnits,
        custodialTerm,
        extendedTerm,
        hasBeenReviewed
      )
    }
    return null
  }

  private fun extendedSentenceDetails(conviction: ConvictionResponse?, isExtendedSentenceInRecommendation: Boolean?): Pair<String?, String?> {
    return if ("Extended Determinate Sentence" == conviction?.sentenceDescription ||
      "CJA - Extended Sentence" == conviction?.sentenceDescription ||
      isExtendedSentenceInRecommendation == true
    ) {
      val custodialTerm = conviction?.sentenceOriginalLength?.toString() + MrdTextConstants.WHITE_SPACE + conviction?.sentenceOriginalLengthUnits
      val sentenceSecondLength = conviction?.sentenceSecondLength?.toString() ?: MrdTextConstants.EMPTY_STRING
      val sentenceSecondLengthUnits = conviction?.sentenceSecondLengthUnits ?: MrdTextConstants.EMPTY_STRING

      Pair(custodialTerm, sentenceSecondLength + MrdTextConstants.WHITE_SPACE + sentenceSecondLengthUnits)
    } else Pair(null, null)
  }

  @Throws(InvalidRequestException::class)
  private fun validateRecallType(jsonRequest: JsonNode?) {
    val selectedRecallType = jsonRequest?.get("recallType")?.get("selected")?.get("value")?.textValue()
    checkRecallTypesAreValid(selectedRecallType, jsonRequest)
  }

  @Throws(InvalidRequestException::class)
  private fun validateManagerRecallDecision(jsonRequest: JsonNode?) {
    val selectedRecallType = jsonRequest?.get("managerRecallDecision")?.get("selected")?.get("value")?.textValue()
    checkRecallTypesAreValid(selectedRecallType, jsonRequest)
  }

  private fun checkRecallTypesAreValid(
    selectedRecallType: String?,
    jsonRequest: JsonNode?
  ) {
    if (selectedRecallType != null) {
      val allOptions = jsonRequest?.get("recallType")?.get("allOptions")
      val allOptionsList = allOptions?.map { it.get("value").asText() }?.toList()
      val valid = allOptionsList?.any { it == selectedRecallType }
      val errorMessage =
        "$selectedRecallType is not a valid recall type, available types are ${allOptionsList?.joinToString(",")}"
      if (valid == false) throw InvalidRequestException(errorMessage)
    }
  }

  fun getRecommendations(crn: String): RecommendationsResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      RecommendationsResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val recommendationDetails = getRecommendationsInProgressForCrn(crn)
      val recommendations = recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name, Status.DOCUMENT_DOWNLOADED.name))

      return RecommendationsResponse(
        personalDetailsOverview = personalDetailsOverview,
        activeRecommendation = recommendationDetails,
        recommendations = buildRecommendationsResponse(recommendations)
      )
    }
  }

  private fun buildRecommendationsResponse(recommendationEntityList: List<RecommendationEntity>?): List<RecommendationsListItem>? {
    val sorted = recommendationEntityList?.sortedBy {
      OffsetDateTime.parse(it.data.lastModifiedDate).toLocalDateTime()
    }?.reversed()

    return sorted
      ?.map {
        RecommendationsListItem(
          recommendationId = it.id,
          lastModifiedByName = it.data.lastModifiedByUserName,
          createdDate = it.data.createdDate,
          lastModifiedDate = it.data.lastModifiedDate,
          status = it.data.status,
          recallType = it.data.recallType
        )
      }
  }
}
