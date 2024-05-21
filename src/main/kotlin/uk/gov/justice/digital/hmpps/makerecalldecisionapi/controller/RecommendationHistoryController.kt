package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationHistoryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationHistoryService
import java.time.LocalDate

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class RecommendationHistoryController(
  private val recommendationHistoryService: RecommendationHistoryService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')") // TODO correct, as this will be different
  @GetMapping("/history/crn/{crn}")
  @Operation(summary = "Gets latest complete recommendation overview for case")
  suspend fun getRecommendationHistoryForCrn(
    @PathVariable("crn") crn: String,
    @RequestParam("startDate") startDate: LocalDate,
    @RequestParam("endDate") endDate: LocalDate,
  ): RecommendationHistoryResponse {
    log.info(normalizeSpace("Getting recommendation history for crn: $crn from: $startDate to: $endDate"))
    return recommendationHistoryService.getRecommendationHistoryByCrn(crn, startDate, endDate)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')") // TODO correct as this will be different
  @GetMapping("/history/nomsId/{nomsId}")
  @Operation(summary = "Gets latest complete recommendation overview for case")
  suspend fun getRecommendationHistoryForNomsId(
    @PathVariable("nomsId") nomsId: String,
    @RequestParam("startDate") startDate: LocalDate,
    @RequestParam("endDate") endDate: LocalDate,
  ): RecommendationHistoryResponse {
    log.info(normalizeSpace("Getting recommendation history for nomsId: $nomsId from: $startDate to: $endDate"))
    return recommendationHistoryService.getRecommendationHistoryByNomsId(nomsId, startDate, endDate)
  }
}
