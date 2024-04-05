package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
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

  private lateinit var recommendationsCleanupTask: RecommendationsCleanupTask

  @Test
  fun `softDeleteOldRecommendations with domain events`() {
    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      recommendationService,
      true,
    )
    // given
    val openRecommendationIds = listOf(1L)

    // and
    `when`(recommendationStatusRepository.findStaleRecommendations(any())).thenReturn(openRecommendationIds)
    `when`(
      recommendationRepository.findById(any()),
    ).thenReturn(Optional.of(RecommendationEntity(1L, RecommendationModel(crn = "mycrn", createdByUserFullName = "Bob"), deleted = false)))

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    Mockito.verify(recommendationService).sendSystemDeleteRecommendationEvent(any(), any())
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
    `when`(recommendationStatusRepository.findStaleRecommendations(any())).thenReturn(openRecommendationIds)

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    Mockito.verify(recommendationService, never()).sendSystemDeleteRecommendationEvent(any(), any())
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
    `when`(
      recommendationStatusRepository.findStaleRecommendations(thresholdDate),
    ).thenReturn(openRecommendationIds)

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    verify(recommendationStatusRepository).findStaleRecommendations(thresholdDate)
    verify(recommendationRepository).softDeleteByIds(openRecommendationIds)
  }
}
