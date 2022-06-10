package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoActiveConvictionsException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ReleaseDetailsNotFoundException
import kotlin.streams.toList

@Service
class LicenceHistoryService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService
) {

  suspend fun getLicenceHistory(crn: String, filterContacts: Boolean): LicenceHistoryResponse {
    val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
    val contactSummary = getContactSummary(crn, filterContacts)
    val releaseSummary = getReleaseSummary(crn)

    return LicenceHistoryResponse(
      personalDetailsOverview = personalDetailsOverview,
      contactSummary = contactSummary,
      releaseSummary = releaseSummary,
    )
  }

  private suspend fun getContactSummary(crn: String, filterContacts: Boolean): List<ContactSummaryResponse> {
    val contactSummaryResponse = getValue(communityApiClient.getContactSummary(crn, filterContacts))

    return contactSummaryResponse?.content
      ?.stream()
      ?.map {
        ContactSummaryResponse(
          contactStartDate = it.contactStart,
          code = it.type?.code,
          descriptionType = it.type?.description,
          outcome = it.outcome?.description,
          notes = it.notes,
          enforcementAction = it.enforcement?.enforcementAction?.description,
          systemGenerated = it.type?.systemGenerated
        )
      }?.toList() ?: emptyList()
  }

  private suspend fun getReleaseSummary(crn: String): ReleaseSummaryResponse? {
    return getValue(communityApiClient.getReleaseSummary(crn))
  }

  private fun <T : Any> getValue(mono: Mono<T>?): T? {
    return try {
      val value = mono?.block()
      value ?: value
    } catch (wrappedException: RuntimeException) {
      when (wrappedException.cause) {
        is ClientTimeoutException -> throw wrappedException.cause as ClientTimeoutException
        is ReleaseDetailsNotFoundException -> throw wrappedException.cause as NoActiveConvictionsException
        else -> throw wrappedException
      }
    }
  }
}
