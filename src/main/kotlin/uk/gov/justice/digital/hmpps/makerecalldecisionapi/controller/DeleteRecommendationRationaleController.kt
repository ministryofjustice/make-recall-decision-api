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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DeleteRecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.DeleteRecommendationService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class DeleteRecommendationRationaleController(
  private val deleteRecommendationService: DeleteRecommendationService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/delete-recommendation-rationale/{crn}")
  @Operation(summary = "Provides notes on rationale for deleting recommendation for Delius")
  suspend fun getRecommendationStatus(
    @PathVariable("crn") crn: String,
  ): ResponseEntity<DeleteRecommendationResponse> {
    log.info(normalizeSpace("Delete recommendation rationale endpoint hit for crn: $crn"))
    return deleteRecommendationService.getDeleteRecommendationResponse(
      crn = crn,
    )
  }
}
