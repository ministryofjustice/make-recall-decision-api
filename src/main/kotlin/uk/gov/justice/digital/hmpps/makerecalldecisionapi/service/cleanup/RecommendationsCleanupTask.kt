package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.cleanup

import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
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
  @Lazy private val recommendationRepository: RecommendationRepository,
  @Lazy private val recommendationStatusRepository: RecommendationStatusRepository,
  private val cleanUpConfiguration: CleanUpConfiguration,
  private val recommendationService: RecommendationService,
  @Value("\${housekeeping.sendDomainEvents}") private val sendDomainEvents: Boolean = false,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(
    timeUnit = TimeUnit.SECONDS,
    initialDelayString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(10*60, 60*60) }",
    fixedRateString = "#{T(java.util.concurrent.TimeUnit).HOURS.toSeconds(24)}",
  )
  @Transactional(isolation = Isolation.SERIALIZABLE)
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

  @Scheduled(cron = "\${clean-up.ftr48.cron}", zone = "Europe/London")
  @SchedulerLock(name = "ftr48CleanUp", lockAtLeastFor = "1m", lockAtMostFor = "15m")
  @Transactional(isolation = Isolation.SERIALIZABLE)
  fun softDeleteActiveRecommendationsNotYetDownloaded() {
    log.info("FTR48 clean-up task started")
    LockAssert.assertLocked()

    if (LocalDate.now().year != 2025) {
      log.warn("FTR48 clean-up task is still running, but it is no longer 2025!")
    }

    val idsOfActiveRecommendationsNotYetDownloaded =
      recommendationRepository.findActiveRecommendationsNotYetDownloaded(
        cleanUpConfiguration.ftr48.thresholdDateTime.minusDays(cleanUpConfiguration.recurrent.lookBackInDays),
        cleanUpConfiguration.ftr48.thresholdDateTime,
      )
    recommendationRepository.softDeleteByIds(idsOfActiveRecommendationsNotYetDownloaded)
    log.info("The recommendations with the following IDs were soft deleted, as they were active but not yet downloaded: $idsOfActiveRecommendationsNotYetDownloaded")

    if (sendDomainEvents) {
      val activeRecommendationsNotYetDownloaded =
        recommendationRepository.findAllById(idsOfActiveRecommendationsNotYetDownloaded)
      activeRecommendationsNotYetDownloaded.forEach(this::sendDeletionEvents)
    }

    log.info("FTR48 clean-up task ended")
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
