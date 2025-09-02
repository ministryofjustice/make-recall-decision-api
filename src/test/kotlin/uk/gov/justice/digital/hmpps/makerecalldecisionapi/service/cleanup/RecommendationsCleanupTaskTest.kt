package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.cleanup

import ch.qos.logback.classic.Level
import net.javacrumbs.shedlock.core.LockAssert
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.inOrder
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup.cleanUpConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.cleanup.recurrentCleanUpConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLong
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class RecommendationsCleanupTaskTest {

  @Mock
  private lateinit var recommendationRepository: RecommendationRepository

  @Mock
  private lateinit var recommendationStatusRepository: RecommendationStatusRepository

  @Mock
  private lateinit var recommendationService: RecommendationService

  private val logAppender = findLogAppender(RecommendationsCleanupTask::class.java)

  private lateinit var recommendationsCleanupTask: RecommendationsCleanupTask

  @Test
  fun `domain-event-activated service deletes stale recommendations and sends out the relevant domain events`() {
    val cleanUpConfiguration = cleanUpConfiguration(
      recurrent = recurrentCleanUpConfiguration(
        lookBackInDays = randomLong().mod(100L), // we restrict this to prevent exceeding epoch values later on
      ),
    )
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      cleanUpConfiguration,
      recommendationService,
      true,
    )
    // given
    val presentRecommendationId = randomLong()
    val missingRecommendationId = randomLong()
    val openRecommendationIds = listOf(presentRecommendationId, missingRecommendationId)

    // and
    val thresholdDate = LocalDate.now().minusDays(cleanUpConfiguration.recurrent.lookBackInDays)
    given(recommendationStatusRepository.findStaleRecommendations(thresholdDate)).willReturn(openRecommendationIds)
    val crn = randomString()
    val username = randomString()
    given(
      recommendationRepository.findById(presentRecommendationId),
    ).willReturn(
      Optional.of(
        RecommendationEntity(
          presentRecommendationId,
          RecommendationModel(crn = crn, createdBy = username),
          deleted = false,
        ),
      ),
    )

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    verify(recommendationRepository).softDeleteByIds(openRecommendationIds)
    verify(recommendationService).sendSystemDeleteRecommendationEvent(crn, username)

    with(logAppender.list) {
      assertThat(size).isEqualTo(2)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("System delete domain event sent for crn::'$crn' username::'$username")
      }
      with(get(1)) {
        assertThat(level).isEqualTo(Level.ERROR)
        assertThat(message).isEqualTo("Recommendation not found for id $missingRecommendationId")
      }
    }
  }

  @Test
  fun `domain-event-deactivated service deletes stale recommendations without sending out domain events`() {
    val cleanUpConfiguration = cleanUpConfiguration(
      recurrent = recurrentCleanUpConfiguration(
        lookBackInDays = randomLong().mod(100L), // we restrict this to prevent exceeding epoch values later on
      ),
    )
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      cleanUpConfiguration,
      recommendationService,
      false,
    )
    // given
    val openRecommendationIds = listOf(1L)

    // and
    val thresholdDate = LocalDate.now().minusDays(cleanUpConfiguration.recurrent.lookBackInDays)
    given(recommendationStatusRepository.findStaleRecommendations(thresholdDate)).willReturn(openRecommendationIds)

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    verify(recommendationRepository).softDeleteByIds(openRecommendationIds)
    verify(recommendationService, never()).sendSystemDeleteRecommendationEvent(any(), any())
  }

  @Test
  fun `domain-event-activated FTR48 clean-up task deletes ongoing recommendations and sends out the relevant domain events`() {
    val cleanUpConfiguration = cleanUpConfiguration(
      recurrent = recurrentCleanUpConfiguration(
        lookBackInDays = randomLong().mod(100L), // we restrict this to prevent exceeding epoch values later on
      ),
    )
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      cleanUpConfiguration,
      recommendationService,
      true,
    )

    // given
    val lockAssertMock = Mockito.mockStatic(LockAssert::class.java)
    lockAssertMock.use {
      val idsOfActiveRecommendationsNotYetDownloaded = List(43, { randomLong() })
      // TODO update the threshold values below based on config for your roll-out
      val thresholdStartDate: ZonedDateTime = any()
      val thresholdEndDate: ZonedDateTime = any()
      given(recommendationRepository.findActiveRecommendationsNotYetDownloaded(thresholdStartDate, thresholdEndDate))
        .willReturn(idsOfActiveRecommendationsNotYetDownloaded)

      val activeRecommendationsNotYetDownloaded = List(
        43,
        {
          RecommendationEntity(
            randomLong(),
            RecommendationModel(crn = randomString(), createdBy = randomString()),
            deleted = false,
          )
        },
      )
      given(recommendationRepository.findAllById(idsOfActiveRecommendationsNotYetDownloaded)).willReturn(
        activeRecommendationsNotYetDownloaded,
      )

      // when
      recommendationsCleanupTask.softDeleteActiveRecommendationsNotYetDownloaded()

      // then
      val inOrder = inOrder(LockAssert::class.java, recommendationRepository, recommendationService)
      inOrder.verify(it, LockAssert::assertLocked)
      then(recommendationRepository).should(inOrder).softDeleteByIds(idsOfActiveRecommendationsNotYetDownloaded)
      activeRecommendationsNotYetDownloaded.forEach {
        then(recommendationService).should(inOrder)
          .sendSystemDeleteRecommendationEvent(it.data.crn, it.data.createdBy!!)
      }

      val startUpMessage = listOf("<project/roll-out name> clean-up task started")
      val deletedRecommendationIdMessages = listOf(
        "The recommendations with the following IDs were soft deleted, as they were" +
          " active but not yet downloaded: ${idsOfActiveRecommendationsNotYetDownloaded.subList(0, 20)}",
        "The recommendations with the following IDs were soft deleted, as they were" +
          " active but not yet downloaded: ${idsOfActiveRecommendationsNotYetDownloaded.subList(20, 40)}",
        "The recommendations with the following IDs were soft deleted, as they were" +
          " active but not yet downloaded: ${idsOfActiveRecommendationsNotYetDownloaded.subList(40, 43)}",
      )
      val deletionDomainEventMessages = activeRecommendationsNotYetDownloaded.map {
        "System delete domain event sent for crn::'${it.data.crn}' username::'${it.data.createdBy}"
      }
      val endMessage = listOf("<project/roll-out name> clean-up task ended")
      val expectedInfoMessages =
        startUpMessage + deletedRecommendationIdMessages + deletionDomainEventMessages + endMessage
      with(logAppender.list) {
        this.forEach { assertThat(it.level).isEqualTo(Level.INFO) }
        assertThat(this.map { it.message }).containsExactlyElementsOf(expectedInfoMessages)
      }
    }
  }

  @Test
  fun `domain-event-deactivated FTR48 clean-up task deletes ongoing recommendations without sending out domain events`() {
    val cleanUpConfiguration = cleanUpConfiguration(
      recurrent = recurrentCleanUpConfiguration(
        lookBackInDays = randomLong().mod(100L), // we restrict this to prevent exceeding epoch values later on
      ),
    )
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      cleanUpConfiguration,
      recommendationService,
      false,
    )

    // given
    val lockAssertMock = Mockito.mockStatic(LockAssert::class.java)
    lockAssertMock.use {
      val idsOfActiveRecommendationsNotYetDownloaded = List(43, { randomLong() })
      // TODO update the threshold values below based on config for your roll-out
      val thresholdStartDate: ZonedDateTime = any()
      val thresholdEndDate: ZonedDateTime = any()
      given(recommendationRepository.findActiveRecommendationsNotYetDownloaded(thresholdStartDate, thresholdEndDate))
        .willReturn(idsOfActiveRecommendationsNotYetDownloaded)

      // when
      recommendationsCleanupTask.softDeleteActiveRecommendationsNotYetDownloaded()

      // then
      val inOrder = inOrder(LockAssert::class.java, recommendationRepository)
      inOrder.verify(it, LockAssert::assertLocked)
      then(recommendationRepository).should(inOrder).softDeleteByIds(idsOfActiveRecommendationsNotYetDownloaded)

      val startUpMessage = listOf("<project/roll-out name> clean-up task started")
      val deletedRecommendationIdMessages = listOf(
        "The recommendations with the following IDs were soft deleted, as they were" +
          " active but not yet downloaded: ${idsOfActiveRecommendationsNotYetDownloaded.subList(0, 20)}",
        "The recommendations with the following IDs were soft deleted, as they were" +
          " active but not yet downloaded: ${idsOfActiveRecommendationsNotYetDownloaded.subList(20, 40)}",
        "The recommendations with the following IDs were soft deleted, as they were" +
          " active but not yet downloaded: ${idsOfActiveRecommendationsNotYetDownloaded.subList(40, 43)}",
      )
      val endMessage = listOf("<project/roll-out name> clean-up task ended")
      val expectedInfoMessages = startUpMessage + deletedRecommendationIdMessages + endMessage
      with(logAppender.list) {
        this.forEach { assertThat(it.level).isEqualTo(Level.INFO) }
        assertThat(this.map { it.message }).containsExactlyElementsOf(expectedInfoMessages)
      }
    }
  }
}
