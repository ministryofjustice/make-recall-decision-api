package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RecommendationServiceTest : ServiceTestBase() {

  private lateinit var recommendationService: RecommendationService

  @Mock
  private lateinit var recommendationRepository: RecommendationRepository

  @BeforeEach
  fun setup() {
    recommendationService = RecommendationService(recommendationRepository)
  }

  @Test
  fun `saves a recommendation to the database`() {
    // given
    val crn = "12345"
    val recommendationToSave = RecommendationEntity(data = RecommendationModel(crn = crn))

    // and
    given(recommendationRepository.save(any()))
      .willReturn(recommendationToSave)

    // when
    recommendationService.createRecommendation(CreateRecommendationRequest(crn))

    // then
    then(recommendationRepository).should().save(recommendationToSave)
  }

  @Test
  fun `get a recommendation from the database`() {
    val recommendation = Optional.of(RecommendationEntity(RecommendationModel(crn = "12345")))

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    val result = recommendationService.getRecommendation(456L)

    assertThat(result.id).isEqualTo(recommendation.get().id)
    assertThat(result.crn).isEqualTo(recommendation.get().data.crn)
  }

  @Test
  fun `throws exception when no recommendation available for given id`() {
    val recommendation = Optional.empty<RecommendationEntity>()

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    Assertions.assertThatThrownBy {
      runTest {
        recommendationService.getRecommendation(456L)
      }
    }.isInstanceOf(NoRecommendationFoundException::class.java)
      .hasMessage("No recommendation found for id: 456")

    then(recommendationRepository).should().findById(456L)
  }
}
