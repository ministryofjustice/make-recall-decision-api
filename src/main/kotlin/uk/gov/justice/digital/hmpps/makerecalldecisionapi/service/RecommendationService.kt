package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import kotlin.jvm.optionals.getOrNull

@Service
internal class RecommendationService(
  val recommendationRepository: RecommendationRepository
) {
  fun createRecommendation(recommendationRequest: CreateRecommendationRequest): CreateRecommendationResponse {
    val savedRecommendation = recommendationRepository.save(RecommendationEntity(data = RecommendationModel(crn = recommendationRequest.crn)))
    return CreateRecommendationResponse(
      id = savedRecommendation.id
    )
  }

  @OptIn(ExperimentalStdlibApi::class)
  fun getRecommendation(recommendationId: Long): RecommendationResponse {
    val recommendationEntity = recommendationRepository.findById(recommendationId).getOrNull()
      ?: throw NoRecommendationFoundException("No recommendation found for id: $recommendationId")
    return RecommendationResponse(
      id = recommendationEntity.id,
      crn = recommendationEntity.data.crn
    )
  }
}
