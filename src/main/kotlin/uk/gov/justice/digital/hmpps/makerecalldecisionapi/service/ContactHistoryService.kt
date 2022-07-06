package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.csv.ContactGroup
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactGroupResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ReleaseSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.reader.ContactGroupsCsvReader

@Service
internal class ContactHistoryService(
  private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService,
  private val userAccessValidator: UserAccessValidator,
  private val documentService: DocumentService
) {
  suspend fun getContactHistory(crn: String): ContactHistoryResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedOrRestricted(userAccessResponse)) {
      ContactHistoryResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val contactSummary = getContactSummary(crn)
      val contactTypeGroups = buildRelevantContactTypeGroups(contactSummary)
      val releaseSummary = getReleaseSummary(crn)
      ContactHistoryResponse(
        personalDetailsOverview = personalDetailsOverview,
        contactSummary = contactSummary,
        contactTypeGroups = contactTypeGroups,
        releaseSummary = releaseSummary,
      )
    }
  }

  private suspend fun getContactSummary(crn: String): List<ContactSummaryResponse> {
    val contactSummaryResponse = getValueAndHandleWrappedException(communityApiClient.getContactSummary(crn))?.content
    val allContactDocuments = documentService.getDocumentsForContacts(crn)

    return contactSummaryResponse
      ?.stream()
      ?.map {
        ContactSummaryResponse(
          contactStartDate = it.contactStart,
          code = it.type?.code,
          descriptionType = it.type?.description,
          outcome = it.outcome?.description,
          notes = it.notes,
          enforcementAction = it.enforcement?.enforcementAction?.description,
          systemGenerated = it.type?.systemGenerated,
          sensitive = it.sensitive,
          contactDocuments = allContactDocuments?.filter { document -> document.parentPrimaryKeyId == it.contactId }
        )
      }?.toList() ?: emptyList()
  }

  private fun buildRelevantContactTypeGroups(contactSummary: List<ContactSummaryResponse>): List<ContactGroupResponse?> {
    val allRelevantContacts = contactSummary.distinctBy { it.code }

    val contactGroups = ContactGroupsCsvReader.getContactGroups().groupBy(ContactGroup::groupId)
      .entries.mapNotNull { (id, contactGroups) ->
        val contacts = contactGroups.map { it.code }.filter { i -> allRelevantContacts.any { it.code == i } }
        if (contacts.isNotEmpty())
          ContactGroupResponse(id, contactGroups.first().groupName, contacts)
        else
          null
      }

    return addUnknownContactGroupToList(allRelevantContacts, contactGroups)
  }

  private fun addUnknownContactGroupToList(
    allRelevantContacts: List<ContactSummaryResponse>,
    existingContacts: List<ContactGroupResponse>
  ): List<ContactGroupResponse> {
    val unknownContacts = allRelevantContacts.filter { relevantContact ->
      ContactGroupsCsvReader.getContactGroups().none { it.code == relevantContact.code }
    }
    val codes = unknownContacts.mapNotNull { it.code }

    return if (codes.isNotEmpty())
      existingContacts + ContactGroupResponse("unknown", "Unknown", codes)
    else
      existingContacts
  }

  private suspend fun getReleaseSummary(crn: String): ReleaseSummaryResponse? {
    return getValueAndHandleWrappedException(communityApiClient.getReleaseSummary(crn))
  }
}
