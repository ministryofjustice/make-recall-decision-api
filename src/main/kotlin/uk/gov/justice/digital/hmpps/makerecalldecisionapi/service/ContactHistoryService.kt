package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.csv.ContactGroup
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactGroupResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.reader.ContactGroupsCsvReader

@Service
internal class ContactHistoryService(
  private val communityApiClient: CommunityApiClient,
  private val personDetailsService: PersonDetailsService,
  private val userAccessValidator: UserAccessValidator,
  private val documentService: DocumentService,
  private val recommendationService: RecommendationService
) {
  suspend fun getContactHistory(crn: String, featureFlags: FeatureFlags?): ContactHistoryResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      ContactHistoryResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val contactSummary = getContactSummary(crn)
      val contactTypeGroups = buildRelevantContactTypeGroups(contactSummary, featureFlags)
      val recommendationDetails = recommendationService.getRecommendationsInProgressForCrn(crn)

      ContactHistoryResponse(
        personalDetailsOverview = personalDetailsOverview,
        contactSummary = contactSummary,
        contactTypeGroups = contactTypeGroups,
        activeRecommendation = recommendationDetails,
      )
    }
  }

  private suspend fun getContactSummary(crn: String): List<ContactSummaryResponse> {
    val contactSummaryResponse = getValueAndHandleWrappedException(communityApiClient.getContactSummary(crn))?.content
    val allContactDocuments = documentService.getDocumentsByDocumentType(crn, "CONTACT_DOCUMENT")

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
          contactDocuments = allContactDocuments?.filter { document -> document.parentPrimaryKeyId == it.contactId },
          description = it.description,
        )
      }?.toList() ?: emptyList()
  }

  private fun buildRelevantContactTypeGroups(contactSummary: List<ContactSummaryResponse>, featureFlags: FeatureFlags?): List<ContactGroupResponse?> {
    val allRelevantContacts = contactSummary.distinctBy { it.code }

    val contactGroupToUse = if (featureFlags?.flagShowSystemGenerated == true) ContactGroupsCsvReader.getContactGroupsForSystemGeneratedContacts() else ContactGroupsCsvReader.getContactGroups()

    val contactGroups = contactGroupToUse.groupBy(ContactGroup::groupId)
      .entries.mapNotNull { (id, contactGroups) ->
        val contacts = contactGroups.map { it.code }.filter { i -> allRelevantContacts.any { it.code == i } }
        if (contacts.isNotEmpty())
          ContactGroupResponse(id, contactGroups.first().groupName, contacts)
        else
          null
      }

    return addUnknownContactGroupToList(allRelevantContacts, contactGroups, featureFlags)
  }

  private fun addUnknownContactGroupToList(
    allRelevantContacts: List<ContactSummaryResponse>,
    existingContacts: List<ContactGroupResponse>,
    featureFlags: FeatureFlags?
  ): List<ContactGroupResponse> {
    val contactGroupToUse = if (featureFlags?.flagShowSystemGenerated == true) ContactGroupsCsvReader.getContactGroupsForSystemGeneratedContacts() else ContactGroupsCsvReader.getContactGroups()

    val unknownContacts = allRelevantContacts.filter { relevantContact ->
      contactGroupToUse.none { it.code == relevantContact.code }
    }
    val codes = unknownContacts.mapNotNull { it.code }

    return if (codes.isNotEmpty())
      existingContacts + ContactGroupResponse("unknown", "Not categorised", codes)
    else
      existingContacts
  }
}
