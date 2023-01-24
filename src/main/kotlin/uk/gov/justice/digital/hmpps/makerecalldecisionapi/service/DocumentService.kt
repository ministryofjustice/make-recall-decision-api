package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
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
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getDocumentsByDocumentType(crn: String, documentType: String): List<CaseDocument>? {
    log.info(StringUtils.normalizeSpace("About to build documents by document type $documentType for $crn in document service"))
    val groupedDocuments = getValueAndHandleWrappedException(communityApiClient.getGroupedDocuments(crn))
    log.info(StringUtils.normalizeSpace("Got documents for $crn in document service"))
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
    log.info(StringUtils.normalizeSpace("Built documents for $crn in document service"))

    return docs?.let { docList ->
      convictionDocs?.let { convictionDocsList -> docList + convictionDocsList }
    }
  }

  fun getDocumentByCrnAndId(crn: String, documentId: String): ResponseEntity<Resource>? {
    return getValueAndHandleWrappedException(communityApiClient.getDocumentByCrnAndId(crn, documentId))
  }
}
