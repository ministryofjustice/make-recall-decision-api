package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpcsSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpcsSearchResult
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository

@Service
internal class PpcsService(
  private val recommendationRepository: RecommendationRepository,
  private val recommendationStatusRepository: RecommendationStatusRepository,
  @Qualifier("offenderSearchApiClientUserEnhanced") private val offenderSearchApiClient: OffenderSearchApiClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun search(crn: String): PpcsSearchResponse {
    log.info("ppcs searching for crn: " + crn)

    val apiResponse = getValueAndHandleWrappedException(
      offenderSearchApiClient.searchPeople(
        crn = crn,
        page = 0,
        pageSize = 20,
      ),
    )

    log.info("prison api returns " + apiResponse!!.content.size + " results")

    val results = mutableListOf<PpcsSearchResult>()

    for (offender in apiResponse.content) {
      if (!offender.isNameNullOrBlank) {
        log.info("looking for recommendation doc")
        val activeRecDoc = recommendationRepository.findByCrn(offender.otherIds.crn)
          .sorted()
          .firstOrNull()

        if (activeRecDoc != null && isRecommendationReadyForPpcs(activeRecDoc)) {
          log.info("doc is accepted")
          results.add(
            PpcsSearchResult(
              crn = activeRecDoc.data.crn!!,
              name = (offender.firstName + " " + offender.surname).trim(),
              dateOfBirth = offender.dateOfBirth,
              recommendationId = activeRecDoc.id,
            ),
          )
        } else {
          log.info("doc is rejected")
        }
      } else {
        log.info("result is excluded")
      }
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
