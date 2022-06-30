package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException

@Component
class UserAccessValidator(private val communityApiClient: CommunityApiClient) {
  fun checkUserAccess(crn: String): UserAccessResponse? {
    val userAccessResponse = try {
      getValue(communityApiClient.getUserAccess(crn))
    } catch (webClientResponseException: WebClientResponseException) {
      return if (webClientResponseException.rawStatusCode == 403) {
        Gson().fromJson(webClientResponseException.responseBodyAsString, UserAccessResponse::class.java)
      } else {
        throw webClientResponseException
      }
    }
    return userAccessResponse
  }

  fun isUserExcludedOrRestricted(userAccessResponse: UserAccessResponse?): Boolean {
    return userAccessResponse?.userExcluded == true || userAccessResponse?.userRestricted == true
  }

  private fun <T : Any> getValue(mono: Mono<T>?): T? {
    return try {
      val value = mono?.block()
      value ?: value
    } catch (wrappedException: RuntimeException) {
      when (wrappedException.cause) {
        is ClientTimeoutException -> throw wrappedException.cause as ClientTimeoutException
        is WebClientResponseException -> throw wrappedException.cause as WebClientResponseException
        else -> throw wrappedException
      }
    }
  }
}
