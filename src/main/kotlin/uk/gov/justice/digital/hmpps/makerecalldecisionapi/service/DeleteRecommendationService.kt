package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DeleteRecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoDeletedRecommendationRationaleException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository

@Transactional
@Service
internal class DeleteRecommendationService(
  val recommendationRepository: RecommendationRepository,
  val recommendationStatusRepository: RecommendationStatusRepository,
  @Value("\${mrd.url}") private val mrdUrl: String? = null,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getDeleteRecommendationResponse(
    crn: String,
  ): ResponseEntity<DeleteRecommendationResponse> {
    val recommendations = recommendationRepository.findByCrn(crn).sorted()
    val spoDeleteRecommendationRationale = recommendations[0].data.spoDeleteRecommendationRationale
      ?: throw NoDeletedRecommendationRationaleException("No deleted recommendation rationale available for crn:$crn")
    val readableNameOfUser = recommendationStatusRepository.findByRecommendationIdAndName(recommendations[0].id, "REC_DELETED").firstOrNull() { it.active }?.createdByUserFullName
    return ResponseEntity(
      DeleteRecommendationResponse(
        sensitive = recommendations[0].data.sensitive ?: false,
        notes = "$readableNameOfUser said:\n" +
          "${spoDeleteRecommendationRationale}\n" +
          "View the case summary for ${recommendations[0].data.personOnProbation?.name}: $mrdUrl/cases/${recommendations[0].data.crn}/overview",
      ),
      HttpStatus.OK,
    )
  }

  suspend fun getSystemDeleteRecommendationResponse(
    crn: String,
  ): ResponseEntity<DeleteRecommendationResponse> {
    val recommendations = recommendationRepository.findByCrn(crn).sorted()
    return ResponseEntity(
      DeleteRecommendationResponse(
        sensitive = recommendations.firstOrNull()?.data?.sensitive ?: false,
        notes = "Recommendation expired, deleted by system\n" +
          "View the case summary for ${recommendations.firstOrNull()?.data?.personOnProbation?.name}: $mrdUrl/cases/${recommendations.firstOrNull()?.data?.crn}/overview",
      ),
      HttpStatus.OK,
    )
  }
}
