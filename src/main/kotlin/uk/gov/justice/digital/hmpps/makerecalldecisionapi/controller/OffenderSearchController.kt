package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SearchByCrnResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.OffenderSearchService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.LogHelper.Helper.redact

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class OffenderSearchController(
  private val offenderSearchService: OffenderSearchService
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/search")
  @Operation(summary = "Returns a list of people on probation based on a given CRN or name")
  suspend fun search(
    @RequestParam(required = false) crn: String,
    @RequestParam(required = false) firstName: String,
    @RequestParam(required = false) lastName: String
  ): List<SearchByCrnResponse> {
    log.info(normalizeSpace("Offender search endpoint hit for CRN: '$crn', FirstName: '${redact(firstName)}', LastName: '${redact(lastName)}'}"))
    return offenderSearchService.search(crn)
  }
}
