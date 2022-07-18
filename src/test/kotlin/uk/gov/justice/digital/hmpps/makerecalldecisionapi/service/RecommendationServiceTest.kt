package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
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
    val recommendationToSave = RecommendationEntity(crn = crn)

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
    val recommendation = Optional.of(RecommendationEntity(crn = "12345"))

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    val result = recommendationService.getRecommendation(456L)

    assertThat(result, equalTo(recommendation))
  }
}
