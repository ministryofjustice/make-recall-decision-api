package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.FetchRecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecallTypeOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Recommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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
    val lastModifiedDate = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'"))
    val savedRecommendation = saveRecommendationEntity(recommendationRequest, username, lastModifiedDate.toString())

    return RecommendationResponse(
      id = savedRecommendation?.id,
      status = savedRecommendation?.data?.status
    )
  }

  @OptIn(ExperimentalStdlibApi::class)
  fun getRecommendation(recommendationId: Long): FetchRecommendationResponse {
    val recommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
    return FetchRecommendationResponse(
      id = recommendationEntity.id,
      crn = recommendationEntity.data.crn,
      recallType = recommendationEntity.data.recommendation,
      status = recommendationEntity.data.status
    )
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Transactional
  fun updateRecommendation(updateRecommendationRequest: UpdateRecommendationRequest, recommendationId: Long): RecommendationResponse {
    val existingRecommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")

    val updatedRecommendationEntity = updateRecommendation(existingRecommendationEntity, updateRecommendationRequest)

    return RecommendationResponse(
      id = updatedRecommendationEntity!!.id!!,
      crn = updatedRecommendationEntity.data.crn,
      status = updatedRecommendationEntity.data.status,
      recallType = updateRecommendationRequest.recallType
        ?: RecallType(
          value = updatedRecommendationEntity.data.recommendation ?: existingRecommendationEntity.data.recommendation,
          options = useExistingOptions()
        )
    )
  }

  private fun updateRecommendation(
    existingRecommendationEntity: RecommendationEntity,
    updateRecommendationRequest: UpdateRecommendationRequest
  ): RecommendationEntity? {
    val updatedRecallType = updateRecommendationRequest.recallType?.value
    val originalRecallType = existingRecommendationEntity.data.recommendation?.name
    val recallType = updatedRecallType ?: originalRecallType
    val recommendation = if (recallType != null) {
      Recommendation.valueOf(recallType.toString())
    } else {
      null
    }

    val status = updateRecommendationRequest?.status ?: existingRecommendationEntity.data.status

    return recommendationRepository.save(
      existingRecommendationEntity.copy(
        id = existingRecommendationEntity.id,
        data = RecommendationModel(
          crn = existingRecommendationEntity.data.crn,
          recommendation = recommendation,
          status = status,
          lastModifiedDate = existingRecommendationEntity.data.lastModifiedDate,
          lastModifiedBy = existingRecommendationEntity.data.lastModifiedBy
        )
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
      )
    } else {
      null
    }
  }

  private fun useExistingOptions() = listOf(
    RecallTypeOption(
      value = Recommendation.NO_RECALL.name,
      text = Recommendation.NO_RECALL.text
    ),
    RecallTypeOption(
      value = Recommendation.FIXED_TERM.name,
      text = Recommendation.FIXED_TERM.text
    ),
    RecallTypeOption(
      value = Recommendation.STANDARD.name,
      text = Recommendation.STANDARD.text
    )
  )

  private fun saveRecommendationEntity(
    recommendationRequest: CreateRecommendationRequest,
    username: String?,
    lastModifiedDate: String?
  ): RecommendationEntity? {
    return recommendationRepository.save(
      RecommendationEntity(
        data = RecommendationModel(
          crn = recommendationRequest.crn,
          status = Status.DRAFT,
          lastModifiedBy = username,
          lastModifiedDate = lastModifiedDate
        )
      )
    )
  }
}
