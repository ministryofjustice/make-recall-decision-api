package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation.SERIALIZABLE
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import java.time.LocalDate
import java.util.concurrent.TimeUnit

// import java.util.concurrent.TimeUnit

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

  // TODO re-enable once the correctAppInsightsEventsForPreviouslyDeletedStaleRecommendations(..) task has run
//  @Scheduled(
//    timeUnit = TimeUnit.MINUTES,
//    initialDelayString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(10,20) }",
//    fixedRateString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(10,20) }",
//  )
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
              it.data.createdBy ?: MrdTextConstants.EMPTY_STRING,
            )
            log.info("System delete domain event sent for crn::'${it.data.crn}' username::'${it.data.createdBy}")
            sendAppInsightsEvent(it)
          },
          {
            val message = "Recommendation not found for id $openRecommendationId"
            log.error(message, RecommendationNotFoundException(message))
          },
        )
      }
    }
  }

  @Scheduled(
    timeUnit = TimeUnit.SECONDS,
    initialDelayString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(0,60*60) }",
    fixedDelay = -1,
  ) // run immediately once, and never again
  @Transactional(isolation = SERIALIZABLE)
  fun correctAppInsightsEventsForPreviouslyDeletedStaleRecommendations() {
    val recommendationIds = recommendationStatusRepository.findStaleRecommendationsForAppInsightsEventCorrection()
    log.info("${recommendationIds.size} recommendationIds retrieved, processing batch")
    recommendationIds.forEach { recommendationId ->
      val recommendation = recommendationRepository.findById(recommendationId)
      recommendation.ifPresentOrElse(
        {
          sendAppInsightsEvent(it)
          recommendationStatusRepository.save(RecommendationStatusEntity(recommendationId = recommendationId, active = true, name = "SENT_DELETED_TO_APP_INSIGHTS", created = DateTimeHelper.utcNowDateTimeString(), createdBy = "SYSTEM", createdByUserFullName = "SYSTEM"))
        },
        {
          val message = "Recommendation not found for id $recommendationId"
          log.error(message, RecommendationNotFoundException(message))
        },
      )
    }
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
