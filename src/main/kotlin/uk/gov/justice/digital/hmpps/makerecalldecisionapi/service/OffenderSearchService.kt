package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SearchByCrnResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchByPhraseRequest
import kotlin.streams.toList

@Service
class OffenderSearchService(
  private val offenderSearchApiClient: OffenderSearchApiClient
) {
  suspend fun search(crn: String): List<SearchByCrnResponse> {
    val request = OffenderSearchByPhraseRequest(
      phrase = crn
    )
    val apiResponse = offenderSearchApiClient.searchOffenderByPhrase(request)
      .awaitFirstOrNull()?.content

    return apiResponse
      ?.stream()
      ?.map {
        SearchByCrnResponse(
          name = "${it.firstName} ${it.surname}",
          dateOfBirth = it.dateOfBirth,
          crn = crn
        )
      }?.toList() ?: emptyList()
  }
}
