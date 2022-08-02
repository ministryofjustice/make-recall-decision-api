package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat.forPattern
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import java.util.Collections
import kotlin.jvm.optionals.getOrNull

@Service
internal class RecommendationService(
  val recommendationRepository: RecommendationRepository
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun createRecommendation(recommendationRequest: CreateRecommendationRequest, username: String?): RecommendationResponse {
    val savedRecommendation = saveNewRecommendationEntity(recommendationRequest, username)

    return RecommendationResponse(
      id = savedRecommendation?.id,
      status = savedRecommendation?.data?.status,
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
      status = recommendationEntity.data.status
    )
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Transactional
  fun updateRecommendation(updateRecommendationRequest: UpdateRecommendationRequest, recommendationId: Long, username: String?): RecommendationResponse {
    val existingRecommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")

    val updatedRecommendationEntity = updateRecommendation(existingRecommendationEntity, updateRecommendationRequest, username)

    return RecommendationResponse(
      id = updatedRecommendationEntity!!.id!!,
      crn = updatedRecommendationEntity.data.crn,
      status = updatedRecommendationEntity.data.status,
      recallType = updateRecommendationRequest.recallType
        ?: RecallType(
          value = updatedRecommendationEntity.data.recallType?.value ?: existingRecommendationEntity.data.recallType?.value,
          options = updatedRecommendationEntity.data.recallType?.options ?: existingRecommendationEntity.data.recallType?.options,
        )
    )
  }

  private fun updateRecommendation(
    existingRecommendationEntity: RecommendationEntity,
    updateRecommendationRequest: UpdateRecommendationRequest,
    updatedByUserName: String?
  ): RecommendationEntity? {
    val updatedRecallType = updateRecommendationRequest.recallType
    val originalRecallType = existingRecommendationEntity.data.recallType
    val recallType = updatedRecallType ?: originalRecallType
    val status = updateRecommendationRequest.status ?: existingRecommendationEntity.data.status

    existingRecommendationEntity.data.recallType = recallType
    existingRecommendationEntity.data.status = status
    existingRecommendationEntity.data.lastModifiedDate = nowDate()
    existingRecommendationEntity.data.lastModifiedBy = updatedByUserName

    return recommendationRepository.save(existingRecommendationEntity)
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
          createdDate = now
        )
      )
    )
  }
}
