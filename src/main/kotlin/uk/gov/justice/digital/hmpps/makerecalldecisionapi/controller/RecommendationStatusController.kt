package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationStatusResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationStatusService
import java.security.Principal

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class RecommendationStatusController(
  private val recommendationStatusService: RecommendationStatusService,
  private val authenticationFacade: AuthenticationFacade
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION', 'ROLE_MAKE_RECALL_DECISION_SPO')")
  @GetMapping("/recommendations/{recommendationId}/statuses")
  @Operation(summary = "Gets recommendation statuses")
  suspend fun getRecommendationStatus(@PathVariable("recommendationId") recommendationId: Long): List<RecommendationStatusResponse> {
    log.info(normalizeSpace("Get recommendation statuses endpoint hit for recommendation id: $recommendationId"))
    return recommendationStatusService.fetchRecommendationStatuses(recommendationId)
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION', 'ROLE_MAKE_RECALL_DECISION_SPO')")
  @PatchMapping("/recommendations/{recommendationId}/status")
  @Operation(summary = "Creates or updates a recommendation status")
  suspend fun createOrUpdateRecommendationStatus(
    @PathVariable("recommendationId") recommendationId: Long,
    @RequestBody request: RecommendationStatusRequest,
    userLogin: Principal
  ): ResponseEntity<List<RecommendationStatusResponse>> {
    log.info(normalizeSpace("Create or update a recommendation status endpoint for recommendation id: $recommendationId"))
    val userId = userLogin.name
    val readableUserName = authenticationFacade.currentNameOfUser
    return ResponseEntity(
      recommendationStatusService.updateRecommendationStatus(
        recommendationStatusRequest = request,
        userId = userId,
        readableNameOfUser = readableUserName,
        recommendationId = recommendationId
      ),
      OK
    )
  }
}
