package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.cleanup

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation.SERIALIZABLE
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup.CleanUpConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Service
internal class RecommendationsCleanupTask(
  @param:Lazy private val recommendationRepository: RecommendationRepository,
  @param:Lazy private val recommendationStatusRepository: RecommendationStatusRepository,
  private val cleanUpConfiguration: CleanUpConfiguration,
  private val recommendationService: RecommendationService,
  @param:Value("\${housekeeping.sendDomainEvents}") private val sendDomainEvents: Boolean = false,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(
    timeUnit = TimeUnit.SECONDS,
    initialDelayString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(10*60, 60*60) }",
    fixedRateString = "#{T(java.util.concurrent.TimeUnit).HOURS.toSeconds(24)}",
  )
  @Transactional(isolation = SERIALIZABLE)
  fun softDeleteOldRecommendations() {
    val thresholdDate = LocalDate.now().minusDays(cleanUpConfiguration.recurrent.lookBackInDays)
    val openRecommendationIds = recommendationStatusRepository.findStaleRecommendations(thresholdDate)
    recommendationRepository.softDeleteByIds(openRecommendationIds)
    openRecommendationIds.forEach { openRecommendationId ->
      if (sendDomainEvents) {
        val openRecommendation = recommendationRepository.findById(openRecommendationId)
        openRecommendation.ifPresentOrElse(
          this::sendDeletionEvents,
          {
            val message = "Recommendation not found for id $openRecommendationId"
            log.error(message, RecommendationNotFoundException(message))
          },
        )
      }
    }
  }

  private fun sendDeletionEvents(recommendation: RecommendationEntity) {
    recommendationService.sendSystemDeleteRecommendationEvent(
      recommendation.data.crn,
      recommendation.data.createdBy ?: MrdTextConstants.Constants.EMPTY_STRING,
    )
    log.info("System delete domain event sent for crn::'${recommendation.data.crn}' username::'${recommendation.data.createdBy}")
    sendAppInsightsEvent(recommendation)
  }

  private fun sendAppInsightsEvent(it: RecommendationEntity) {
    recommendationService.sendSystemDeleteRecommendationAppInsightsEvent(
      crn = it.data.crn,
      recommendationId = it.id.toString(),
      region = it.data.region,
      username = it.data.createdBy,
    )
  }
}
