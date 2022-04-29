package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Crn
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.FullName

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class OverviewController {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasAuthority('MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/overview")
  @Operation(summary = "WIP: Returns an overview of the case details")
  fun overview(@PathVariable("crn") crn: Crn): OverviewResponse {
    log.info("Overview endpoint hit for CRN: $crn")
    return OverviewResponse(FullName("Johnny Test"))
  }
}

data class OverviewResponse(
  val name: FullName
)
