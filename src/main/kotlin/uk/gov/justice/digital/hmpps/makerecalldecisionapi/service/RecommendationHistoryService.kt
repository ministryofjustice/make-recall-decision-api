package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationHistoryRepository
import java.time.LocalDate

@Transactional
@Service
internal class RecommendationHistoryService(
  private val recommendationHistoryRepository: RecommendationHistoryRepository,
  private val offenderSearchApiClient: OffenderSearchApiClient,
) {
  fun getRecommendationHistoryByNomsId(nomsId: String, startDate: LocalDate, endDate: LocalDate): RecommendationHistoryResponse {
    val offenderDetails = getValueAndHandleWrappedException(
      offenderSearchApiClient.searchPeople(
        nomsNumber = nomsId,
        page = 0,
        pageSize = 1,
      ),
    )?.content
    val crn = offenderDetails!!.first().otherIds.crn
    val recommendationHistory = recommendationHistoryRepository.findByCrn(crn, startDate, endDate)
    if (recommendationHistory.isEmpty()) throw NoRecommendationFoundException("No recommendation found for nomsId: $nomsId")
    return RecommendationHistoryResponse(
      recommendationId = recommendationHistory.first().recommendationId,
      crn = crn,
      recommendations = recommendationHistory.map { it.recommendation },
    )
  }

  fun getRecommendationHistoryByCrn(crn: String, startDate: LocalDate, endDate: LocalDate): RecommendationHistoryResponse {
    val recommendationHistory = recommendationHistoryRepository.findByCrn(crn, startDate, endDate)
    if (recommendationHistory.isEmpty()) throw NoRecommendationFoundException("No recommendation found for crn: $crn")
    return RecommendationHistoryResponse(
      recommendationId = recommendationHistory.first().recommendationId,
      crn = crn,
      recommendations = recommendationHistory.map { it.recommendation },
    )
  }
}
