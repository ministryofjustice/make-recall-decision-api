package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.DocumentService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class DocumentController(
  private val documentService: DocumentService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/documents/{documentId}")
  @Operation(summary = "Returns the requested document")
  suspend fun documentRequest(@PathVariable("crn") crn: String, @PathVariable("documentId") documentId: String): ResponseEntity<Resource>? {
    log.info(normalizeSpace("Document endpoint hit for CRN: $crn and document ID: $documentId"))
    return documentService.getDocumentByCrnAndId(crn, documentId)
  }
}
