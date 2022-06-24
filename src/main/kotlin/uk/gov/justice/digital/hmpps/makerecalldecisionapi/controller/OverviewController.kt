package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
// import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.AuditService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.AuditType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.CaseSummaryOverviewService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class OverviewController(
  private val caseSummaryOverviewService: CaseSummaryOverviewService,
) {
  @Autowired
  private lateinit var auditService: AuditService

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

//  @PreAuthorize("hasRole('ROLE_PROBATION')")
//  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/overview")
  @Operation(summary = "WIP: Returns an overview of the case details")
  suspend fun overview(@PathVariable("crn") crn: String): CaseSummaryOverviewResponse {
    log.info(normalizeSpace("Overview tab viewed for CRN: $crn"))
    auditService.sendMessage(AuditType.MRD_CASE_OVERVIEW_VIEWED, "Overview tab viewed for CRN: $crn", crn)
    return caseSummaryOverviewService.getOverview(crn)
  }
}
