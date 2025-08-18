package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.cleanup

import ch.qos.logback.classic.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLong
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate
import java.time.ZoneId
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
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      recommendationService,
      true,
    )
    // given
    val presentRecommendationId = randomLong()
    val missingRecommendationId = randomLong()
    val openRecommendationIds = listOf(presentRecommendationId, missingRecommendationId)

    // and
    val thresholdDate = LocalDate.now().minusDays(21)
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
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      recommendationService,
      false,
    )
    // given
    val openRecommendationIds = listOf(1L)

    // and
    val thresholdDate = LocalDate.now().minusDays(21)
    given(recommendationStatusRepository.findStaleRecommendations(thresholdDate)).willReturn(openRecommendationIds)

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    verify(recommendationRepository).softDeleteByIds(openRecommendationIds)
    verify(recommendationService, never()).sendSystemDeleteRecommendationEvent(any(), any())
  }

  @Test
  fun `domain-event-activated FTR48 clean-up task deletes ongoing recommendations and sends out the relevant domain events`() {
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      recommendationService,
      true,
    )

    // given
    val presentRecommendationId = randomLong()
    val missingRecommendationId = randomLong()
    val idsOfActiveRecommendationsNotYetDownloaded = listOf(presentRecommendationId, missingRecommendationId)
    val thresholdDate = ZonedDateTime.of(2025, 9, 2, 0, 0, 0, 0, ZoneId.of("Europe/London"))
    given(recommendationRepository.findActiveRecommendationsNotYetDownloaded(thresholdDate)).willReturn(
      idsOfActiveRecommendationsNotYetDownloaded,
    )

    val crn = randomString()
    val username = randomString()
    given(
      recommendationRepository.findAllById(idsOfActiveRecommendationsNotYetDownloaded),
    ).willReturn(
      listOf(
        RecommendationEntity(
          presentRecommendationId,
          RecommendationModel(crn = crn, createdBy = username),
          deleted = false,
        ),
      ),
    )

    // when
    recommendationsCleanupTask.softDeleteActiveRecommendationsNotYetDownloaded()

    // then
    verify(recommendationRepository).softDeleteByIds(idsOfActiveRecommendationsNotYetDownloaded)
    verify(recommendationService).sendSystemDeleteRecommendationEvent(crn, username)

    with(logAppender.list) {
      assertThat(size).isEqualTo(2)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo(
          "The recommendations with the following IDs were soft deleted, as they were" +
            " active but not yet downloaded: $idsOfActiveRecommendationsNotYetDownloaded",
        )
      }
      with(get(1)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("System delete domain event sent for crn::'$crn' username::'$username")
      }
    }
  }

  @Test
  fun `domain-event-deactivated FTR48 clean-up task deletes ongoing recommendations without sending out domain events`() {
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      recommendationService,
      false,
    )

    // given
    val idsOfActiveRecommendationsNotYetDownloaded = listOf(randomLong(), randomLong())
    val thresholdDate = ZonedDateTime.of(2025, 9, 2, 0, 0, 0, 0, ZoneId.of("Europe/London"))
    given(recommendationRepository.findActiveRecommendationsNotYetDownloaded(thresholdDate)).willReturn(
      idsOfActiveRecommendationsNotYetDownloaded,
    )

    // when
    recommendationsCleanupTask.softDeleteActiveRecommendationsNotYetDownloaded()

    // then
    verify(recommendationRepository).softDeleteByIds(idsOfActiveRecommendationsNotYetDownloaded)

    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo(
          "The recommendations with the following IDs were soft deleted, as they were active but not yet downloaded:" +
            " $idsOfActiveRecommendationsNotYetDownloaded",
        )
      }
    }
  }
}