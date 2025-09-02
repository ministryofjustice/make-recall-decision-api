package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.cleanup

import net.javacrumbs.shedlock.core.LockAssert
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
import java.time.ZonedDateTime
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

    private val RECOMMENDATION_ID_LOGGING_CHUNK_SIZE = 20
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

  /**
   * Deletes ongoing recommendations, i.e. those created but not downloaded before the configured date and time.
   *
   * <p>Originally written for the FTR48 roll-out, where policy required recalls issued after midnight on 2nd September BST
   * to ask different recall suitability questions, the function has not been removed, as it is likely to be re-used in
   * similar policy changes in the future.
   */
//  @Scheduled(cron = "\${clean-up.ftr48.cron}", zone = "Europe/London")
//  @SchedulerLock(name = "nameIndicatingRelevantRollOut", lockAtLeastFor = "1m", lockAtMostFor = "15m")
  @Transactional(isolation = SERIALIZABLE)
  fun softDeleteActiveRecommendationsNotYetDownloaded() {
    log.info("<project/roll-out name> clean-up task started")
    LockAssert.assertLocked()

    if (LocalDate.now().year != LocalDate.now().year) {
      log.warn("<project/roll-out name> clean-up task is still running, but it is no longer <project/roll-out year>!")
    }

    // We have a startDate and set it to endDate.minusDays(lookBackInDays - 1) for two reasons:
    //  1. To avoid deleting older recommendations that would be selected by the query but shouldn't be deleted, as they
    //     were created when the workflows worked differently.
    //  2. To avoid any overlap with the recurrent clean-up task. Otherwise, the tasks might try to update the same
    //      recommendation and clash (or succeed and both end up sending out the same domain event, which could be a
    //      problem).
    // TODO update the threshold values below based on config for your roll-out
    val thresholdStartDate = ZonedDateTime.now().minusDays(cleanUpConfiguration.recurrent.lookBackInDays - 1)
    val thresholdEndDate = ZonedDateTime.now()
    val idsOfActiveRecommendationsNotYetDownloaded =
      recommendationRepository.findActiveRecommendationsNotYetDownloaded(thresholdStartDate, thresholdEndDate)
    recommendationRepository.softDeleteByIds(idsOfActiveRecommendationsNotYetDownloaded)
    idsOfActiveRecommendationsNotYetDownloaded.chunked(RECOMMENDATION_ID_LOGGING_CHUNK_SIZE).forEach { subListOfIds ->
      log.info("The recommendations with the following IDs were soft deleted, as they were active but not yet downloaded: $subListOfIds")
    }

    if (sendDomainEvents) {
      val activeRecommendationsNotYetDownloaded =
        recommendationRepository.findAllById(idsOfActiveRecommendationsNotYetDownloaded)
      activeRecommendationsNotYetDownloaded.forEach(this::sendDeletionEvents)
    }

    log.info("<project/roll-out name> clean-up task ended")
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
