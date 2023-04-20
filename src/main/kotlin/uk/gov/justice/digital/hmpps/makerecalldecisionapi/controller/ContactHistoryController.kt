package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ContactHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.ContactHistoryService
import java.time.LocalDate

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class ContactHistoryController(
  private val contactHistoryService: ContactHistoryService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/contact-history")
  @Operation(summary = "Returns all details of a case contact history")
  suspend fun allContactHistory(
    @PathVariable("crn") crn: String,
    @Parameter(description = "Search for contacts that contain the provided text. This currently performs a simple substring match against the notes.")
    @RequestParam(required = false)
    query: String?,
    @Parameter(description = "Return only contacts that start after this date")
    @RequestParam(required = false)
    from: LocalDate?,
    @Parameter(description = "Return only contacts that start before this date. Defaults to the current date. If provided, this value must be on or before the current date.")
    @RequestParam(required = false)
    to: LocalDate?,
    @Parameter(description = "Filter on contact type codes")
    @RequestParam(defaultValue = "")
    type: List<String> = listOf(),
    @Parameter(description = "Include (true) or exclude (false) any contact types that are system-generated")
    @RequestParam(defaultValue = "true")
    includeSystemGenerated: Boolean = true
  ): ContactHistoryResponse {
    log.info(normalizeSpace("All contact history endpoint hit for CRN: $crn"))
    return contactHistoryService.getContactHistory(crn)
  }
}
