package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationFileRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationFileResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationFileService
import java.security.Principal


@RestController
internal class RecommendationFileController(
  private val recommendationFileService: RecommendationFileService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION', 'ROLE_MAKE_RECALL_DECISION_SPO')")
  @GetMapping("/recommendations/{recommendationId}/files")
  @Operation(summary = "Gets recommendation files")
  suspend fun getRecommendationStatus(@PathVariable("recommendationId") recommendationId: Long): List<RecommendationFileResponse> {

    log.info(StringUtils.normalizeSpace("Get recommendation files endpoint hit for recommendation id: $recommendationId"))

    return recommendationFileService.fetchRecommendationFiles(recommendationId)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/recommendations/{recommendationId}/files")
  @Operation(summary = "Creates a file")
  suspend fun recommendation(
    @PathVariable("recommendationId") recommendationId: Long,
    @RequestBody recommendationFileRequest: CreateRecommendationFileRequest,
    @RequestHeader("X-Feature-Flags") featureFlags: String?,
    userLogin: Principal
  ): ResponseEntity<RecommendationFileResponse> {

    log.info(StringUtils.normalizeSpace("Create recommendation file for category: ${recommendationId} - ${recommendationFileRequest.category}"))

    return ResponseEntity(
      recommendationFileService.create(recommendationId, category = recommendationFileRequest.category),
      HttpStatus.CREATED
    )
  }
}