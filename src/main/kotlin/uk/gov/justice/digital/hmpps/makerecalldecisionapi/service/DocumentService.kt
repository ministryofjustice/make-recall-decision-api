package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CaseDocument

@Service
internal class DocumentService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient
) {

  fun getDocumentsForContacts(crn: String): List<CaseDocument>? {
    val groupedDocuments = getValueAndHandleWrappedException(communityApiClient.getGroupedDocuments(crn))
    return groupedDocuments?.documents?.filter { it.type?.code == "CONTACT_DOCUMENT" }
  }

  fun getDocumentsForConvictions(crn: String): List<CaseDocument>? {
    val groupedDocuments = getValueAndHandleWrappedException(communityApiClient.getGroupedDocuments(crn))

    return groupedDocuments?.convictions?.flatMap { e ->
      e.documents?.filter { it.type?.code == "CONVICTION_DOCUMENT" } as List<CaseDocument>
    }
  }

  fun getDocumentByCrnAndId(crn: String, documentId: String): ResponseEntity<Resource>? {
    return getValueAndHandleWrappedException(communityApiClient.getDocumentByCrnAndId(crn, documentId))
  }
}
