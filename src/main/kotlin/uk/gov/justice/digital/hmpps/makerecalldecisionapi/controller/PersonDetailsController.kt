package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.PersonDetailsService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class PersonDetailsController(
  private val personDetailsService: PersonDetailsService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/personal-details")
  @Operation(summary = "WIP: Returns an overview of the person details")
  fun personDetails(@PathVariable("crn") crn: String): PersonDetailsResponse {
    log.info("Person details endpoint hit for CRN: $crn")
    return personDetailsService.getPersonDetails(crn)
  }
}
