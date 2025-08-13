package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.cleanup

import ch.qos.logback.classic.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import java.time.LocalDate
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
  fun `domain-activated service deletes stale recommendations and sends out the relevant domain events`() {
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      recommendationService,
      true,
    )
    // given
    val openRecommendationIds = listOf(1L)

    // and
    Mockito.`when`(recommendationStatusRepository.findStaleRecommendations(any())).thenReturn(openRecommendationIds)
    val crn = "mycrn"
    val username = "Bob"
    Mockito.`when`(
      recommendationRepository.findById(any()),
    ).thenReturn(
      Optional.of(
        RecommendationEntity(
          1L,
          RecommendationModel(crn = crn, createdBy = username),
          deleted = false
        )
      ))

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    Mockito.verify(recommendationService).sendSystemDeleteRecommendationEvent(any(), any())

    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("System delete domain event sent for crn::'$crn' username::'$username")
      }
    }
  }

  @Test
  fun `softDeleteOldRecommendations without domain events`() {
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      recommendationService,
      false,
    )
    // given
    val openRecommendationIds = listOf(1L)

    // and
    Mockito.`when`(recommendationStatusRepository.findStaleRecommendations(any())).thenReturn(openRecommendationIds)

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    Mockito.verify(recommendationService, Mockito.never()).sendSystemDeleteRecommendationEvent(any(), any())
  }

  @Test
  fun `softDeleteOldRecommendations deletes old open recommendations`() {
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      recommendationService,
      false,
    )
    // given
    val thresholdDate = LocalDate.now().minusDays(21)
    val openRecommendationIds = listOf(1L)

    // and
    Mockito.`when`(
      recommendationStatusRepository.findStaleRecommendations(thresholdDate),
    ).thenReturn(openRecommendationIds)

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    verify(recommendationStatusRepository).findStaleRecommendations(thresholdDate)
    verify(recommendationRepository).softDeleteByIds(openRecommendationIds)
  }
}