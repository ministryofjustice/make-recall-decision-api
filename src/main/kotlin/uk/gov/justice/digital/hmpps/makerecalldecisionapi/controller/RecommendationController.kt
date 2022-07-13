package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class RecommendationController(
  private val recommendationService: RecommendationService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/recommendations")
  @Operation(summary = "WIP: Creates a recommendation")
  suspend fun recommendation(
    @RequestBody recommendationRequest: CreateRecommendationRequest
  ): ResponseEntity<CreateRecommendationResponse>? {
    log.info(normalizeSpace("Recommendation details endpoint hit for CRN: ${recommendationRequest.crn}"))
    val responseBody = recommendationService.createRecommendation(recommendationRequest)
    return ResponseEntity<CreateRecommendationResponse>(responseBody, HttpStatus.CREATED)
  }
}
