package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
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
  @Scheduled(initialDelay = 0, fixedDelay = 1814400000) // Initial delay: 0 milliseconds, Fixed delay: 21 days
  @Transactional
  fun softDeleteOldRecommendations() {
    val thresholdDate = LocalDateTime.now().minusDays(21)
    val openRecommendations = recommendationStatusRepository.findStaleRecommendations(thresholdDate)
    recommendationRepository.softDeleteByIds(openRecommendations)
    openRecommendations.forEach {
      if (environment?.activeProfiles?.contains("dev") == false) {
        val openRecommendation = recommendationRepository.findById(it)
        openRecommendation.ifPresent { rec ->
          recommendationService.sendSystemDeleteRecommendationEvent(rec.data.crn)
        }
      }
    }
  }
}
