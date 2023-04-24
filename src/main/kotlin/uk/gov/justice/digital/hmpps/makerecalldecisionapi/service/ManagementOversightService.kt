package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ManagementOversightResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
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
    val recommendations = crn?.let { recommendationRepository.findByCrnAndStatus(it, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name)) }
    // recommendationRepository.findByCrn(crn)
    Collections.sort(recommendations)
    val recommendation = recommendations?.first()
    val dateTime = splitDateTime(localDateTimeFromString(recommendations?.get(0)?.data?.managerRecallDecision?.createdDate))
    val readableNameOfUser = recommendation?.data?.managerRecallDecision?.createdBy
    return ResponseEntity(
      ManagementOversightResponse(
        sensitive = recommendation?.data?.sensitive ?: false,
        notes = "Comment added by $readableNameOfUser on ${dateTime.first} at ${dateTime.second}: " +
          "$readableNameOfUser entered the following into the service 'Decide if someone should be recalled or not': " +
          "${recommendation?.data?.managerRecallDecision?.selected?.details} " +
          "View the case summary: $mrdUrl/cases/${recommendation?.data?.crn}/overview"
      ),
      HttpStatus.OK
    )
  }
}
