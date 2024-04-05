package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

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
  suspend fun getDeleteRecommendationResponse(
    crn: String,
  ): ResponseEntity<DeleteRecommendationResponse> {
    val recommendations = recommendationRepository.findByCrn(crn).sorted()
    val spoDeleteRecommendationRationale = recommendations[0].data.spoDeleteRecommendationRationale
      ?: throw NoDeletedRecommendationRationaleException("No deleted recommendation rationale available for crn:$crn")
    val readableNameOfUser = recommendationStatusRepository.findByRecommendationIdAndName(recommendations[0].id, "REC_DELETED").firstOrNull { it.active }?.createdByUserFullName
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
    return ResponseEntity(
      DeleteRecommendationResponse(
        sensitive = false,
        notes = "Recommendation automatically deleted by Consider a Recall. This is because there is an old, incomplete Part A or decision not to recall letter.\n" +
          "View the case summary: $mrdUrl/cases/$crn/overview",
      ),
      HttpStatus.OK,
    )
  }
}
