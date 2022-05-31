package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.CaseSummaryOverviewService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class OverviewController(
  private val caseSummaryOverviewService: CaseSummaryOverviewService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION', 'ROLE_ASSESS_RISK_AND_NEEDS)')")
  @GetMapping("/cases/{crn}/overview")
  @Operation(summary = "WIP: Returns an overview of the case details")
  suspend fun overview(@PathVariable("crn") crn: String): CaseSummaryOverviewResponse {
    log.info("Overview endpoint hit for CRN: $crn")
    return caseSummaryOverviewService.getOverview(crn)
  }
}
