package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudBookRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudReferenceListResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.PpudService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class PpudController(
  private val ppudService: PpudService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/search")
  @Operation(summary = "Calls PPUD Automation service for a search.")
  suspend fun ppudSearch(
    @RequestBody request: PpudSearchRequest,
  ): PpudSearchResponse {
    return ppudService.search(request)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/details/{id}")
  @Operation(summary = "Calls PPUD Automation service for a search.")
  suspend fun ppcsDetails(
    @PathVariable("id") id: String,
  ): PpudDetailsResponse {
    return ppudService.details(id)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/book-recall/{nomisId}")
  @Operation(summary = "Calls PPUD Automation service to book recall.")
  suspend fun createOffender(
    @PathVariable("nomisId") nomisId: String,
    @RequestBody request: PpudBookRecall,
  ): PpudBookRecallResponse {
    return ppudService.bookToPpud(nomisId, request)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/reference/{name}")
  @Operation(summary = "Calls PPUD Automation service to retrieve reference list.")
  suspend fun retrieveList(
    @PathVariable("name") name: String,
  ): PpudReferenceListResponse {
    return ppudService.retrieveList(name)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/offender")
  @Operation(summary = "Calls PPUD Automation service to book offender.")
  suspend fun createOffender(
    @RequestBody request: PpudCreateOffender,
  ): PpudCreateOffenderResponse {
    return ppudService.createOffender(request)
  }
}
