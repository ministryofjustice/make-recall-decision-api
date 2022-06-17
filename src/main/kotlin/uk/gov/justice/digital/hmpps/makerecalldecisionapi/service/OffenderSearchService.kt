package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SearchByCrnResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchByPhraseRequest

@Service
class OffenderSearchService(
  private val offenderSearchApiClient: OffenderSearchApiClient,
  private val communityApiClient: CommunityApiClient
) {
  suspend fun search(crn: String): List<SearchByCrnResponse> {
    val request = OffenderSearchByPhraseRequest(
      phrase = crn
    )
    val apiResponse = offenderSearchApiClient.searchOffenderByPhrase(request)
      .awaitFirstOrNull()?.content

    return apiResponse?.map {
      var name = "${it.firstName} ${it.surname}"

      // Workaround for an issue in Delius Probation Search API which omits key details when a case has ANY exclusion/restriction on it.
      if (it.firstName == null && it.surname == null) {
        val userAccessResponse = communityApiClient.getUserAccess(crn).awaitFirst()

        if (true == userAccessResponse?.userExcluded || true == userAccessResponse?.userRestricted) {
          name = "Limited access"
        } else {
          // This tries to fill in the blank details which are incorrectly omitted by Delius Search API
          // Don't attempt to do this if more than one result as could end up causing the Community API (and our service) to grind to a halt.
          // Shouldn't be an issue at the moment as CRN is the only field that can be used to search so we can only ever get 1 result.
          // Refactor needed if we decide to expand this to include more searchable fields. Hopefully, this will have been fixed
          // by the Delius team by the time we need it and we can rip this code out.
          if (apiResponse.size == 1) {
            val allDetails = communityApiClient.getAllOffenderDetails(crn).awaitFirst()
            name = "${allDetails.firstName} ${allDetails.surname}"
          }
        }
      }

      SearchByCrnResponse(
        name = name,
        dateOfBirth = it.dateOfBirth,
        crn = crn
      )
    }?.toList() ?: emptyList()
  }
}
