package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.LicenceHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi.ReleaseSummaryResponse
import kotlin.streams.toList

@Service
class LicenceHistoryService(
  private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService
) {

  suspend fun getLicenceHistory(crn: String): LicenceHistoryResponse {
    val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
    val contactSummary = getContactSummary(crn)
    val releaseSummary = getReleaseSummary(crn)

    return LicenceHistoryResponse(
      personalDetailsOverview = personalDetailsOverview,
      contactSummary = contactSummary,
      releaseSummary = releaseSummary,
    )
  }

  private suspend fun getContactSummary(crn: String): List<ContactSummaryResponse> {
    val contactSummaryResponse = communityApiClient.getContactSummary(crn).awaitFirstOrNull()?.content

    return contactSummaryResponse
      ?.stream()
      ?.map {
        ContactSummaryResponse(
          contactStartDate = it.contactStart,
          descriptionType = it.type?.description,
          outcome = it.outcome?.description,
          notes = it.notes,
          enforcementAction = it.enforcement?.enforcementAction?.description,
        )
      }?.toList() ?: emptyList()
  }

  private suspend fun getReleaseSummary(crn: String): ReleaseSummaryResponse? {
    return communityApiClient.getReleaseSummary(crn).awaitFirstOrNull()
  }
}
