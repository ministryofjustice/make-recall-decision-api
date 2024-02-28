package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation.SERIALIZABLE
import org.springframework.transaction.annotation.Transactional
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
  @Scheduled(initialDelay = 0, fixedRate = 900000) // Fixed rate: 15 minutes (900000 milliseconds), remove this line after the first deployment
  @Scheduled(cron = "0 0 * 1/1 * ?") // The second schedule will run every hour after the initial 15-minute interval
  @Transactional(isolation = SERIALIZABLE)
  fun softDeleteOldRecommendations() {
    val thresholdDate = LocalDateTime.now().minusDays(21)
    val openRecommendations = recommendationStatusRepository.findStaleRecommendations(thresholdDate) // TODO BS limit to 5 here not delet limit use config not hardcod
    openRecommendations.forEach { recommendation ->
      softDeleteAndLockByIds(openRecommendations)
      if (environment?.activeProfiles?.contains("dev") == false) {
        val openRecommendation = recommendationRepository.findById(recommendation)
          .filter { !it.deleted }
        openRecommendation.ifPresent { rec ->
          recommendationService.sendSystemDeleteRecommendationEvent(rec.data.crn, rec.data.createdByUserFullName ?: MrdTextConstants.EMPTY_STRING)
        }
      }
    }
  }
  fun softDeleteAndLockByIds(ids: List<Long>) {
    val lockedRecommendations = recommendationRepository.lockRecordsForUpdate(ids)
    recommendationRepository.softDeleteByIds(lockedRecommendations)
  }
}
