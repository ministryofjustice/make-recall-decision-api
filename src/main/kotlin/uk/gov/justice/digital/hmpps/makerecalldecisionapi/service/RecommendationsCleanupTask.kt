package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation.SERIALIZABLE
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.time.LocalDateTime

@Service
internal class RecommendationsCleanupTask(
  @Lazy private val recommendationRepository: RecommendationRepository,
  @Lazy private val recommendationStatusRepository: RecommendationStatusRepository,
  private val recommendationService: RecommendationService,
  @Value("\${mrd.url}") private val mrdUrl: String? = null,
  @Value("\${mrd.api.url}") private val mrdApiUrl: String? = null,
  private val environment: Environment? = null,
) {
  @Scheduled(cron = "0 */15 * ? * *") // run every 15 mins
  @Transactional(isolation = SERIALIZABLE)
  fun softDeleteOldRecommendations() {
    val thresholdDate = LocalDateTime.now().minusDays(21)
    val openRecommendationIds = recommendationStatusRepository.findStaleRecommendations(thresholdDate)
    val recommendationsDeleted = lockAndSoftDeleteByIds(openRecommendationIds)
    recommendationsDeleted.forEach { deleted ->
      if (environment?.activeProfiles?.contains("dev") == false) {
        recommendationService.sendSystemDeleteRecommendationEvent(deleted.data.crn, deleted.data.createdByUserFullName ?: MrdTextConstants.EMPTY_STRING)
      }
    }
  }
  fun lockAndSoftDeleteByIds(ids: List<Long>): List<RecommendationEntity> {
    val recommendationDtos = recommendationRepository.lockRecordsForUpdate(ids)
    recommendationRepository.softDeleteByIds(recommendationDtos.map { it.id })
    return recommendationDtos
  }
}
