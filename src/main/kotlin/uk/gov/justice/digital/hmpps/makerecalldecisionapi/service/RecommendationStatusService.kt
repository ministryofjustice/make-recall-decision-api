package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toActiveRecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationUpdateException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UpdateExceptionTypes
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.toRecommendationStatusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper

@Transactional
@Service
internal class RecommendationStatusService(
  val recommendationStatusRepository: RecommendationStatusRepository,
  val recommendationRepository: RecommendationRepository? = null,
  val deliusClient: DeliusClient? = null,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun updateRecommendationStatus(
    recommendationStatusRequest: RecommendationStatusRequest,
    userId: String?,
    readableNameOfUser: String?,
    recommendationId: Long,
  ): List<RecommendationStatusResponse> {
    deactivateOldStatus(recommendationId, recommendationStatusRequest, userId, readableNameOfUser)
    return activateNewStatus(
      recommendationStatusRequest,
      recommendationId,
      userId,
      readableNameOfUser,
    ).map { it.toRecommendationStatusResponse() }
  }

  suspend fun fetchRecommendationStatuses(
    recommendationId: Long,
  ): List<RecommendationStatusResponse> = recommendationStatusRepository.findByRecommendationId(recommendationId)
    .map { it.toRecommendationStatusResponse() }
    .sortedBy { it.modified }

  private fun activateNewStatus(
    recommendationStatusRequest: RecommendationStatusRequest,
    recommendationId: Long,
    userId: String?,
    readableNameOfUser: String?,
  ): List<RecommendationStatusEntity> {
    val statuses = recommendationStatusRequest.toActiveRecommendationStatusEntity(
      recommendationId = recommendationId,
      userId = userId,
      createdByUserName = readableNameOfUser,
      email = fetchEmailAndPersistDetails(recommendationStatusRequest, userId),
    )
    return saveAllRecommendationStatuses(statuses)
  }

  private fun fetchEmailAndPersistDetails(
    recommendationStatusRequest: RecommendationStatusRequest,
    userId: String?,
  ): String? {
    val emailAddress =
      if (recommendationStatusRequest.activate.contains("ACO_SIGNED") ||
        recommendationStatusRequest.activate.contains("SPO_SIGNED") ||
        recommendationStatusRequest.activate.contains(
          "PO_RECALL_CONSULT_SPO",
        )
      ) {
        userId?.let { deliusClient?.getUserInfo(it) }?.email
      } else {
        null
      }
    return emailAddress
  }

  private fun deactivateOldStatus(
    recommendationId: Long,
    recommendationStatusRequest: RecommendationStatusRequest,
    userId: String?,
    readableNameOfUser: String?,
  ) {
    val statusesToDeactivate = recommendationStatusRequest.deActivate
      .flatMap {
        recommendationStatusRepository.findByRecommendationIdAndName(
          recommendationId,
          it,
        )
      }
    if (statusesToDeactivate.isNotEmpty()) {
      statusesToDeactivate
        .filter { it.name != null }
        .map {
          it.active = false
          it.modifiedBy = userId
          it.modifiedByUserFullName = readableNameOfUser
          it.modified = DateTimeHelper.utcNowDateTimeString()
        }
      saveAllRecommendationStatuses(statusesToDeactivate)
      log.info("recommendation status ${recommendationStatusRequest.deActivate} for $recommendationId deactivated")
    } else {
      log.info("no active recommendation status ${recommendationStatusRequest.deActivate} found for $recommendationId")
    }
  }

  private fun saveAllRecommendationStatuses(existingRecommendationStatusList: List<RecommendationStatusEntity>): List<RecommendationStatusEntity> = try {
    recommendationStatusRepository.saveAll(existingRecommendationStatusList)
  } catch (ex: Exception) {
    throw RecommendationUpdateException(
      message = "Update failed for recommendation id:: ${existingRecommendationStatusList.firstOrNull()?.id}$ex.message",
      error = UpdateExceptionTypes.RECOMMENDATION_STATUS_UPDATE_FAILED.toString(),
    )
  }
}
