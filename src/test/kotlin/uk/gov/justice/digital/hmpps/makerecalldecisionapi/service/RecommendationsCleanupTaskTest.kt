package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.springframework.core.env.Environment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class RecommendationsCleanupTaskTest {

  @Mock
  private lateinit var recommendationRepository: RecommendationRepository

  @Mock
  private lateinit var recommendationStatusRepository: RecommendationStatusRepository

  @Mock
  private lateinit var mrdEventsEmitter: MrdEventsEmitter

  @Mock
  private lateinit var environment: Environment

  private val mrdUrl: String = "http://example.com"
  private val mrdApiUrl: String = "http://api.example.com"

  private lateinit var recommendationsCleanupTask: RecommendationsCleanupTask

  @ParameterizedTest
  @ValueSource(strings = ["default", "dev"])
  fun `softDeleteOldRecommendations deletes old open recommendations`(activeProfile: String) {
    `when`(environment.activeProfiles).thenReturn(arrayOf(activeProfile))

    recommendationsCleanupTask = RecommendationsCleanupTask(
      recommendationRepository,
      recommendationStatusRepository,
      mrdEventsEmitter,
      mrdUrl,
      mrdApiUrl,
      environment,
    )
    // given
    val thresholdDate = LocalDateTime.now().minusDays(21)
    val openRecommendationIds = listOf(1L)
    val crn = "CRN123"

    // and
    `when`(
      recommendationStatusRepository.findStaleRecommendations(
        argThat { argument ->
          argument.toLocalDate().equals(thresholdDate.toLocalDate())
        },
      ),
    ).thenReturn(openRecommendationIds)

    if (activeProfile == "default") {
      `when`(recommendationRepository.findById(1L)).thenReturn(
        Optional.of(RecommendationEntity(data = RecommendationModel(crn = crn))),
      )
    }

    // when
    recommendationsCleanupTask.softDeleteOldRecommendations()

    // then
    verify(recommendationStatusRepository).findStaleRecommendations(
      argThat { argument ->
        argument.toLocalDate().equals(thresholdDate.toLocalDate())
      },
    )
    verify(recommendationRepository).softDeleteByIds(openRecommendationIds)
    if (activeProfile == "default") {
      Mockito.verify(mrdEventsEmitter).sendEvent(any())
    } else {
      Mockito.verify(mrdEventsEmitter, never()).sendEvent(any())
    }
  }
}
