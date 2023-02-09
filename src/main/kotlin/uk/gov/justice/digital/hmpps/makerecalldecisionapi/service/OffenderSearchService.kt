package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.nimbusds.jose.shaded.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SearchByCrnResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchByPhraseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO_NAME_AVAILABLE

@Service
internal class OffenderSearchService(
  @Qualifier("offenderSearchApiClientUserEnhanced") private val offenderSearchApiClient: OffenderSearchApiClient,
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient
) {
  suspend fun search(phrase: String): List<SearchByCrnResponse> {
    val request = OffenderSearchByPhraseRequest(
      phrase = phrase
    )
    val apiResponse = getValueAndHandleWrappedException(offenderSearchApiClient.searchOffenderByPhrase(request))?.content

    return apiResponse?.map {
      var name = "${it.firstName} ${it.surname}"
      var excluded: Boolean? = null
      var restricted: Boolean? = null
      var crn: String = it.otherIds?.crn ?: phrase

      // Check whether an empty name is genuinely due to a restriction or exclusion
      if (it.firstName == null && it.surname == null) {
        try {
          getValueAndHandleWrappedException(communityApiClient.getUserAccess(crn))
          name = NO_NAME_AVAILABLE
        } catch (webClientResponseException: WebClientResponseException) {
          if (webClientResponseException.rawStatusCode == 403) {
            val userAccessResponse = Gson().fromJson(webClientResponseException.responseBodyAsString, UserAccessResponse::class.java)
            excluded = userAccessResponse.userExcluded
            restricted = userAccessResponse?.userRestricted
          } else {
            name = NO_NAME_AVAILABLE
          }
        }
      }

      SearchByCrnResponse(
        name = name,
        dateOfBirth = it.dateOfBirth,
        crn = it.otherIds?.crn,
        userExcluded = excluded,
        userRestricted = restricted
      )
    }?.toList() ?: emptyList()
  }
}
