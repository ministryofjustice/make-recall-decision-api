package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectReader
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.RecommendationModel.ConvictionDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.toPersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentRequestType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecision
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecisionTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecisionTypeValue
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationHistoryEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.toRecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.toRecommendationStatusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationHistoryRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader.CustomMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.dateTimeWithDaylightSavingFromString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.localNowDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.nowDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.utcNowDateTimeString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.jvm.optionals.getOrNull
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.RecommendationModel as DeliusRecommendationModel

@Transactional
@Service
internal class RecommendationService(
  val recommendationRepository: RecommendationRepository,
  val recommendationStatusRepository: RecommendationStatusRepository,
  @Lazy val personDetailsService: PersonDetailsService,
  val templateReplacementService: TemplateReplacementService,
  private val userAccessValidator: UserAccessValidator,
  @Lazy private val riskService: RiskService?,
  private val deliusClient: DeliusClient,
  private val mrdEventsEmitter: MrdEventsEmitter?,
  @Value("\${mrd.url}") private val mrdUrl: String? = null,
  @Value("\${mrd.api.url}") private val mrdApiUrl: String? = null,
  val recommendationHistoryRepository: RecommendationHistoryRepository? = null,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createRecommendation(
    recommendationRequest: CreateRecommendationRequest,
    userId: String?,
    readableNameOfUser: String?,
    featureFlags: FeatureFlags?,
  ): RecommendationResponse? {
    val userAccessResponse = recommendationRequest.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      return RecommendationResponse(userAccessResponse = userAccessResponse)
    } else {
      val status = if (featureFlags?.flagConsiderRecall == true) Status.RECALL_CONSIDERED else Status.DRAFT
      val recallConsideredList = if (featureFlags?.flagConsiderRecall == true) {
        listOf(
          RecallConsidered(
            userId = userId,
            createdDate = utcNowDateTimeString(),
            userName = readableNameOfUser,
            recallConsideredDetail = recommendationRequest.recallConsideredDetail,
          ),
        )
      } else {
        null
      }
      val sendRecommendationStartedDomainEvent =
        featureFlags?.flagDomainEventRecommendationStarted == true && featureFlags.flagConsiderRecall == false
      if (sendRecommendationStartedDomainEvent) {
        log.info("About to send domain event for ${recommendationRequest.crn} on Recommendation started")
        sendRecommendationStartedEvent(recommendationRequest.crn)
        log.info("Sent domain event for ${recommendationRequest.crn} on Recommendation started asynchronously")
      }
      val personDetails = recommendationRequest.crn?.let { personDetailsService.getPersonDetails(it) }
      return saveNewRecommendationEntity(
        recommendationRequest,
        userId,
        readableNameOfUser,
        status,
        recallConsideredList,
        StaticRecommendationDataWrapper(
          personDetails?.toPersonOnProbation(),
          personDetails?.offenderManager?.probationAreaDescription,
          personDetails?.offenderManager?.probationTeam?.localDeliveryUnitDescription,
        ),
        sendRecommendationStartedDomainEvent,
      )?.toRecommendationResponse()
    }
  }

  fun buildRecallDecisionList(
    username: String?,
    readableUsername: String?,
    recallConsideredDetail: String?,
  ): List<RecallConsidered> {
    return listOf(
      RecallConsidered(
        userId = username,
        createdDate = utcNowDateTimeString(),
        userName = readableUsername,
        recallConsideredDetail = recallConsideredDetail,
      ),
    )
  }

  fun getRecommendation(recommendationId: Long): RecommendationResponse {
    val recommendationResponse = getRecommendationResponseById(recommendationId)
    val userAccessResponse = recommendationResponse.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      RecommendationResponse(
        userAccessResponse,
      )
    } else {
      recommendationResponse
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun getRecommendationResponseById(recommendationId: Long): RecommendationResponse {
    val recommendationEntity = getRecommendationEntityById(recommendationId)

    return buildRecommendationResponse(recommendationEntity)
  }

  fun getLatestCompleteRecommendationOverview(crn: String): RecommendationsResponse {
    val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)

    val completedRecommendation = getLatestRecommendationEntity(crn)

    return RecommendationsResponse(
      personalDetailsOverview = personalDetailsOverview,
      recommendations = completedRecommendation?.let {
        listOf(
          RecommendationsListItem(
            recommendationId = it.id,
            lastModifiedByName = it.data.lastModifiedByUserName,
            createdDate = it.data.createdDate,
            lastModifiedDate = it.data.lastModifiedDate,
            status = it.data.status,
            statuses = recommendationStatusRepository.findByRecommendationId(it.id),
            recallType = it.data.recallType,
            deleted = it.data.deleted,
          ),
        )
      },
    )
  }

  private fun getLatestRecommendationEntity(crn: String): RecommendationEntity? {
    return recommendationRepository.findByCrn(crn)
      .filter {
        recommendationStatusRepository.findByRecommendationId(it.id).stream().anyMatch() { it.name == "COMPLETED" }
      }.minOrNull()
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun getRecommendationEntityById(recommendationId: Long): RecommendationEntity {
    return recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
  }

  private fun buildRecommendationResponse(recommendationEntity: RecommendationEntity): RecommendationResponse {
    return RecommendationResponse(
      id = recommendationEntity.id,
      createdByUserFullName = recommendationEntity.data.createdByUserFullName,
      createdBy = recommendationEntity.data.createdBy,
      createdDate = recommendationEntity.data.createdDate,
      crn = recommendationEntity.data.crn,
      sensitive = recommendationEntity.data.sensitive,
      reviewPractitionersConcerns = recommendationEntity.data.reviewPractitionersConcerns,
      spoRecallType = recommendationEntity.data.spoRecallType,
      spoRecallRationale = recommendationEntity.data.spoRecallRationale,
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
      cvlLicenceConditionsBreached = recommendationEntity.data.cvlLicenceConditionsBreached,
      underIntegratedOffenderManagement = recommendationEntity.data.underIntegratedOffenderManagement,
      localPoliceContact = recommendationEntity.data.localPoliceContact,
      vulnerabilities = recommendationEntity.data.vulnerabilities,
      convictionDetail = recommendationEntity.data.convictionDetail,
      region = recommendationEntity.data.region,
      localDeliveryUnit = recommendationEntity.data.localDeliveryUnit,
      userNameDntrLetterCompletedBy = recommendationEntity.data.userNameDntrLetterCompletedBy,
      lastDntrLetterDownloadDateTime = recommendationEntity.data.lastDntrLetterADownloadDateTime,
      indexOffenceDetails = recommendationEntity.data.indexOffenceDetails,
      offenceDataFromLatestCompleteAssessment = recommendationEntity.data.offenceDataFromLatestCompleteAssessment,
      offencesMatch = recommendationEntity.data.offencesMatch,
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
      roshSummary = recommendationEntity.data.roshSummary,
      countersignSpoTelephone = recommendationEntity.data.countersignSpoTelephone,
      countersignSpoExposition = recommendationEntity.data.countersignSpoExposition,
      countersignAcoTelephone = recommendationEntity.data.countersignAcoTelephone,
      countersignAcoExposition = recommendationEntity.data.countersignAcoExposition,
      whoCompletedPartA = recommendationEntity.data.whoCompletedPartA,
      practitionerForPartA = recommendationEntity.data.practitionerForPartA,
      revocationOrderRecipients = recommendationEntity.data.revocationOrderRecipients,
      ppcsQueryEmails = recommendationEntity.data.ppcsQueryEmails,
      prisonApiLocationDescription = recommendationEntity.data.prisonApiLocationDescription,

      releaseUnderECSL = recommendationEntity.data.releaseUnderECSL,
      dateOfRelease = recommendationEntity.data.dateOfRelease,
      conditionalReleaseDate = recommendationEntity.data.conditionalReleaseDate,
      deleted = recommendationEntity.deleted,
    )
  }

  @Deprecated("Now using updateRecommendation")
  @kotlin.Throws(Exception::class)
  @OptIn(ExperimentalStdlibApi::class)
  suspend fun updateRecommendationWithManagerRecallDecision(
    jsonRequest: JsonNode?,
    recommendationId: Long,
    userId: String,
    readableUserName: String?,
  ): RecommendationResponse {
    validateManagerRecallDecision(jsonRequest)
    val existingRecommendationEntity = getRecommendationEntityById(recommendationId)
    val userAccessResponse = existingRecommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      RecommendationResponse(userAccessResponse = userAccessResponse)
    } else {
      val updatedRecommendation: RecommendationModel =
        recommendationFromRequest(existingRecommendationEntity, jsonRequest)
      existingRecommendationEntity.data.managerRecallDecision = updatedRecommendation.managerRecallDecision
        ?.copy(
          createdDate = utcNowDateTimeString(),
          createdBy = readableUserName,
        )
      val result = updateAndSaveRecommendation(existingRecommendationEntity, userId, readableUserName)
      log.info("recommendation for ${result.crn} updated with manager recall decision for recommendationId $recommendationId")
      result
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
    pageRefreshIds: List<String>,
    featureFlags: FeatureFlags?,
  ): RecommendationResponse {
    validateRecallType(jsonRequest)
    val existingRecommendationEntity = getRecommendationEntityById(recommendationId)
    val updatedRecommendation: RecommendationModel =
      recommendationFromRequest(existingRecommendationEntity, jsonRequest)

    val requestJson = JSONObject(jsonRequest.toString())
    val sendToDelius = requestJson.has("sendSpoRationaleToDelius") && requestJson.getBoolean("sendSpoRationaleToDelius")
    log.info("sendSpoRationaleToDelius present::" + requestJson.has("sendSpoRationaleToDelius"))
    log.info("send to delius value::$sendToDelius")
    if (sendToDelius) {
      existingRecommendationEntity.data =
        addSpoRationale(existingRecommendationEntity, updatedRecommendation, readableUserName)
      sendManagementOversightDomainEvent(recommendationId, existingRecommendationEntity, userId)
    }
    existingRecommendationEntity.deleted = requestJson.has("status") && requestJson.getString("status") == "DELETED"
    existingRecommendationEntity.data.recallConsideredList = updateRecallConsideredList(
      updatedRecommendation,
      existingRecommendationEntity.data,
      userId,
      readableUserName,
    )
    existingRecommendationEntity.data =
      updatePageReviewedValues(updatedRecommendation, existingRecommendationEntity).data
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

  private fun addSpoRationale(
    existingRecommendationEntity: RecommendationEntity,
    updatedRecommendation: RecommendationModel,
    readableUserName: String?,
  ): RecommendationModel {
    return existingRecommendationEntity.data.copy(
      managerRecallDecision = ManagerRecallDecision(
        isSentToDelius = true,
        selected = ManagerRecallDecisionTypeSelectedValue(
          value = ManagerRecallDecisionTypeValue.valueOf(updatedRecommendation.spoRecallType!!),
          details = updatedRecommendation.spoRecallRationale,
        ),
        createdBy = readableUserName,
        createdDate = utcNowDateTimeString(),
      ),
    )
  }

  private fun sendManagementOversightDomainEvent(
    recommendationId: Long,
    existingRecommendationEntity: RecommendationEntity,
    userId: String?,
  ) {
    log.info("About to send domain event for ${existingRecommendationEntity.data.crn} on manager recall decision made for recommendationId $recommendationId")
    try {
      sendManagerRecallDecisionMadeEvent(
        crn = existingRecommendationEntity.data.crn,
        contactOutcome = toDeliusContactOutcome(existingRecommendationEntity.data.spoRecallType).toString(),
        username = userId ?: MrdTextConstants.EMPTY_STRING,
      )
    } catch (ex: Exception) {
      log.info("Failed to send domain event for ${existingRecommendationEntity.data.crn} on manager recall decision for recommendationId $recommendationId reverting isSentToDelius to false")
      throw ex
    }
    log.info("Sent domain event for ${existingRecommendationEntity.data.crn} on manager recall decision made asynchronously for recommendationId $recommendationId")
  }

  @OptIn(ExperimentalStdlibApi::class)
  private suspend fun updateAndSaveRecommendation(
    existingRecommendationEntity: RecommendationEntity,
    userId: String?,
    readableUserName: String?,
  ): RecommendationResponse {
    val userAccessResponse = existingRecommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      RecommendationResponse(userAccessResponse = userAccessResponse)
    } else {
      val savedRecommendation = saveRecommendation(existingRecommendationEntity, userId, readableUserName)
      auditUpdate(savedRecommendation, userId, readableUserName)
      buildRecommendationResponse(savedRecommendation)
    }
  }

  private fun auditUpdate(
    savedRecommendation: RecommendationEntity,
    userId: String?,
    readableUserName: String?,
  ) {
    recommendationHistoryRepository?.save(
      RecommendationHistoryEntity(
        recommendationId = savedRecommendation.id,
        modifiedBy = userId,
        modifiedByUserFullName = readableUserName,
        modified = utcNowDateTimeString(),
        recommendation = savedRecommendation.data,
      ),
    )
  }

  private fun recommendationFromRequest(
    existingRecommendationEntity: RecommendationEntity,
    jsonRequest: JsonNode?,
  ): RecommendationModel {
    val readerForUpdating: ObjectReader = CustomMapper.readerForUpdating(existingRecommendationEntity.data)
    return readerForUpdating.readValue(jsonRequest)
  }

  @Throws(RecommendationUpdateException::class)
  private fun saveRecommendation(
    existingRecommendationEntity: RecommendationEntity,
    userId: String?,
    readableUserName: String?,
  ): RecommendationEntity {
    existingRecommendationEntity.data.lastModifiedDate = utcNowDateTimeString()
    existingRecommendationEntity.data.lastModifiedBy = userId
    existingRecommendationEntity.data.lastModifiedByUserName = readableUserName
    val savedRecommendation = try {
      recommendationRepository.save(existingRecommendationEntity)
    } catch (ex: Exception) {
      throw RecommendationUpdateException(
        message = "Update failed for recommendation id:: ${existingRecommendationEntity.id}$ex.message",
        error = RECOMMENDATION_UPDATE_FAILED.toString(),
      )
    }
    log.info("recommendation for ${existingRecommendationEntity.data.crn} updated")
    return savedRecommendation
  }

  private fun updateDownloadLetterDataForRecommendation(
    existingRecommendationEntity: RecommendationEntity,
    readableUserName: String?,
    isPartADownloaded: Boolean,
  ) {
    if (isPartADownloaded) {
      existingRecommendationEntity.data.lastPartADownloadDateTime = localNowDateTime()
    } else {
      existingRecommendationEntity.data.userNameDntrLetterCompletedBy = readableUserName
      existingRecommendationEntity.data.lastDntrLetterADownloadDateTime = localNowDateTime()
    }
  }

  private fun updateRecallConsideredList(
    updateRecommendationRequest: RecommendationModel,
    existingRecommendation: RecommendationModel,
    username: String?,
    readableUserName: String?,
  ): List<RecallConsidered>? {
    if (updateRecommendationRequest.recallConsideredList != null && updateRecommendationRequest.recallConsideredList?.isNotEmpty() == true) {
      return buildRecallDecisionList(
        username,
        readableUserName,
        updateRecommendationRequest.recallConsideredList!![0].recallConsideredDetail,
      )
    }
    return existingRecommendation.recallConsideredList
  }

  private suspend fun refreshData(pageRefreshIds: List<String>, model: RecommendationModel) {
    model.crn?.let {
      val deliusDetails = lazy { deliusClient.getRecommendationModel(model.crn) }
      if ("previousReleases" in pageRefreshIds) model.refreshPreviousReleases(deliusDetails.value)
      if ("previousRecalls" in pageRefreshIds) model.refreshPreviousRecalls(deliusDetails.value)
      if ("personOnProbation" in pageRefreshIds) model.refreshPersonOnProbation(deliusDetails.value)
      if ("mappa" in pageRefreshIds) model.refreshMappa(deliusDetails.value)
      if ("riskOfSeriousHarm" in pageRefreshIds) model.refreshRoshSummary(model.crn)
      if ("indexOffenceDetails" in pageRefreshIds) model.refreshIndexOffenceDetails(model.crn, deliusDetails.value)
      if ("convictionDetail" in pageRefreshIds) model.refreshConvictionDetail(deliusDetails.value)
    }
  }

  private fun RecommendationModel.refreshPreviousReleases(deliusDetails: DeliusRecommendationModel) {
    previousReleases = PreviousReleases(
      lastReleaseDate = deliusDetails.lastRelease?.releaseDate,
      lastReleasingPrisonOrCustodialEstablishment = deliusDetails.lastReleasedFromInstitution?.name,
      hasBeenReleasedPreviously = previousReleases?.hasBeenReleasedPreviously,
      previousReleaseDates = previousReleases?.previousReleaseDates,
    )
  }

  private fun RecommendationModel.refreshPreviousRecalls(deliusDetails: DeliusRecommendationModel) {
    previousRecalls = PreviousRecalls(
      lastRecallDate = deliusDetails.lastRelease?.recallDate,
      hasBeenRecalledPreviously = previousRecalls?.hasBeenRecalledPreviously,
      previousRecallDates = previousRecalls?.previousRecallDates,
    )
  }

  private fun RecommendationModel.refreshPersonOnProbation(deliusDetails: DeliusRecommendationModel) {
    personOnProbation = deliusDetails.toPersonOnProbation().copy(
      hasBeenReviewed = personOnProbation?.hasBeenReviewed,
      mappa = personOnProbation?.mappa,
    )
  }

  private fun RecommendationModel.refreshMappa(deliusDetails: DeliusRecommendationModel) {
    personOnProbation?.mappa = deliusDetails.mappa?.let {
      Mappa(
        level = deliusDetails.mappa.level,
        lastUpdatedDate = deliusDetails.mappa.startDate,
        category = deliusDetails.mappa.category,
        hasBeenReviewed = personOnProbation?.mappa?.hasBeenReviewed,
      )
    }
  }

  private suspend fun RecommendationModel.refreshRoshSummary(crn: String) {
    roshSummary = riskService?.getRoshSummary(crn)
  }

  private suspend fun RecommendationModel.refreshIndexOffenceDetails(
    crn: String,
    deliusDetails: DeliusRecommendationModel,
  ) {
    val assessmentInfo = riskService?.fetchAssessmentInfo(crn, deliusDetails.activeConvictions)

    indexOffenceDetails = assessmentInfo?.offenceDescription
    offenceDataFromLatestCompleteAssessment = assessmentInfo?.offenceDataFromLatestCompleteAssessment
    offencesMatch = assessmentInfo?.offencesMatch
  }

  private fun RecommendationModel.refreshConvictionDetail(deliusDetails: DeliusRecommendationModel) {
    convictionDetail = buildRecommendationConvictionResponse(
      deliusDetails.activeCustodialConvictions,
      convictionDetail?.hasBeenReviewed,
      isExtendedSentence,
    )
  }

  private fun updatePageReviewedValues(
    updateRecommendationRequest: RecommendationModel,
    recommendationEntity: RecommendationEntity,
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
        hasBeenReviewed = true,
      ) ?: PersonOnProbation(hasBeenReviewed = true)
    }
    if (updateRecommendationRequest.hasBeenReviewed?.convictionDetail == true) {
      convictionDetail = convictionDetail?.copy(
        hasBeenReviewed = true,
      ) ?: ConvictionDetail(hasBeenReviewed = true)
    }

    return recommendationEntity.copy(
      data = data.copy(
        personOnProbation = personOnProbation?.copy(mappa = mappa),
        convictionDetail = convictionDetail,
        hasBeenReviewed = null,
      ),
    )
  }

  fun getRecommendationsInProgressForCrn(crn: String): ActiveRecommendation? {
    val recommendationEntity =
      recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name))
        .sorted()
        .filter { isStatusOpen(it) }

    val legacyRecommendationOpen = recommendationEntity.size > 1
    val legacyStatusOpen = recommendationEntity.isNotEmpty() &&
      recommendationStatusRepository.findByRecommendationId(recommendationEntity[0].id)
        .any { it.active && (it.name != "CLOSED" && it.name != "DELETED") }

    if (legacyRecommendationOpen && legacyStatusOpen) {
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
    isUserSpoOrAco: Boolean? = false,
    featureFlags: FeatureFlags = FeatureFlags(),
  ): DocumentResponse {
    return if (documentRequestType == DocumentRequestType.DOWNLOAD_DOC_X) {
      val recommendationEntity = getRecommendationEntityById(recommendationId)
      val isFirstDntrDownload = recommendationEntity.data.userNameDntrLetterCompletedBy == null
      val documentResponse =
        generateDntrDownload(recommendationEntity, userId, readableUsername, recommendationId, featureFlags)
      if (featureFlags.flagSendDomainEvent && isFirstDntrDownload) {
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

  private fun sendManagerRecallDecisionMadeEvent(crn: String?, contactOutcome: String?, username: String) {
    sendMrdEventToEventsEmitter(
      toManagerRecallDecisionMadeEventPayload(
        crn = crn,
        recommendationUrl = "$mrdUrl/cases/$crn/overview",
        contactOutcome = contactOutcome,
        username = username,
        detailUrl = "$mrdApiUrl/managementOversight/$crn",
      ),
    )
  }

  private fun sendRecommendationStartedEvent(crn: String?) {
    sendMrdEventToEventsEmitter(
      toRecommendationStartedEventPayload("$mrdUrl/cases/$crn/overview", crn),
    )
  }

  private fun sendDntrDownloadEvent(recommendationId: Long) {
    val crn = recommendationRepository.findById(recommendationId).map { it.data.crn }.get()
    sendMrdEventToEventsEmitter(
      toDntrDownloadedEventPayload(crn),
    )
  }

  private suspend fun generateDntrDownload(
    recommendationEntity: RecommendationEntity,
    userId: String?,
    readableUsername: String?,
    recommendationId: Long,
    flags: FeatureFlags,
  ): DocumentResponse {
    val recommendationResponse = if (recommendationEntity.data.userNameDntrLetterCompletedBy == null) {
      updateDownloadLetterDataForRecommendation(recommendationEntity, readableUsername, false)
      updateAndSaveRecommendation(recommendationEntity, userId, readableUsername)
    } else {
      buildRecommendationResponse(recommendationEntity)
    }

    val userAccessResponse = recommendationResponse.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      DocumentResponse(userAccessResponse)
    } else {
      val metaData = getRecDocMetaData(recommendationId)
      val fileContents =
        templateReplacementService.generateDocFromRecommendation(
          recommendationResponse,
          DocumentType.DNTR_DOCUMENT,
          metaData,
          flags,
        )
      DocumentResponse(
        fileName = generateDocumentFileName(recommendationResponse, "No_Recall"),
        fileContents = fileContents,
      )
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  suspend fun generateDntrPreview(recommendationId: Long): DocumentResponse {
    val recommendationResponse = getRecommendationResponseById(recommendationId)
    val userAccessResponse = recommendationResponse.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      DocumentResponse(userAccessResponse)
    } else {
      val letterContent =
        templateReplacementService.generateLetterContentForPreviewFromRecommendation(recommendationResponse)
      DocumentResponse(
        letterContent = letterContent,
      )
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  suspend fun generatePartA(
    recommendationId: Long,
    userId: String?,
    readableUsername: String?,
    preview: Boolean = false,
    isUserSpoOrAco: Boolean? = false,
    featureFlags: FeatureFlags = FeatureFlags(),
  ): DocumentResponse {
    val recommendationEntity = getRecommendationEntityById(recommendationId)
    val recommendationResponse = buildRecommendationResponse(recommendationEntity)
    val userAccessResponse = recommendationResponse.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      DocumentResponse(userAccessResponse)
    } else {
      val metaData = getRecDocMetaData(recommendationId)
      val fileContents =
        templateReplacementService.generateDocFromRecommendation(
          recommendationResponse,
          if (preview) DocumentType.PREVIEW_PART_A_DOCUMENT else DocumentType.PART_A_DOCUMENT,
          metaData,
          featureFlags,
        )
      log.info("responding with file: " + if (preview) "Preview_NAT_Recall_Part_A" else "NAT_Recall_Part_A")
      DocumentResponse(
        fileName = generateDocumentFileName(
          recommendationResponse,
          if (preview) "Preview_NAT_Recall_Part_A" else "NAT_Recall_Part_A",
        ),
        fileContents = fileContents,
      )
    }
  }

  private fun getRecDocMetaData(
    recommendationId: Long,
  ): RecommendationMetaData {
    val statuses = recommendationStatusRepository.findByRecommendationId(recommendationId)
      .map { it.toRecommendationStatusResponse() }
    return RecommendationMetaData().fromFetchRecommendationsStatusResponse(statuses)
  }

  private fun generateDocumentFileName(recommendation: RecommendationResponse, prefix: String): String {
    val surname = recommendation.personOnProbation?.surname ?: ""
    val firstName =
      if (recommendation.personOnProbation?.firstName != null && recommendation.personOnProbation.firstName.isNotEmpty()) {
        recommendation.personOnProbation.firstName.subSequence(0, 1)
      } else {
        ""
      }
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
    recommendationStartedDomainEventSent: Boolean?,
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
        createdByUserFullName = readableNameOfUser,
        personOnProbation = recommendationWrapper?.personOnProbation,
        region = recommendationWrapper?.region,
        localDeliveryUnit = recommendationWrapper?.localDeliveryUnit,
        recommendationStartedDomainEventSent = recommendationStartedDomainEventSent,
      ),
    )

    return recommendationRepository.save(recommendationEntity)
  }

  private fun buildRecommendationConvictionResponse(
    convictionResponse: List<ConvictionDetails>,
    hasBeenReviewed: Boolean? = false,
    isExtendedSentenceInRecommendation: Boolean?,
  ): ConvictionDetail? {
    if (convictionResponse.size == 1) {
      val mainOffence = convictionResponse[0].mainOffence
      val (custodialTerm, extendedTerm) = extendedSentenceDetails(
        convictionResponse[0],
        isExtendedSentenceInRecommendation,
      )

      return ConvictionDetail(
        mainOffence.description,
        mainOffence.date,
        convictionResponse[0].sentence.startDate,
        convictionResponse[0].sentence.length,
        convictionResponse[0].sentence.lengthUnits,
        convictionResponse[0].sentence.description,
        convictionResponse[0].sentence.licenceExpiryDate,
        convictionResponse[0].sentence.sentenceExpiryDate,
        convictionResponse[0].sentence.secondLength,
        convictionResponse[0].sentence.secondLengthUnits,
        custodialTerm,
        extendedTerm,
        hasBeenReviewed,
      )
    }
    return null
  }

  private fun extendedSentenceDetails(
    conviction: ConvictionDetails,
    isExtendedSentenceInRecommendation: Boolean?,
  ): Pair<String?, String?> {
    return if ("Extended Determinate Sentence" == conviction.sentence.description ||
      "CJA - Extended Sentence" == conviction.sentence.description ||
      isExtendedSentenceInRecommendation == true
    ) {
      val custodialTerm =
        conviction.sentence.length?.toString() + MrdTextConstants.WHITE_SPACE + conviction.sentence.lengthUnits
      val sentenceSecondLength = conviction.sentence.secondLength?.toString() ?: MrdTextConstants.EMPTY_STRING
      val sentenceSecondLengthUnits = conviction.sentence.secondLengthUnits ?: MrdTextConstants.EMPTY_STRING

      Pair(custodialTerm, sentenceSecondLength + MrdTextConstants.WHITE_SPACE + sentenceSecondLengthUnits)
    } else {
      Pair(null, null)
    }
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
    jsonRequest: JsonNode?,
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
      val recommendations = recommendationRepository.findByCrnAndStatus(
        crn,
        listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name, Status.DOCUMENT_DOWNLOADED.name),
      )

      return RecommendationsResponse(
        personalDetailsOverview = personalDetailsOverview,
        activeRecommendation = recommendationDetails,
        recommendations = buildRecommendationsResponse(recommendations),
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
          statuses = recommendationStatusRepository.findByRecommendationId(it.id),
          recallType = it.data.recallType,
        )
      }
  }

  private fun isStatusOpen(it: RecommendationEntity) =
    recommendationStatusRepository.findByRecommendationId(it.id)
      .any { it.active && (it.name == "BOOK_TO_PPUD" || it.name == "DNTR_DOWNLOADED") }
      .not()
}

