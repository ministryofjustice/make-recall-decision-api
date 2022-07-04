package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Recommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository

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
  fun `retrieves person details when no registration available`() {
    val crn = "12345"

    val recommendationToSave = RecommendationEntity(
      recommendation = Recommendation.NOT_RECALL,
      alternateActions = "increase reporting",
      name = "",
      crn = crn
    )

    given(recommendationRepository.save(any()))
      .willReturn(recommendationToSave)

    val response = recommendationService.createRecommendation(
      crn,
      RecommendationRequest(
        recommendation = Recommendation.NOT_RECALL.name,
        alternateActions = "increase reporting"
      )
    )

    assertThat(response.recommendation).isEqualTo("NOT_RECALL")
    assertThat(response.alternateActions).isEqualTo("increase reporting")

    then(recommendationRepository).should().save(recommendationToSave)
  }
}
