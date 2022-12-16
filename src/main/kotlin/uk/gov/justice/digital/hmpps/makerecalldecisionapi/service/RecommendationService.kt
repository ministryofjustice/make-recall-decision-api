package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectReader
import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AdditionalInformation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentRequestType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MessageAttributes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEventMessageBody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonReference
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.TypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallConsidered
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.toPersonOnProbationDto
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toPersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader.CustomMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.localNowDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.nowDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.utcNowDateTimeString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.lang.System.getenv
import java.time.LocalDateTime
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
  private val mrdEventsEmitter: MrdEventsEmitter?
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createRecommendation(
    recommendationRequest: CreateRecommendationRequest,
    username: String?,
    readableUsername: String?,
    featureFlags: FeatureFlags?
  ): RecommendationResponse {
    val userAccessResponse = recommendationRequest.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val personDetails = recommendationRequest.crn?.let { personDetailsService.getPersonDetails(it) }
      val status = if (featureFlags?.flagDomainEventConsiderRecall == true) Status.RECALL_CONSIDERED else Status.DRAFT
      val recallConsideredList = if (featureFlags?.flagDomainEventConsiderRecall == true) listOf(
        RecallConsidered(
          userId = username,
          createdDate = utcNowDateTimeString(),
          userName = readableUsername,
          recallConsideredDetail = recommendationRequest.recallConsideredDetail
        )
      ) else null

      val savedRecommendation = saveNewRecommendationEntity(
        recommendationRequest,
        username,
        status,
        recallConsideredList,
        StaticRecommendationDataWrapper(
          personDetails?.toPersonOnProbation(),
          personDetails?.offenderManager?.probationAreaDescription,
          personDetails?.offenderManager?.probationTeam?.localDeliveryUnitDescription
        )
      )

      val recommendationId = savedRecommendation?.id
      if (getenv("spring_profiles_active") != "dev" && featureFlags?.flagDomainEventRecommendationStarted == true) {
        recommendationId?.let { sendRecommendationStartedEvent(it) }
      }

      return RecommendationResponse(
        id = savedRecommendation?.id,
        status = savedRecommendation?.data?.status,
        personOnProbation = savedRecommendation?.data?.personOnProbation?.toPersonOnProbationDto()
      )
    }
  }

  fun buildRecallDecisionList(username: String?, readableUsername: String?, recallConsideredDetail: String?): List<RecallConsidered> {
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
    val recommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")

    return buildRecommendationResponse(recommendationEntity)
  }

  private fun buildRecommendationResponse(recommendationEntity: RecommendationEntity): RecommendationResponse {
    return RecommendationResponse(
      id = recommendationEntity.id,
      crn = recommendationEntity.data.crn,
      recallType = recommendationEntity.data.recallType,
      status = recommendationEntity.data.status,
      custodyStatus = recommendationEntity.data.custodyStatus,
      responseToProbation = recommendationEntity.data.responseToProbation,
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
      recallConsideredList = recommendationEntity.data.recallConsideredList
    )
  }

  @OptIn(ExperimentalStdlibApi::class)
  suspend fun updateRecommendation(
    jsonRequest: JsonNode?,
    recommendationId: Long,
    userId: String?,
    readableUserName: String?,
    userEmail: String?,
    isPartADownloaded: Boolean,
    isDntrDownloaded: Boolean = false,
    pageRefreshIds: List<String>?
  ): RecommendationResponse {
    validateRecallType(jsonRequest)
    val existingRecommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
    val userAccessResponse = existingRecommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      if (isPartADownloaded) {
        existingRecommendationEntity.data.userNamePartACompletedBy = readableUserName
        existingRecommendationEntity.data.userEmailPartACompletedBy = userEmail
        existingRecommendationEntity.data.lastPartADownloadDateTime = localNowDateTime()
        existingRecommendationEntity.data.status = Status.DOCUMENT_DOWNLOADED
      } else if (isDntrDownloaded) {
        existingRecommendationEntity.data.userNameDntrLetterCompletedBy = readableUserName
        existingRecommendationEntity.data.lastDntrLetterADownloadDateTime = localNowDateTime()
        existingRecommendationEntity.data.status = Status.DOCUMENT_DOWNLOADED
      } else {
        val readerForUpdating: ObjectReader = CustomMapper.readerForUpdating(existingRecommendationEntity.data)
        val updateRecommendationRequest: RecommendationModel = readerForUpdating.readValue(jsonRequest)
        existingRecommendationEntity.data.recallConsideredList = updateRecallConsideredList(updateRecommendationRequest, existingRecommendationEntity.data, userId, readableUserName)
        existingRecommendationEntity.data = updatePageReviewedValues(updateRecommendationRequest, existingRecommendationEntity).data
      }
      refreshData(pageRefreshIds, existingRecommendationEntity.data)

      existingRecommendationEntity.data.lastModifiedDate = utcNowDateTimeString()
      existingRecommendationEntity.data.lastModifiedBy = userId
      existingRecommendationEntity.data.lastModifiedByUserName = readableUserName

      val savedRecommendation = recommendationRepository.save(existingRecommendationEntity)
      log.info("recommendation for ${savedRecommendation.data.crn} updated")
      return buildRecommendationResponse(savedRecommendation)
    }
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

    // FIXME: This will need to be fixed when we open up functionally to allow multiple recommendations per CRN
    if (recommendationEntity.size > 1) {
      log.error("More than one recommendation found for CRN. Returning the latest.")
    }
    return if (recommendationEntity.isNotEmpty()) {
      ActiveRecommendation(
        recommendationId = recommendationEntity[0].id,
        lastModifiedDate = recommendationEntity[0].data.lastModifiedDate,
        lastModifiedBy = recommendationEntity[0].data.lastModifiedBy,
        recallType = recommendationEntity[0].data.recallType,
        recallConsideredList = recommendationEntity[0].data.recallConsideredList,
        status = recommendationEntity[0].data.status,
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
      val documentResponse = generateDntrDownload(recommendationId, userId, readableUsername)

      if (getenv("spring_profiles_active") != "dev" && featureFlags?.flagSendDomainEvent == true) {
        sendDntrDownloadEvent(recommendationId)
      }
      documentResponse
    } else {
      generateDntrPreview(recommendationId)
    }
  }

  private fun sendRecommendationStartedEvent(recommendationId: Long) {
    val crn = recommendationRepository.findById(recommendationId).map { it.data.crn }.get()
    val payload = MrdEvent(
      message = MrdEventMessageBody(
        eventType = "prison-recall.recommendation.started",
        version = 1,
        description = "Recommendation started (recall or no recall)",
        occurredAt = LocalDateTime.now(),
        detailUrl = "", // TODO TBD
        personReference = PersonReference(listOf(TypeValue(type = "CRN", value = crn))),
        additionalInformation = AdditionalInformation(recommendationUrl = "https://make-recall-decision-api.hmpps.service.justice.gov.uk/cases/$crn/overview")
      ),
      messageAttributes = MessageAttributes(eventType = TypeValue(type = "String", value = "prison-recall.recommendation.started"))
    )
    mrdEventsEmitter?.sendEvent(payload)
  }

  private fun sendDntrDownloadEvent(recommendationId: Long) {
    val crn = recommendationRepository.findById(recommendationId).map { it.data.crn }.get()
    val payload = MrdEvent(
      message = MrdEventMessageBody(
        eventType = "DNTR_LETTER_DOWNLOADED",
        version = 1,
        description = "DNTR letter downloaded",
        occurredAt = LocalDateTime.now(),
        detailUrl = "", // TODO TBD
        personReference = PersonReference(listOf(TypeValue(type = "CRN", value = crn)))
      )
    )
    mrdEventsEmitter?.sendEvent(payload)
  }

  private suspend fun generateDntrDownload(recommendationId: Long, userId: String?, readableUsername: String?,): DocumentResponse {
    val recommendationResponse = updateRecommendation(null, recommendationId, userId, readableUsername, null, false, true, null)
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
  suspend fun generatePartA(recommendationId: Long, username: String?, readableUsername: String?, userEmail: String?): DocumentResponse {
    val recommendationModel = updateRecommendation(null, recommendationId, username, readableUsername, userEmail, true, false, null)
    val userAccessResponse = recommendationModel.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val fileContents =
        templateReplacementService.generateDocFromRecommendation(recommendationModel, DocumentType.PART_A_DOCUMENT)
      return DocumentResponse(
        fileName = generateDocumentFileName(recommendationModel, "NAT_Recall_Part_A"),
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
    createdByUserName: String?,
    status: Status?,
    recallConsideredList: List<RecallConsidered>?,
    recommendationWrapper: StaticRecommendationDataWrapper?
  ): RecommendationEntity? {
    val now = utcNowDateTimeString()
    val recommendationEntity = RecommendationEntity(
      data = RecommendationModel(
        crn = recommendationRequest.crn,
        recallConsideredList = recallConsideredList,
        status = status,
        lastModifiedBy = createdByUserName,
        lastModifiedDate = now,
        createdBy = createdByUserName,
        createdDate = now,
        personOnProbation = recommendationWrapper?.personOnProbation,
        region = recommendationWrapper?.region,
        localDeliveryUnit = recommendationWrapper?.localDeliveryUnit
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
    if (selectedRecallType != null) {
      val allOptions = jsonRequest.get("recallType")?.get("allOptions")
      val allOptionsList = allOptions?.map { it.get("value").asText() }?.toList()
      val valid = allOptionsList?.any { it == selectedRecallType }
      val errorMessage = "$selectedRecallType is not a valid recall type, available types are ${allOptionsList?.joinToString(",")}"
      if (valid == false) throw InvalidRequestException(errorMessage)
    }
  }
}
