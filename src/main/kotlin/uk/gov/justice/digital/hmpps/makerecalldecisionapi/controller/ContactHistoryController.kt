package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
// import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.ContactHistoryService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class ContactHistoryController(
  private val contactHistoryService: ContactHistoryService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

//  @PreAuthorize("hasRole('ROLE_PROBATION')")
//  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/contact-history")
  @Operation(summary = "Returns all details of a case contact history")
  suspend fun allContactHistory(@PathVariable("crn") crn: String): ContactHistoryResponse {
    log.info(normalizeSpace("All contact history endpoint hit for CRN: $crn"))
    return contactHistoryService.getContactHistory(crn)
  }
}
