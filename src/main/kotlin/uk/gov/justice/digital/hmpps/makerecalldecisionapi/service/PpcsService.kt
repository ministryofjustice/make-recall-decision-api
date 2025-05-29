package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.toPersonalDetailsOverview
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.joinToString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpcsSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpcsSearchResult
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository

@Service
internal class PpcsService(
  private val recommendationRepository: RecommendationRepository,
  private val recommendationStatusRepository: RecommendationStatusRepository,
  private val deliusClient: DeliusClient,
  private val userAccessValidator: UserAccessValidator,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun search(crn: String): PpcsSearchResponse {
    log.info("ppcs searching for crn: " + crn)

    val apiResponse = deliusClient.findByCrn(crn)?.takeIf {
      userAccessValidator.checkUserAccess(crn, SecurityContextHolder.getContext().authentication.name)
        .run { !userRestricted && !userExcluded }
    }

    log.info("delius returns ${if (apiResponse != null) 1 else 0} results")

    val results = mutableListOf<PpcsSearchResult>()

    if (apiResponse != null && apiResponse.status != 404) {
      log.info("looking for recommendation doc")
      val personalDetails = apiResponse.toPersonalDetailsOverview(crn)
      val activeRecDoc = recommendationRepository.findByCrn(personalDetails.identifiers.crn).minOrNull()

      if (activeRecDoc != null && isRecommendationReadyForPpcs(activeRecDoc)) {
        log.info("doc is accepted")
        results.add(
          PpcsSearchResult(
            crn = activeRecDoc.data.crn!!,
            name = (personalDetails.name.forename + " " + personalDetails.name.surname).trim(),
            dateOfBirth = personalDetails.dateOfBirth,
            recommendationId = activeRecDoc.id,
          ),
        )
      } else {
        log.info("doc is rejected")
      }
    } else {
      log.info("result is not found or excluded")
    }

    return PpcsSearchResponse(results = results)
  }

  private fun isRecommendationReadyForPpcs(doc: RecommendationEntity?): Boolean {
    val activeRecommendationOpen = if (doc != null) {
      log.info("found doc " + doc.id)
      val statuses = recommendationStatusRepository.findByRecommendationId(doc.id).filter { it.active }
      log.info("statuses: " + statuses.map { it.name }.joinToString(","))
      statuses.any { it.name == "PP_DOCUMENT_CREATED" } && statuses.none { it.name == "REC_CLOSED" } && !doc.deleted
    } else {
      false
    }
    return activeRecommendationOpen
  }
}
