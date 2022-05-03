package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.SearchByCrnResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.CaseSummaryService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class CaseSummaryController(
  private val caseSummaryService: CaseSummaryService
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasAuthority('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/search")
  @Operation(summary = "WIP: Returns a simple overview of the case summary")
  suspend fun overview(@PathVariable("crn") crn: String): SearchByCrnResponse {
    log.info("Case summary search endpoint hit for CRN: $crn")
    return caseSummaryService.search(crn)
  }
}
