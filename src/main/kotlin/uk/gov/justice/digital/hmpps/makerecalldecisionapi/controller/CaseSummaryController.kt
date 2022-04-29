package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Crn
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.FullName
import javax.annotation.security.RolesAllowed

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class CaseSummaryController {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

//  @PreAuthorize("hasRole('MAKE_RECALL_DECISION')")//TODO correct role?
  @GetMapping("/cases/{crn}/search")
  @Operation(summary = "WIP: Returns an overview of the case details")
  fun overview(@PathVariable("crn") crn: Crn): String {
    log.info("Overview endpoint hit for CRN: $crn")
    return "hello"
  }
}


