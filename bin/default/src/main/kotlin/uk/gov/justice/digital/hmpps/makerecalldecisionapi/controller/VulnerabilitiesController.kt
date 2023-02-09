package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.VulnerabilitiesResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.VulnerabilitiesService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class VulnerabilitiesController(
  private val vulnerabilitiesService: VulnerabilitiesService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/vulnerabilities")
  @Operation(summary = "Returns case risk vulnerabilities")
  suspend fun risk(@PathVariable("crn") crn: String): VulnerabilitiesResponse {
    log.info("Vulnerabilities endpoint hit for CRN: $crn")
    return vulnerabilitiesService.getVulnerabilities(crn)
  }
}