data class RecommendationMetaData(
  var countersignAcoName: String? = null,
  var countersignAcoDateTime: LocalDateTime? = null,
  var acoCounterSignEmail: String? = null,
  var countersignSpoName: String? = null,
  var countersignSpoDateTime: LocalDateTime? = null,
  var spoCounterSignEmail: String? = null,
  var userNamePartACompletedBy: String? = null,
  var userEmailPartACompletedBy: String? = null,
  var userPartACompletedByDateTime: LocalDateTime? = null,
)

private fun RecommendationMetaData.fromFetchRecommendationsStatusResponse(
  fetchRecommendationStatusesResponse: List<RecommendationStatusResponse>,
): RecommendationMetaData {
  fetchRecommendationStatusesResponse.forEach {
    if (it.name.equals("ACO_SIGNED")) {
      this.countersignAcoName = it.createdByUserFullName
      this.countersignAcoDateTime = dateTimeWithDaylightSavingFromString(utcDateTimeString = it.created)
      this.acoCounterSignEmail = it.emailAddress
    }
    if (it.name.equals("SPO_SIGNED")) {
      this.countersignSpoName = it.createdByUserFullName
      this.countersignSpoDateTime = dateTimeWithDaylightSavingFromString(utcDateTimeString = it.created)
      this.spoCounterSignEmail = it.emailAddress
    }
    if (it.name.equals("PO_RECALL_CONSULT_SPO")) {
      this.userNamePartACompletedBy = it.createdByUserFullName
      this.userPartACompletedByDateTime = LocalDateTime.now(ZoneId.of("Europe/London"))
      this.userEmailPartACompletedBy = it.emailAddress
    }
  }
  return this
}
