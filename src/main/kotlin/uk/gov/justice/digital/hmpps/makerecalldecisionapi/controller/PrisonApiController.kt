package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.PrisonerApiService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class PrisonApiController(
  private val prisonerApiService: PrisonerApiService,
) {

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/prison-offender-search")
  @Operation(summary = "Returns a list of recommendation docs that are appropriate for ppcs to process.")
  suspend fun prisonOffenderSearch(
    @RequestBody request: PrisonOffenderSearchRequest,
  ): PrisonOffenderSearchResponse {
    return prisonerApiService.searchPrisonApi(request.nomsId)
  }
}
