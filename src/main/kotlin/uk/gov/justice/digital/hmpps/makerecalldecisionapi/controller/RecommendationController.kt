package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
//import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class RecommendationController(
  private val recommendationService: RecommendationService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

//  @PreAuthorize("hasRole('ROLE_PROBATION')")
//  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/cases/{crn}/recommendation")
  @Operation(summary = "WIP: Creates a recommendation")
  suspend fun recommendation(
    @PathVariable("crn") crn: String,
    @RequestBody recommendationRequest: RecommendationRequest
  ): RecommendationResponse {
    log.info(normalizeSpace("Recommendation details endpoint hit for CRN: $crn"))
    return recommendationService.createRecommendation(crn, recommendationRequest)
  }
}
