package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation.SERIALIZABLE
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Service
internal class RecommendationsCleanupTask(
  @Lazy private val recommendationRepository: RecommendationRepository,
  @Lazy private val recommendationStatusRepository: RecommendationStatusRepository,
  private val recommendationService: RecommendationService,
  @Value("\${housekeeping.sendDomainEvents}") private val sendDomainEvents: Boolean = false,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(
    timeUnit = TimeUnit.SECONDS,
    initialDelayString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(10,20) }",
    fixedRateString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(10,20) }",
  )
  @Transactional(isolation = SERIALIZABLE)
  fun softDeleteOldRecommendations() {
    val thresholdDate = LocalDate.now().minusDays(21)
    val openRecommendationIds = recommendationStatusRepository.findStaleRecommendations(thresholdDate)
    recommendationRepository.softDeleteByIds(openRecommendationIds)
    openRecommendationIds.forEach { openRecommendationId ->
      if (sendDomainEvents) {
        val openRecommendation = recommendationRepository.findById(openRecommendationId)
        openRecommendation.ifPresentOrElse(
          {
            recommendationService.sendSystemDeleteRecommendationEvent(
              it.data.crn,
              it.data.createdByUserFullName ?: MrdTextConstants.EMPTY_STRING,
            )
          },
          {
            val message = "Recommendation not found for id $openRecommendationId"
            log.error(message, RecommendationNotFoundException(message))
          },
        )
      }
    }
  }
}
