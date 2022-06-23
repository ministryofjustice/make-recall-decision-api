package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.OffenderSearchApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SearchByCrnResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderSearchByPhraseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException

@Service
class OffenderSearchService(
  @Qualifier("offenderSearchApiClientUserEnhanced") private val offenderSearchApiClient: OffenderSearchApiClient,
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient
) {
  suspend fun search(crn: String): List<SearchByCrnResponse> {
    val request = OffenderSearchByPhraseRequest(
      phrase = crn
    )
    val apiResponse = getValue(offenderSearchApiClient.searchOffenderByPhrase(request))?.content

    return apiResponse?.map {
      var name = "${it.firstName} ${it.surname}"
      var excluded: Boolean? = null
      var restricted: Boolean? = null

      // Check whether an empty name is genuinely due to a restriction or exclusion
      if (it.firstName == null && it.surname == null) {
        val userAccessResponse = getValue(communityApiClient.getUserAccess(crn))

        if (true == userAccessResponse?.userRestricted || true == userAccessResponse?.userExcluded) {
          excluded = userAccessResponse.userExcluded
          restricted = userAccessResponse.userRestricted
        } else {
          name = "No name available"
        }
      }

      SearchByCrnResponse(
        name = name,
        dateOfBirth = it.dateOfBirth,
        crn = crn,
        userExcluded = excluded,
        userRestricted = restricted
      )
    }?.toList() ?: emptyList()
  }

  private fun <T : Any> getValue(mono: Mono<T>?): T? {
    return try {
      val value = mono?.block()
      value ?: value
    } catch (wrappedException: RuntimeException) {
      when (wrappedException.cause) {
        is ClientTimeoutException -> throw wrappedException.cause as ClientTimeoutException
        is PersonNotFoundException -> throw wrappedException.cause as PersonNotFoundException
        else -> throw wrappedException
      }
    }
  }
}
