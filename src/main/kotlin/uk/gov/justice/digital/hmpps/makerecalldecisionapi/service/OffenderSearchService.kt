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

      // Workaround for an issue in Delius Probation Search API which omits key details when a case has ANY exclusion/restriction on it.
      if (it.firstName == null && it.surname == null) {
        val userAccessResponse = getValue(communityApiClient.getUserAccess(crn))

        if (true == userAccessResponse?.userExcluded || true == userAccessResponse?.userRestricted) {
          name = "Limited access"
        } else {
          // This tries to fill in the blank details which are incorrectly omitted by Delius Search API
          // Don't attempt to do this if more than one result as could end up causing the Community API (and our service) to grind to a halt.
          // Shouldn't be an issue at the moment as CRN is the only field that can be used to search so we can only ever get 1 result.
          // Refactor needed if we decide to expand this to include more searchable fields. Hopefully, this will have been fixed
          // by the Delius team by the time we need it and we can rip this code out.
          if (apiResponse.size == 1) {
            val allDetails = getValue(communityApiClient.getAllOffenderDetails(crn))
            name = "${allDetails?.firstName} ${allDetails?.surname}"
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
