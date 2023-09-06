package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SearchByCrnResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.OffenderSearchService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.LogHelper.Helper.redact

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class OffenderSearchController(
  private val offenderSearchService: OffenderSearchService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/search")
  @Operation(summary = "Returns a list of people on probation based on a given CRN")
  @Deprecated("This endpoint has been replaced by the /paged-search POST endpoint", level = DeprecationLevel.WARNING)
  suspend fun search(@RequestParam(required = false) crn: String): List<SearchByCrnResponse> {
    log.info(normalizeSpace("Offender search endpoint hit for CRN: $crn"))
    val response = offenderSearchService.search(crn = crn, page = 0, pageSize = 10)
    return response.results.map {
      SearchByCrnResponse(
        userExcluded = it.userExcluded,
        userRestricted = it.userRestricted,
        name = it.name,
        crn = it.crn,
        dateOfBirth = it.dateOfBirth,
      )
    }
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/paged-search")
  @Operation(summary = "Returns a list of people on probation based on a given CRN or name")
  suspend fun pagedSearch(
    @RequestParam(required = true) page: Int,
    @RequestParam(required = true) pageSize: Int,
    @RequestBody body: OffenderSearchRequest,
  ): OffenderSearchResponse {
    log.info(
      normalizeSpace(
        "Offender paged search endpoint hit for " +
          "CRN: '$body.crn', FirstName: '${redact(body.firstName)}', LastName: '${redact(body.lastName)}'}",
      ),
    )
    return offenderSearchService.search(body.crn, body.firstName, body.lastName, page = page, pageSize = pageSize)
  }
}
