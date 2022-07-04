package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Recommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository

@Service
internal class RecommendationService(
  val recommendationRepository: RecommendationRepository
) {
  fun createRecommendation(crn: String, recommendationRequest: RecommendationRequest): RecommendationResponse {
    // TODO use crn to call PersonDetailsService, RiskService and OffenderSearchService to
    // populate those particular fields in RecommendationResponse
    val savedRecommendation = recommendationRepository.save(
      RecommendationEntity(
        crn = crn,
        recommendation = Recommendation.valueOf(recommendationRequest.recommendation!!),
        alternateActions = recommendationRequest.alternateActions!!,
        name = ""
      )
    )
    return RecommendationResponse(
      recommendation = savedRecommendation.recommendation.name,
      alternateActions = savedRecommendation.alternateActions
    )
  }
}
