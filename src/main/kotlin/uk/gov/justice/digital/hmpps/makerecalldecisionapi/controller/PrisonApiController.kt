package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonOffenderSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PrisonSentencesRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceSequence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.Offender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.prison.OffenderMovement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.prisonapi.PrisonerApiService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class PrisonApiController(
  private val prisonerApiService: PrisonerApiService,
) {

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/prison-offender-search")
  @Operation(summary = "Returns a list of prison offenders.")
  suspend fun prisonOffenderSearch(
    @RequestBody request: PrisonOffenderSearchRequest,
  ): Offender = prisonerApiService.searchPrisonApi(request.nomsId)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/prison-sentences")
  @Operation(summary = "Returns a list of prison sentences.")
  suspend fun retrieveSentences(
    @RequestBody request: PrisonSentencesRequest,
  ): List<SentenceSequence> = prisonerApiService.retrieveOffences(request.nomsId)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION_PPCS')")
  @GetMapping("/offenders/{nomisId}/movements")
  suspend fun getOffenderMovements(
    @PathVariable("nomisId") nomisId: String,
  ): List<OffenderMovement> = prisonerApiService.getOffenderMovements(nomisId)
}
