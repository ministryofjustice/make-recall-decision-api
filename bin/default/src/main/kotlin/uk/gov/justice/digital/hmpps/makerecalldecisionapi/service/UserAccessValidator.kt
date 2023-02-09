package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

@Component
internal class UserAccessValidator(@Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient) {
  fun checkUserAccess(crn: String): UserAccessResponse? {
    val userAccessResponse = try {
      getValueAndHandleWrappedException(communityApiClient.getUserAccess(crn))
    } catch (webClientResponseException: WebClientResponseException) {
      return if (webClientResponseException.rawStatusCode == 403) {
        Gson().fromJson(webClientResponseException.responseBodyAsString, UserAccessResponse::class.java)
      } else if (webClientResponseException.rawStatusCode == 404) {
        UserAccessResponse(
          userRestricted = false,
          userExcluded = false,
          userNotFound = true,
          exclusionMessage = null,
          restrictionMessage = null
        )
      } else {
        throw webClientResponseException
      }
    }
    return userAccessResponse
  }

  fun isUserExcludedRestrictedOrNotFound(userAccessResponse: UserAccessResponse?): Boolean {
    return userAccessResponse?.userExcluded == true || userAccessResponse?.userRestricted == true || userAccessResponse?.userNotFound == true
  }
}
