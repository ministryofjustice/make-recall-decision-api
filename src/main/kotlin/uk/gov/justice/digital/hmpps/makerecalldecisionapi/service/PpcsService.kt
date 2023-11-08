package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpcsSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpcsSearchResult
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationStatusRepository

@Service
internal class PpcsService(
  private val recommendationRepository: RecommendationRepository,
  private val recommendationStatusRepository: RecommendationStatusRepository,
  @Qualifier("offenderSearchApiClientUserEnhanced") private val offenderSearchApiClient: OffenderSearchApiClient,
) {
  fun search(crn: String): PpcsSearchResponse {
    val apiResponse = getValueAndHandleWrappedException(
      offenderSearchApiClient.searchPeople(
        crn = crn,
        page = 1,
        pageSize = 20,
      ),
    )

    val results = mutableListOf<PpcsSearchResult>()

    for (offender in apiResponse!!.content) {
      if (!offender.isNameNullOrBlank) {
        val doc = recommendationRepository.findByCrn(offender.otherIds.crn)
          .filter {
            val statuses = recommendationStatusRepository.findByRecommendationId(it.id)
              .filter { it.active }

            statuses.any { it.name == "PP_DOCUMENT_CREATED" } && statuses.none { it.name === "BOOKED_IN_PPUD" }
          }
          .firstOrNull()

        if (doc != null) {
          results.add(
            PpcsSearchResult(
              crn = doc.data.crn!!,
              name = (offender.firstName + " " + offender.surname).trim(),
              dateOfBirth = offender.dateOfBirth,
              recommendationId = doc.id,
            ),
          )
        }
      }
    }

    return PpcsSearchResponse(results = results)
  }
}
