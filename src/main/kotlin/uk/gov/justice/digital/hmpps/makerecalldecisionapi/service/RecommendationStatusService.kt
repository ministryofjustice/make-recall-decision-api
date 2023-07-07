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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationHistoryRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.localNowDateTime
import java.util.Collections
import kotlin.jvm.optionals.getOrNull

@Transactional
@Service
internal class RecommendationStatusService(
  val recommendationStatusRepository: RecommendationStatusRepository,
  val recommendationHistoryRepository: RecommendationHistoryRepository? = null,
  val recommendationRepository: RecommendationRepository? = null,
  val deliusClient: DeliusClient? = null
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun updateRecommendationStatus(
    recommendationStatusRequest: RecommendationStatusRequest,
    userId: String?,
    readableNameOfUser: String?,
    recommendationId: Long
  ): List<RecommendationStatusResponse> {
    deactivateOldStatus(recommendationId, recommendationStatusRequest, userId, readableNameOfUser)
    return activateNewStatus(
      recommendationStatusRequest,
      recommendationId,
      userId,
      readableNameOfUser
    ).map { it.toRecommendationStatusResponse() }
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
  ): List<RecommendationStatusEntity> {
    return saveAllRecommendationStatuses(
      recommendationStatusRequest.toActiveRecommendationStatusEntity(
        recommendationId = recommendationId,
        userId = userId,
        createdByUserName = readableNameOfUser,
        recommendationHistoryId = findRecHistoryId(recommendationId),
        email = fetchEmailAndPersistDetails(recommendationStatusRequest, userId, recommendationId, readableNameOfUser)
      )
    )
  }

  private fun fetchEmailAndPersistDetails(
    recommendationStatusRequest: RecommendationStatusRequest,
    userId: String?,
    recommendationId: Long?,
    readableNameOfUser: String?
  ): String? {
    val emailAddress =
      if (recommendationStatusRequest.activate.contains("ACO_SIGNED") || recommendationStatusRequest.activate.contains("SPO_SIGNED") || recommendationStatusRequest.activate.contains("PO_RECALL_CONSULT_SPO")) {
        val email = userId?.let { deliusClient?.getUserInfo(it) }?.email
        saveSpoAcoDetailsToRecDoc(email, recommendationId, recommendationStatusRequest, readableNameOfUser)
        email
      } else {
        null
      }
    return emailAddress
  }

  private fun saveSpoAcoDetailsToRecDoc(
    emailAddress: String?,
    recommendationId: Long?,
    recommendationStatusRequest: RecommendationStatusRequest,
    readableNameOfUser: String?
  ) {
    val recommendation = recommendationId?.let { recommendationRepository?.findById(it)?.getOrNull() }
    if (recommendationStatusRequest.activate.contains("ACO_SIGNED")) {
      recommendation?.data?.countersignAcoName = readableNameOfUser
      recommendation?.data?.countersignAcoDateTime = localNowDateTime()
      recommendation?.data?.acoCounterSignEmail = emailAddress
      recommendation?.let { recommendationRepository?.save(it) }
    }
    if (recommendationStatusRequest.activate.contains("SPO_SIGNED")) {
      recommendation?.data?.countersignSpoName = readableNameOfUser
      recommendation?.data?.countersignSpoDateTime = localNowDateTime()
      recommendation?.data?.spoCounterSignEmail = emailAddress
      recommendation?.let { recommendationRepository?.save(it) }
    }
    if (recommendationStatusRequest.activate.contains("PO_RECALL_CONSULT_SPO")) {
      recommendation?.data?.userNamePartACompletedBy = readableNameOfUser
      recommendation?.data?.userEmailPartACompletedBy = emailAddress
      recommendation?.let { recommendationRepository?.save(it) }
    }
  }

  private fun findRecHistoryId(recommendationId: Long): Long? {
    val recommendationHistories =
      recommendationHistoryRepository?.findByrecommendationId(recommendationId = recommendationId)
    if (recommendationHistories != null) {
      Collections.sort(recommendationHistories)
    }
    val recommendationHistoryId = if (recommendationHistories?.isNotEmpty() == true) {
      recommendationHistories[0].id
    } else null
    return recommendationHistoryId
  }

  private fun deactivateOldStatus(
    recommendationId: Long,
    recommendationStatusRequest: RecommendationStatusRequest,
    userId: String?,
    readableNameOfUser: String?
  ) {
    val statusesToDeactivate = recommendationStatusRequest.deActivate
      .flatMap {
        recommendationStatusRepository.findByRecommendationIdAndName(
          recommendationId,
          it
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
