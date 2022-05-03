package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.SearchByCrnResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.offendersearchapi.OffenderSearchByPhraseRequest

@Service
class CaseSummaryService(
  private val offenderSearchApiClient: OffenderSearchApiClient
) {
  suspend fun search(crn: String): SearchByCrnResponse {
    val request = OffenderSearchByPhraseRequest(
      phrase = crn
    )
    val apiResponse = offenderSearchApiClient.searchOffenderByPhrase(request)
      .awaitFirst().content[0]

    return SearchByCrnResponse(
      name = "${apiResponse.firstName} ${apiResponse.surname}",
      dateOfBirth = apiResponse.dateOfBirth,
      crn = crn
    )
  }
}
