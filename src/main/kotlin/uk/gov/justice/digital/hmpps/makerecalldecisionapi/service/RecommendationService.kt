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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentRequestType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousRecalls
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PreviousReleases
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
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
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createRecommendation(
    recommendationRequest: CreateRecommendationRequest,
    username: String?
  ): RecommendationResponse {
    val userAccessResponse = recommendationRequest.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val personDetails = recommendationRequest.crn?.let { personDetailsService.getPersonDetails(it) }
      val indexOffenceDetails = recommendationRequest.crn?.let { riskService?.fetchAssessmentInfo(crn = it, hideOffenceDetailsWhenNoMatch = true) }
      val convictionResponse = (recommendationRequest.crn?.let { convictionService.buildConvictionResponse(it, false) })
      val convictionForRecommendation =
        buildRecommendationConvictionResponse(convictionResponse?.filter { it.isCustodial == true })

      val savedRecommendation = saveNewRecommendationEntity(
        recommendationRequest,
        username,
        StaticRecommendationDataWrapper(
          PersonOnProbation(
            croNumber = personDetails?.personalDetailsOverview?.croNumber,
            mostRecentPrisonerNumber = personDetails?.personalDetailsOverview?.mostRecentPrisonerNumber,
            nomsNumber = personDetails?.personalDetailsOverview?.nomsNumber,
            pncNumber = personDetails?.personalDetailsOverview?.pncNumber,
            name = personDetails?.personalDetailsOverview?.name,
            firstName = personDetails?.personalDetailsOverview?.firstName,
            middleNames = personDetails?.personalDetailsOverview?.middleNames,
            surname = personDetails?.personalDetailsOverview?.surname,
            gender = personDetails?.personalDetailsOverview?.gender,
            ethnicity = personDetails?.personalDetailsOverview?.ethnicity,
            primaryLanguage = personDetails?.personalDetailsOverview?.primaryLanguage,
            dateOfBirth = personDetails?.personalDetailsOverview?.dateOfBirth,
            addresses = personDetails?.addresses,
          ),
          convictionForRecommendation,
          personDetails?.offenderManager?.probationAreaDescription,
          personDetails?.offenderManager?.probationTeam?.localDeliveryUnitDescription
        )
      )

      return RecommendationResponse(
        id = savedRecommendation?.id,
        status = savedRecommendation?.data?.status,
        personOnProbation = savedRecommendation?.data?.personOnProbation,
        indexOffenceDetails = indexOffenceDetails?.offenceDescription
      )
    }
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
      personOnProbation = recommendationEntity.data.personOnProbation,
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
      previousRecalls = recommendationEntity.data.previousRecalls
    )
  }

  @OptIn(ExperimentalStdlibApi::class)
  suspend fun updateRecommendation(
    jsonRequest: JsonNode?,
    recommendationId: Long,
    username: String?,
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
        existingRecommendationEntity.data.userNamePartACompletedBy = username
        existingRecommendationEntity.data.userEmailPartACompletedBy = userEmail
        existingRecommendationEntity.data.lastPartADownloadDateTime = localNowDateTime()
      } else if (isDntrDownloaded) {
        existingRecommendationEntity.data.userNameDntrLetterCompletedBy = username
        existingRecommendationEntity.data.lastDntrLetterADownloadDateTime = localNowDateTime()
      } else {
        val readerForUpdating: ObjectReader = CustomMapper.readerForUpdating(existingRecommendationEntity.data)
        val updateRecommendationRequest: RecommendationModel = readerForUpdating.readValue(jsonRequest)
        existingRecommendationEntity.data = updatePageReviewedValues(updateRecommendationRequest, existingRecommendationEntity).data
      }
      existingRecommendationEntity.data.previousReleases = getPreviousReleaseDetails(pageRefreshIds, existingRecommendationEntity.data.crn, existingRecommendationEntity.data.previousReleases)
      existingRecommendationEntity.data.previousRecalls = getPreviousRecallDetails(pageRefreshIds, existingRecommendationEntity.data.crn, existingRecommendationEntity.data.previousRecalls)

      existingRecommendationEntity.data.lastModifiedDate = utcNowDateTimeString()
      existingRecommendationEntity.data.lastModifiedBy = username

      val savedRecommendation = recommendationRepository.save(existingRecommendationEntity)
      log.info("recommendation for ${savedRecommendation.data.crn} updated")
      return buildRecommendationResponse(savedRecommendation)
    }
  }

  private fun getPreviousReleaseDetails(pageRefreshIds: List<String>?, crn: String?, previousReleases: PreviousReleases?): PreviousReleases? {
    if (pageRefreshIds?.filter { it == "previousReleases" }?.isNotEmpty() == true && crn != null) {

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
    if (pageRefreshIds?.filter { it == "previousRecalls" }?.isNotEmpty() == true && crn != null) {

      val releaseSummaryResponse = getValueAndHandleWrappedException(communityApiClient.getReleaseSummary(crn))

      return PreviousRecalls(
        lastRecallDate = releaseSummaryResponse?.lastRecall?.date,
        hasBeenRecalledPreviously = previousRecalls?.hasBeenRecalledPreviously,
        previousRecallDates = previousRecalls?.previousRecallDates,
      )
    }
    return previousRecalls
  }

  private fun updatePageReviewedValues(
    updateRecommendationRequest: RecommendationModel,
    recommendationEntity: RecommendationEntity
  ): RecommendationEntity {
    val data = recommendationEntity.data
    var personOnProbation = data.personOnProbation
    var convictionDetail = data.convictionDetail
    var mappa = data.personOnProbation?.mappa
    if (updateRecommendationRequest.hasBeenReviewed?.mappa == true) {
      mappa = mappa?.copy(hasBeenReviewed = true) ?: Mappa(hasBeenReviewed = true)
    }
    if (updateRecommendationRequest.hasBeenReviewed?.personOnProbation == true) {
      personOnProbation = personOnProbation?.copy(
        hasBeenReviewed = true
      )
    }
    if (updateRecommendationRequest.hasBeenReviewed?.convictionDetail == true) {
      convictionDetail = convictionDetail?.copy(
        hasBeenReviewed = true
      )
    }

    return recommendationEntity.copy(
      data = data.copy(
        personOnProbation = personOnProbation?.copy(mappa = mappa),
        convictionDetail = convictionDetail,
        hasBeenReviewed = null
      )
    )
  }

  fun getDraftRecommendationForCrn(crn: String): ActiveRecommendation? {
    val recommendationEntity = recommendationRepository.findByCrnAndStatus(crn, Status.DRAFT.name)
    Collections.sort(recommendationEntity)

    if (recommendationEntity.size > 1) {
      log.error("More than one recommendation found for CRN. Returning the latest.")
    }
    return if (recommendationEntity.isNotEmpty()) {
      ActiveRecommendation(
        recommendationId = recommendationEntity[0].id,
        lastModifiedDate = recommendationEntity[0].data.lastModifiedDate,
        lastModifiedBy = recommendationEntity[0].data.lastModifiedBy,
        recallType = recommendationEntity[0].data.recallType,
      )
    } else {
      null
    }
  }

  suspend fun generateDntr(
    recommendationId: Long,
    username: String?,
    documentRequestType: DocumentRequestType?
  ): DocumentResponse {
    return if (documentRequestType == DocumentRequestType.DOWNLOAD_DOC_X) {
      generateDntrDownload(recommendationId, username)
    } else {
      generateDntrPreview(recommendationId)
    }
  }

  private suspend fun generateDntrDownload(recommendationId: Long, username: String?): DocumentResponse {
    val recommendationResponse = updateRecommendation(null, recommendationId, username, null, false, true, null)
    val userAccessResponse = recommendationResponse.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val fileContents =
        templateReplacementService.generateDocFromRecommendation(recommendationResponse, DocumentType.DNTR_DOCUMENT, null)
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
  suspend fun generatePartA(recommendationId: Long, username: String?, userEmail: String?, featureFlags: FeatureFlags?): DocumentResponse {
    val recommendationModel = updateRecommendation(null, recommendationId, username, userEmail, true, false, null)
    val userAccessResponse = recommendationModel.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val fileContents =
        templateReplacementService.generateDocFromRecommendation(recommendationModel, DocumentType.PART_A_DOCUMENT, featureFlags)
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
    recommendationWrapper: StaticRecommendationDataWrapper?
  ): RecommendationEntity? {
    val now = utcNowDateTimeString()
    val recommendationEntity = RecommendationEntity(
      data = RecommendationModel(
        crn = recommendationRequest.crn,
        status = Status.DRAFT,
        lastModifiedBy = createdByUserName,
        lastModifiedDate = now,
        createdBy = createdByUserName,
        createdDate = now,
        personOnProbation = recommendationWrapper?.personOnProbation,
        convictionDetail = recommendationWrapper?.convictionDetail,
        region = recommendationWrapper?.region,
        localDeliveryUnit = recommendationWrapper?.localDeliveryUnit,
        indexOffenceDetails = recommendationWrapper?.indexOffenceDetails
      )
    )

    return recommendationRepository.save(recommendationEntity)
  }

  private fun buildRecommendationConvictionResponse(convictionResponse: List<ConvictionResponse>?): ConvictionDetail? {
    if (convictionResponse?.size == 1) {

      val mainOffence = convictionResponse[0].offences?.filter { it.mainOffence == true }?.get(0)
      val (custodialTerm, extendedTerm) = extendedSentenceDetails(convictionResponse[0])

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
        extendedTerm
      )
    }
    return null
  }

  private fun extendedSentenceDetails(conviction: ConvictionResponse?): Pair<String?, String?> {
    return if ("Extended Determinate Sentence" == conviction?.sentenceDescription ||
      "CJA - Extended Sentence" == conviction?.sentenceDescription
    ) {
      val custodialTerm = conviction.sentenceOriginalLength?.toString() + MrdTextConstants.WHITE_SPACE + conviction.sentenceOriginalLengthUnits
      val sentenceSecondLength = conviction.sentenceSecondLength?.toString() ?: MrdTextConstants.EMPTY_STRING
      val sentenceSecondLengthUnits = conviction.sentenceSecondLengthUnits ?: MrdTextConstants.EMPTY_STRING

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
