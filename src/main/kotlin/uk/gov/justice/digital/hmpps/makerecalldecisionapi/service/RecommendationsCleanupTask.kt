package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toDeleteRecommendationRationaleDomainEventPayload
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import java.time.LocalDateTime

@Service
class RecommendationsCleanupTask(
  @Lazy private val recommendationRepository: RecommendationRepository,
  @Lazy private val recommendationStatusRepository: RecommendationStatusRepository,
  @Lazy private val mrdEventsEmitter: MrdEventsEmitter,
  @Value("\${mrd.url}") private val mrdUrl: String? = null,
  @Value("\${mrd.api.url}") private val mrdApiUrl: String? = null,
  private val environment: Environment? = null,
) {
  @Scheduled(initialDelay = 0, fixedRate = 1814400000) // Initial delay: 0 milliseconds, Fixed rate: 21 days
  @Scheduled(cron = "0 30 19 1/21 * ?") // The second schedule will run at 7:30 PM every 21 days starting from the first day of the month
  @Transactional
  fun softDeleteOldRecommendations() {
    val thresholdDate = LocalDateTime.now().minusDays(21)
    val openRecommendations = recommendationStatusRepository.findStaleRecommendations(thresholdDate)
    recommendationRepository.softDeleteByIds(openRecommendations)
    openRecommendations.forEach {
      if (environment?.activeProfiles?.contains("dev") == false) {
        val openRecommendation = recommendationRepository.findById(it)
        sendSystemDeleteRecommendationEvent(openRecommendation.get().data.crn)
      }
    }
  }

  private fun sendSystemDeleteRecommendationEvent(crn: String?) {
    mrdEventsEmitter.sendEvent(
      toDeleteRecommendationRationaleDomainEventPayload(
        crn = crn,
        recommendationUrl = "$mrdUrl/cases/$crn/overview",
        contactOutcome = "",
        username = "System",
        detailUrl = "$mrdApiUrl/system-delete-recommendation/$crn",
      ),
    )
  }
}
