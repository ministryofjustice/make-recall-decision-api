package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.ContactTypeSummary
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.csv.ContactGroup
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactGroupResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toOverview
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.reader.ContactGroupsCsvReader
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationService
import java.time.LocalDate

@Service
internal class ContactHistoryService(
  private val deliusClient: DeliusClient,
  private val userAccessValidator: UserAccessValidator,
  private val recommendationService: RecommendationService,
) {
  suspend fun getContactHistory(
    crn: String,
    query: String? = null,
    from: LocalDate? = null,
    to: LocalDate? = null,
    typeCodes: List<String> = emptyList(),
    includeSystemGenerated: Boolean = true,
  ): ContactHistoryResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      ContactHistoryResponse(userAccessResponse = userAccessResponse)
    } else {
      val contactHistory = deliusClient.getContactHistory(crn, query, from, to, typeCodes, includeSystemGenerated)
      val contactTypeGroups = buildRelevantContactTypeGroups(contactHistory.summary.types)
      val recommendationDetails = recommendationService.getRecommendationsInProgressForCrn(crn)

      ContactHistoryResponse(
        personalDetailsOverview = contactHistory.personalDetails.toOverview(crn),
        contactSummary = contactHistory.contacts.map {
          ContactSummaryResponse(
            code = it.type.code,
            descriptionType = it.type.description,
            description = it.description,
            contactStartDate = it.startDateTime.toOffsetDateTime(),
            outcome = it.outcome,
            enforcementAction = it.enforcementAction,
            notes = it.notes,
            sensitive = it.sensitive,
            systemGenerated = it.type.systemGenerated,
            contactDocuments = it.documents.map { doc -> CaseDocument(doc.id, doc.name, doc.lastUpdated) },
          )
        },
        contactTypeGroups = contactTypeGroups,
        activeRecommendation = recommendationDetails,
      )
    }
  }

  private fun buildRelevantContactTypeGroups(contactSummary: List<ContactTypeSummary>): List<ContactGroupResponse?> {
    val allRelevantContacts = contactSummary.distinctBy { it.code }

    val contactGroupToUse = ContactGroupsCsvReader.getContactGroupsForSystemGeneratedContacts()

    val contactGroups = contactGroupToUse.groupBy(ContactGroup::groupId)
      .entries.mapNotNull { (id, contactGroups) ->
        val contacts = contactGroups.map { it.code }.filter { i -> allRelevantContacts.any { it.code == i } }
        if (contacts.isNotEmpty()) {
          ContactGroupResponse(id, contactGroups.first().groupName, contacts)
        } else {
          null
        }
      }

    return addUnknownContactGroupToList(allRelevantContacts, contactGroups)
  }

  private fun addUnknownContactGroupToList(
    allRelevantContacts: List<ContactTypeSummary>,
    existingContacts: List<ContactGroupResponse>,
  ): List<ContactGroupResponse> {
    val contactGroupToUse = ContactGroupsCsvReader.getContactGroupsForSystemGeneratedContacts()

    val unknownContacts = allRelevantContacts.filter { relevantContact ->
      contactGroupToUse.none { it.code == relevantContact.code }
    }
    val codes = unknownContacts.map { it.code }

    return if (codes.isNotEmpty()) {
      existingContacts + ContactGroupResponse("unknown", "Not categorised", codes)
    } else {
      existingContacts
    }
  }
}
