package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat.forPattern
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import java.util.Collections
import kotlin.jvm.optionals.getOrNull

@Service
internal class RecommendationService(
  val recommendationRepository: RecommendationRepository,
  @Lazy val personDetailsService: PersonDetailsService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  fun createRecommendation(recommendationRequest: CreateRecommendationRequest, username: String?): RecommendationResponse {
    val name = recommendationRequest.crn?.let { personDetailsService.getPersonDetails(it) }?.personalDetailsOverview?.name
    val savedRecommendation = saveNewRecommendationEntity(recommendationRequest, username, PersonOnProbation(name = name))

    return RecommendationResponse(
      id = savedRecommendation?.id,
      status = savedRecommendation?.data?.status,
      personOnProbation = savedRecommendation?.data?.personOnProbation
    )
  }

  @OptIn(ExperimentalStdlibApi::class)
  fun getRecommendation(recommendationId: Long): RecommendationResponse {
    val recommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
    return RecommendationResponse(
      id = recommendationEntity.id,
      crn = recommendationEntity.data.crn,
      recallType = RecallType(
        value = recommendationEntity.data.recallType?.value,
        options = recommendationEntity.data.recallType?.options
      ),
      status = recommendationEntity.data.status,
      custodyStatus = CustodyStatus(
        value = recommendationEntity.data.custodyStatus?.value,
        options = recommendationEntity.data.custodyStatus?.options
      ),
      personOnProbation = recommendationEntity.data.personOnProbation
    )
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Transactional
  fun updateRecommendation(updateRecommendationRequest: UpdateRecommendationRequest, recommendationId: Long, username: String?): RecommendationResponse {
    val existingRecommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")

    val updatedRecommendationEntity = updateRecommendationInDb(existingRecommendationEntity, updateRecommendationRequest, username)

    return RecommendationResponse(
      id = updatedRecommendationEntity!!.id!!,
      crn = updatedRecommendationEntity.data.crn,
      status = updatedRecommendationEntity.data.status,
      recallType = updateRecommendationRequest.recallType,
      custodyStatus = updateRecommendationRequest.custodyStatus,
      personOnProbation = updatedRecommendationEntity.data.personOnProbation
    )
  }

  private fun updateRecommendationInDb(
    existingRecommendationEntity: RecommendationEntity,
    updateRecommendationRequest: UpdateRecommendationRequest,
    updatedByUserName: String?
  ): RecommendationEntity? {

    val status = updateRecommendationRequest.status ?: existingRecommendationEntity.data.status

    existingRecommendationEntity.data.recallType = updateRecallType(existingRecommendationEntity, updateRecommendationRequest)
    existingRecommendationEntity.data.custodyStatus = updateCustodyStatus(existingRecommendationEntity, updateRecommendationRequest)
    existingRecommendationEntity.data.status = status
    existingRecommendationEntity.data.lastModifiedDate = nowDate()
    existingRecommendationEntity.data.lastModifiedBy = updatedByUserName

    return recommendationRepository.save(existingRecommendationEntity)
  }

  private fun updateRecallType(existingRecommendationEntity: RecommendationEntity, updateRecommendationRequest: UpdateRecommendationRequest): RecallType? {
    val updatedRecallType = updateRecommendationRequest.recallType
    val originalRecallType = existingRecommendationEntity.data.recallType
    return updatedRecallType ?: originalRecallType
  }

  private fun updateCustodyStatus(existingRecommendationEntity: RecommendationEntity, updateRecommendationRequest: UpdateRecommendationRequest): CustodyStatus? {
    val updatedCustodyStatus = updateRecommendationRequest.custodyStatus
    val originalCustodyStatus = existingRecommendationEntity.data.custodyStatus
    return updatedCustodyStatus ?: originalCustodyStatus
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

  private fun nowDate(): String {
    val formatter = forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    return formatter.print(DateTime(DateTimeZone.UTC)).toString()
  }

  private fun saveNewRecommendationEntity(
    recommendationRequest: CreateRecommendationRequest,
    createdByUserName: String?,
    personOnProbation: PersonOnProbation?
  ): RecommendationEntity? {

    val now = nowDate()

    return recommendationRepository.save(
      RecommendationEntity(
        data = RecommendationModel(
          crn = recommendationRequest.crn,
          status = Status.DRAFT,
          lastModifiedBy = createdByUserName,
          lastModifiedDate = now,
          createdBy = createdByUserName,
          createdDate = now,
          personOnProbation = personOnProbation
        )
      )
    )
  }
}
