package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.cleanup

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
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
    initialDelayString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(10*60, 60*60) }",
    fixedRateString = "#{T(java.util.concurrent.TimeUnit).HOURS.toSeconds(24)}",
  )
  @Transactional(isolation = Isolation.SERIALIZABLE)
  fun softDeleteOldRecommendations() {
    val thresholdDate = LocalDate.now().minusDays(21)
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

  @Transactional(isolation = Isolation.SERIALIZABLE)
  fun softDeleteActiveRecommendationsNotYetDownloaded() {
    val thresholdDate = ZonedDateTime.of(2025, 9, 2, 0, 0, 0, 0, ZoneId.of("Europe/London"))
    val idsOfActiveRecommendationsNotYetDownloaded =
      recommendationRepository.findActiveRecommendationsNotYetDownloaded(thresholdDate)
    recommendationRepository.softDeleteByIds(idsOfActiveRecommendationsNotYetDownloaded)
    log.info("The recommendations with the following IDs were soft deleted, as they were active but not yet downloaded: $idsOfActiveRecommendationsNotYetDownloaded")

    if (sendDomainEvents) {
      val activeRecommendationsNotYetDownloaded =
        recommendationRepository.findAllById(idsOfActiveRecommendationsNotYetDownloaded)
      activeRecommendationsNotYetDownloaded.forEach(this::sendDeletionEvents)
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