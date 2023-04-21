package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ManagementOversightResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.ManagementOversightService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class ManagementOversightController(
  private val managementOversightService: ManagementOversightService,
  private val authenticationFacade: AuthenticationFacade
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION', 'ROLE_MAKE_RECALL_DECISION_SPO')")
  @GetMapping("/managementOversight/{crn}")
  @Operation(summary = "Provides notes on case for Delius")
  suspend fun getRecommendationStatus(
    @PathVariable("crn") crn: String,
  ): ResponseEntity<ManagementOversightResponse> {
    log.info(normalizeSpace("Management oversight endpoint hit for crn: $crn"))
    return managementOversightService.getManagementOversightResponse(
      crn = crn
    )
  }
}
