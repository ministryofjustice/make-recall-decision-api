package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ManagementOversightResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoManagementOversightException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.localDateTimeFromString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper.Helper.splitDateTime
import java.util.Collections

@Transactional
@Service
internal class ManagementOversightService(
  val recommendationRepository: RecommendationRepository,
  @Value("\${mrd.url}") private val mrdUrl: String? = null
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getManagementOversightResponse(
    crn: String
  ): ResponseEntity<ManagementOversightResponse> {
    val recommendations = recommendationRepository.findByCrn(crn) ?: throw NoRecommendationFoundException("No recommendation found for crn:$crn")
    Collections.sort(recommendations)
    val managerRecallDecision = recommendations[0].data.managerRecallDecision
      ?: throw NoManagementOversightException("No management oversight available for crn:$crn")
    val dateTime = splitDateTime(localDateTimeFromString(managerRecallDecision.createdDate))
    val readableNameOfUser = managerRecallDecision.createdBy
    return ResponseEntity(
      ManagementOversightResponse(
        sensitive = recommendations[0].data.sensitive ?: false,
        notes = "$readableNameOfUser said:\n" +
          "${managerRecallDecision.selected?.details}\n" +
          "View the case summary for ${recommendations[0].data.personOnProbation?.name}: $mrdUrl/cases/${recommendations[0].data.crn}/overview"
      ),
      HttpStatus.OK
    )
  }
}
