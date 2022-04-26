package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
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
@PreAuthorize("hasRole('MAKE_RECALL_DECISION')")
class OverviewController(
) {

  @GetMapping("/cases/{crn}/overview")
  @Operation(summary = "WIP: Returns an overview of the case details")
  fun overview(@PathVariable("crn") crn: Crn): OverviewResponse {

    return OverviewResponse(FullName("Johnny Test"));
  }
}

data class OverviewResponse(
  val name: FullName
)
