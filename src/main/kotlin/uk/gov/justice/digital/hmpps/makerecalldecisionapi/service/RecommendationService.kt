package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectReader
import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ConvictionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentRequestType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
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
  @Lazy private val riskService: RiskService?
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createRecommendation(
    recommendationRequest: CreateRecommendationRequest,
    username: String?
  ): RecommendationResponse {
    val userAccessResponse = recommendationRequest.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
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
            dateOfBirth = personDetails?.personalDetailsOverview?.dateOfBirth,
            addresses = personDetails?.addresses
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

  @OptIn(ExperimentalStdlibApi::class)
  fun getRecommendation(recommendationId: Long): RecommendationResponse {
    val recommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
    val userAccessResponse = recommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      RecommendationResponse(userAccessResponse)
    } else {
      RecommendationResponse(
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
        fixedTermAdditionalLicenceConditions = recommendationEntity.data.fixedTermAdditionalLicenceConditions,
        indeterminateOrExtendedSentenceDetails = recommendationEntity.data.indeterminateOrExtendedSentenceDetails,
        mainAddressWherePersonCanBeFound = recommendationEntity.data.mainAddressWherePersonCanBeFound,
        whyConsideredRecall = recommendationEntity.data.whyConsideredRecall,
        reasonsForNoRecall = recommendationEntity.data.reasonsForNoRecall,
        nextAppointment = recommendationEntity.data.nextAppointment
      )
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  suspend fun updateRecommendation(
    jsonRequest: JsonNode?,
    recommendationId: Long,
    username: String?,
    userEmail: String?,
    isPartADownloaded: Boolean,
    isDntrDownloaded: Boolean = false
  ): RecommendationEntity {
    val recommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
    val userAccessResponse = recommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val existingRecommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
        ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")

      if (isPartADownloaded) {
        existingRecommendationEntity.data.userNamePartACompletedBy = username
        existingRecommendationEntity.data.userEmailPartACompletedBy = userEmail
        existingRecommendationEntity.data.lastPartADownloadDateTime = localNowDateTime()
        existingRecommendationEntity.data = patchRecommendationWithExtraData(existingRecommendationEntity).data
      } else if (isDntrDownloaded) {
        existingRecommendationEntity.data.userNameDntrLetterCompletedBy = username
        existingRecommendationEntity.data.lastDntrLetterADownloadDateTime = localNowDateTime()
        existingRecommendationEntity.data = patchRecommendationWithExtraData(existingRecommendationEntity).data
      } else {
        val readerForUpdating: ObjectReader = CustomMapper.readerForUpdating(existingRecommendationEntity.data)
        val updateRecommendationRequest: RecommendationModel = readerForUpdating.readValue(jsonRequest)

        existingRecommendationEntity.data = updateRecommendationRequest
      }
      existingRecommendationEntity.data.lastModifiedDate = utcNowDateTimeString()
      existingRecommendationEntity.data.lastModifiedBy = username

      val savedRecommendation = recommendationRepository.save(existingRecommendationEntity)
      log.info("recommendation for ${savedRecommendation.data.crn} updated")
      return savedRecommendation
    }
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

  @OptIn(ExperimentalStdlibApi::class)
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
    val recommendationEntity = updateRecommendation(null, recommendationId, username, null, false, true)
    val userAccessResponse = recommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val fileContents =
        templateReplacementService.generateDocFromRecommendation(recommendationEntity, DocumentType.DNTR_DOCUMENT)
      DocumentResponse(
        fileName = generateDocumentFileName(recommendationEntity.data, "No_Recall"),
        fileContents = fileContents
      )
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  suspend fun generateDntrPreview(recommendationId: Long): DocumentResponse {
    val recommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
    val enrichedRecommendationEntity = patchRecommendationWithExtraData(recommendationEntity)
    val userAccessResponse = enrichedRecommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val letterContent =
        templateReplacementService.generateLetterContentForPreviewFromRecommendation(enrichedRecommendationEntity)
      DocumentResponse(
        letterContent = letterContent
      )
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  suspend fun generatePartA(recommendationId: Long, username: String?, userEmail: String?): DocumentResponse {
    val recommendationEntity = updateRecommendation(null, recommendationId, username, userEmail, true)
    val userAccessResponse = recommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val fileContents =
        templateReplacementService.generateDocFromRecommendation(recommendationEntity, DocumentType.PART_A_DOCUMENT)
      return DocumentResponse(
        fileName = generateDocumentFileName(recommendationEntity.data, "NAT_Recall_Part_A"),
        fileContents = fileContents
      )
    }
  }

  suspend fun patchRecommendationWithExtraData(recommendationEntity: RecommendationEntity): RecommendationEntity {
    val crn = recommendationEntity.data.crn
    val riskResponse = crn?.let { riskService?.getRisk(it) }
    val personDetails = crn?.let { personDetailsService.getPersonDetails(it) }
    val indexOffenceDetails = crn?.let { riskService?.fetchAssessmentInfo(crn = it, hideOffenceDetailsWhenNoMatch = true) }
    val data = recommendationEntity.data
    val personOnProbation = data.personOnProbation
    return recommendationEntity.copy(
      data = data.copy(
        indexOffenceDetails = indexOffenceDetails?.offenceDescription,
        personOnProbation = personOnProbation?.copy(
          mappa = riskResponse?.mappa,
          addresses = personDetails?.addresses,
          mostRecentPrisonerNumber = personDetails?.personalDetailsOverview?.mostRecentPrisonerNumber
        )
      )
    )
  }

  private fun generateDocumentFileName(recommendation: RecommendationModel, prefix: String): String {
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
      )
    }
    return null
  }
}
