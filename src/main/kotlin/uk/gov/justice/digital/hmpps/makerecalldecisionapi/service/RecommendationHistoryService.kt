package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationHistoryRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Transactional
@Service
class RecommendationHistoryService(
  private val recommendationHistoryRepository: RecommendationHistoryRepository,
) : HmppsProbationSubjectAccessRequestService {
  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val recommendationHistory = if (fromDate != null && toDate != null) recommendationHistoryRepository.findByCrn(crn, fromDate, toDate) else throw InvalidRequestException("Both fromDate and toDate must be present")
    val subjectAccessContent = if (recommendationHistory.isNotEmpty()) {
      HmppsSubjectAccessRequestContent(
        content = RecommendationHistoryResponse(
          recommendationId = recommendationHistory.first().recommendationId,
          crn = recommendationHistory.first().recommendation.crn,
          recommendations = recommendationHistory.map { history -> history.recommendation },
        ),
      )
    } else {
      HmppsSubjectAccessRequestContent(
        content = RecommendationHistoryResponse(
          recommendations = emptyList(),
          crn = crn,
        ),
      )
    }
    return subjectAccessContent
  }
}
