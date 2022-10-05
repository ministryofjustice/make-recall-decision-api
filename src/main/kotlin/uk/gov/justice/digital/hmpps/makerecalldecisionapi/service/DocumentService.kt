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

  fun getDocumentsByDocumentType(crn: String, documentType: String): List<CaseDocument>? {
    val groupedDocuments = getValueAndHandleWrappedException(communityApiClient.getGroupedDocuments(crn))
    val docs: List<CaseDocument>? = groupedDocuments?.documents?.filter { it.type?.code == documentType }
    val convictionDocs: List<CaseDocument>? = groupedDocuments?.convictions?.flatMap { e ->
      if (e.documents != null) {
        e.documents?.filter {
          it.type?.code == documentType
        } as List<CaseDocument>
      } else {
        emptyList()
      }
    }

    return docs?.let { docList ->
      convictionDocs?.let { convictionDocsList -> docList + convictionDocsList }
    }
  }

  fun getDocumentByCrnAndId(crn: String, documentId: String): ResponseEntity<Resource>? {
    return getValueAndHandleWrappedException(communityApiClient.getDocumentByCrnAndId(crn, documentId))
  }
}
