package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import java.util.Optional

@Service
internal class RecommendationService(
  val recommendationRepository: RecommendationRepository
) {
  fun createRecommendation(recommendationRequest: CreateRecommendationRequest): CreateRecommendationResponse {
    val savedRecommendation = recommendationRepository.save(RecommendationEntity(crn = recommendationRequest.crn))
    return CreateRecommendationResponse(
      id = savedRecommendation.id
    )
  }

  fun getRecommendation(recommendationId: Long): Optional<RecommendationEntity> {
    return recommendationRepository.findById(recommendationId)
  }
}
