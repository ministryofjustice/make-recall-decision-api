package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toActiveRecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationUpdateException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UpdateExceptionTypes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.toRecommendationStatusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper

@Transactional
@Service
internal class RecommendationStatusService(
  val recommendationStatusRepository: RecommendationStatusRepository
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun updateRecommendationStatus(
    recommendationStatusRequest: RecommendationStatusRequest,
    userId: String?,
    readableNameOfUser: String?,
    recommendationId: Long
  ): RecommendationStatusResponse? {
    deactivateOldStatus(recommendationId, recommendationStatusRequest, userId, readableNameOfUser)
    return activateNewStatus(recommendationStatusRequest, recommendationId, userId, readableNameOfUser)
      ?.toRecommendationStatusResponse()
  }

  suspend fun fetchRecommendationStatuses(
    recommendationId: Long
  ): List<RecommendationStatusResponse> {
    return recommendationStatusRepository.findByRecommendationId(recommendationId)
      .map { it.toRecommendationStatusResponse() }
  }

  private fun activateNewStatus(
    recommendationStatusRequest: RecommendationStatusRequest,
    recommendationId: Long,
    userId: String?,
    readableNameOfUser: String?
  ): RecommendationStatusEntity? {
    val newStatusToActivate = saveRecommendationStatus(
      recommendationStatusRequest.toActiveRecommendationStatusEntity(
        recommendationId = recommendationId,
        userId = userId,
        createdByUserName = readableNameOfUser
      )
    )
    log.info("recommendation status ${newStatusToActivate?.status} for ${newStatusToActivate?.recommendationId} activated")
    return newStatusToActivate
  }

  private fun deactivateOldStatus(
    recommendationId: Long,
    recommendationStatusRequest: RecommendationStatusRequest,
    userId: String?,
    readableNameOfUser: String?
  ) {
    val statusesToDeactivate = recommendationStatusRepository.findByRecommendationIdAndStatus(
      recommendationId,
      recommendationStatusRequest.deActivate
    )
    if (statusesToDeactivate.isNotEmpty()) {
      statusesToDeactivate.map {
        it.active = false
        it.modifiedBy = userId
        it.modifiedByUserName = readableNameOfUser
        it.modified = DateTimeHelper.utcNowDateTimeString()
      }
      saveAllRecommendationStatuses(statusesToDeactivate) // TODO - should mutate list contents!!
      log.info("recommendation status ${recommendationStatusRequest.deActivate} for $recommendationId deactivated")
    } else {
      log.info("no active recommendation status ${recommendationStatusRequest.deActivate} found for $recommendationId")
    }
  }

  private fun saveRecommendationStatus(existingRecommendationStatus: RecommendationStatusEntity): RecommendationStatusEntity? {
    return try {
      recommendationStatusRepository.save(existingRecommendationStatus)
    } catch (ex: Exception) {
      throw RecommendationUpdateException(
        message = "Update failed for recommendation id:: ${existingRecommendationStatus.id}$ex.message",
        error = UpdateExceptionTypes.RECOMMENDATION_STATUS_UPDATE_FAILED.toString()
      )
    }
  }

  private fun saveAllRecommendationStatuses(existingRecommendationStatusList: List<RecommendationStatusEntity>): List<RecommendationStatusEntity> {
    return try {
      recommendationStatusRepository.saveAll(existingRecommendationStatusList)
    } catch (ex: Exception) {
      throw RecommendationUpdateException(
        message = "Update failed for recommendation id:: ${existingRecommendationStatusList.firstOrNull()?.id}$ex.message",
        error = UpdateExceptionTypes.RECOMMENDATION_STATUS_UPDATE_FAILED.toString()
      )
    }
  }
}
