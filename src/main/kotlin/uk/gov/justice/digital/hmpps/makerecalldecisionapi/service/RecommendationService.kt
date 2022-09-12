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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader.CustomMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.nowDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.nowDateTime
import java.util.Collections
import kotlin.jvm.optionals.getOrNull

@Service
internal class RecommendationService(
  val recommendationRepository: RecommendationRepository,
  @Lazy val personDetailsService: PersonDetailsService,
  val partATemplateReplacementService: PartATemplateReplacementService,
  private val userAccessValidator: UserAccessValidator,
  private val convictionService: ConvictionService,
  @Lazy private val riskService: RiskService?
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun createRecommendation(recommendationRequest: CreateRecommendationRequest, username: String?): RecommendationResponse {
    val userAccessResponse = recommendationRequest.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val personDetails = recommendationRequest.crn?.let { personDetailsService.getPersonDetails(it) }
      val riskResponse = recommendationRequest.crn?.let { riskService?.getRisk(it) }

      val convictionResponse = (recommendationRequest.crn?.let { convictionService.buildConvictionResponse(it, false) })
      val convictionForRecommendation = buildRecommendationConvictionResponse(convictionResponse?.filter { it.isCustodial == true })

      val savedRecommendation = saveNewRecommendationEntity(
        recommendationRequest,
        username,
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
          mappa = riskResponse?.mappa,
          addresses = personDetails?.addresses
        ),
        convictionForRecommendation
      )

      return RecommendationResponse(
        id = savedRecommendation?.id,
        status = savedRecommendation?.data?.status,
        personOnProbation = savedRecommendation?.data?.personOnProbation
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
        isExtendedOrIndeterminateSentence = recommendationEntity.data.isExtendedOrIndeterminateSentence,
        isDeterminateSentence = recommendationEntity.data.isDeterminateSentence,
        activeCustodialConvictionCount = recommendationEntity.data.activeCustodialConvictionCount,
        hasVictimsInContactScheme = recommendationEntity.data.hasVictimsInContactScheme,
        dateVloInformed = recommendationEntity.data.dateVloInformed,
        hasArrestIssues = recommendationEntity.data.hasArrestIssues,
        hasContrabandRisk = recommendationEntity.data.hasContrabandRisk,
        personOnProbation = recommendationEntity.data.personOnProbation,
        alternativesToRecallTried = recommendationEntity.data.alternativesToRecallTried,
        licenceConditionsBreached = recommendationEntity.data.licenceConditionsBreached,
        underIntegratedOffenderManagement = recommendationEntity.data.underIntegratedOffenderManagement,
        localPoliceContact = recommendationEntity.data.localPoliceContact,
        vulnerabilities = recommendationEntity.data.vulnerabilities,
        convictionDetail = recommendationEntity.data.convictionDetail
      )
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Transactional
  fun updateRecommendation(jsonRequest: JsonNode, recommendationId: Long, username: String?) {
    val recommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
    val userAccessResponse = recommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val existingRecommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
        ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")

      val readerForUpdating: ObjectReader = CustomMapper.readerForUpdating(existingRecommendationEntity.data)

      val updateRecommendationRequest: RecommendationModel = readerForUpdating.readValue(jsonRequest)

      existingRecommendationEntity.data = updateRecommendationRequest
      existingRecommendationEntity.data.lastModifiedDate = nowDateTime()
      existingRecommendationEntity.data.lastModifiedBy = username

      val savedRecommendation = recommendationRepository.save(existingRecommendationEntity)
      log.info("recommendation for ${savedRecommendation.data.crn} updated")
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
      )
    } else {
      null
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  fun generatePartA(recommendationId: Long): PartAResponse {
    val recommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
    val userAccessResponse = recommendationEntity.data.crn?.let { userAccessValidator.checkUserAccess(it) }
    if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      throw UserAccessException(Gson().toJson(userAccessResponse))
    } else {
      val fileContents = partATemplateReplacementService.generateDocFromTemplate(recommendationEntity)
      return PartAResponse(
        fileName = generatePartAFileName(recommendationEntity.data),
        fileContents = fileContents
      )
    }
  }

  private fun generatePartAFileName(recommendation: RecommendationModel): String {

    val surname = recommendation.personOnProbation?.surname ?: ""
    val firstName = if (recommendation.personOnProbation?.firstName != null && recommendation.personOnProbation.firstName.isNotEmpty()) {
      recommendation.personOnProbation.firstName.subSequence(0, 1)
    } else ""
    val crn = recommendation.crn ?: ""

    return "NAT_Recall_Part_A_${nowDate()}_${surname}_${firstName}_$crn.docx"
  }

  private fun saveNewRecommendationEntity(
    recommendationRequest: CreateRecommendationRequest,
    createdByUserName: String?,
    personOnProbation: PersonOnProbation?,
    convictionForRecommendation: ConvictionDetail?
  ): RecommendationEntity? {

    val now = nowDateTime()

    return recommendationRepository.save(
      RecommendationEntity(
        data = RecommendationModel(
          crn = recommendationRequest.crn,
          status = Status.DRAFT,
          lastModifiedBy = createdByUserName,
          lastModifiedDate = now,
          createdBy = createdByUserName,
          createdDate = now,
          personOnProbation = personOnProbation,
          convictionDetail = convictionForRecommendation
        )
      )
    )
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
        convictionResponse[0].sentenceOriginalLengthUnits,
      )
    }
    return null
  }
}
