package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.LicenceHistoryService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class LicenceHistoryController(
  private val licenceHistoryService: LicenceHistoryService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

//  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/licence-history")
  @Operation(summary = "Returns filtered details of a case licence history")
  // FIXME: Potentially deprecated - remove once confirmed filters etc will be handled on frontend
  suspend fun licenseHistory(@PathVariable("crn") crn: String): LicenceHistoryResponse {
    log.info("Licence history endpoint hit for CRN: $crn")
    return licenceHistoryService.getLicenceHistory(crn, true)
  }

//  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/all-licence-history")
  @Operation(summary = "Returns all details of a case licence history")
  suspend fun allLicenseHistory(@PathVariable("crn") crn: String): LicenceHistoryResponse {
    log.info("All licence history endpoint hit for CRN: $crn")
    return licenceHistoryService.getLicenceHistory(crn, false)
  }
}
