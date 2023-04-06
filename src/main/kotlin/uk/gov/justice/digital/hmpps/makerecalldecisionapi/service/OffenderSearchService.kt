package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.slf4j.LoggerFactory
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
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun search(crn: String? = null, firstName: String? = null, lastName: String? = null): List<SearchByCrnResponse> {
    val request: OffenderSearchByPhraseRequest = buildOffenderSearchByPhraseRequest(crn, firstName, lastName)
    val apiResponse = getValueAndHandleWrappedException(offenderSearchApiClient.searchOffenderByPhrase(request))
    return apiResponse?.map {
      var name = "${it.firstName} ${it.surname}"
      var excluded: Boolean? = null
      var restricted: Boolean? = null
      var crn: String? = it.otherIds?.crn ?: crn

      // Check whether an empty name is genuinely due to a restriction or exclusion
      if (it.firstName == null && it.surname == null) {
        try {
          getValueAndHandleWrappedException(communityApiClient.getUserAccess(crn!!))
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

  private fun buildOffenderSearchByPhraseRequest(
    crn: String?,
    firstName: String?,
    lastName: String?
  ): OffenderSearchByPhraseRequest {
    return OffenderSearchByPhraseRequest(
      crn = crn,
      firstName = firstName,
      surname = lastName
    )
  }
}
