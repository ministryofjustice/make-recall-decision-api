package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import com.fasterxml.jackson.databind.JsonNode
import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationService
import java.security.Principal

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
  @Operation(summary = "Creates a recommendation")
  suspend fun recommendation(
    @RequestBody recommendationRequest: CreateRecommendationRequest,
    userLogin: Principal
  ): ResponseEntity<RecommendationResponse>? {
    log.info(normalizeSpace("Create recommendation details endpoint hit for CRN: ${recommendationRequest.crn}"))
    val username = userLogin.name
    val responseEntity = try {
      ResponseEntity(recommendationService.createRecommendation(recommendationRequest, username), CREATED)
    } catch (e: UserAccessException) {
      ResponseEntity(RecommendationResponse(Gson().fromJson(e.message, UserAccessResponse::class.java)), FORBIDDEN)
    }
    return responseEntity
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/recommendations/{recommendationId}")
  @Operation(summary = "Gets a recommendation")
  suspend fun getRecommendation(@PathVariable("recommendationId") recommendationId: Long): RecommendationResponse {
    log.info(normalizeSpace("Get recommendation details endpoint hit for recommendation id: $recommendationId"))
    return recommendationService.getRecommendation(recommendationId)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PatchMapping("/recommendations/{recommendationId}")
  @Operation(summary = "WIP: Updates a recommendation")
  suspend fun updateRecommendation(
    @PathVariable("recommendationId") recommendationId: Long,
    @RequestBody updateRecommendationJson: JsonNode,
    userLogin: Principal
  ): ResponseEntity<Unit> {
    log.info(normalizeSpace("Update recommendation details endpoint for recommendation id: $recommendationId"))
    val username = userLogin.name
    recommendationService.updateRecommendation(updateRecommendationJson, recommendationId, username)
    return ResponseEntity<Unit>(OK)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/recommendations/{recommendationId}/part-a")
  @Operation(summary = "WIP: Generates a Part A document")
  suspend fun generatePartADocument(
    @PathVariable("recommendationId") recommendationId: Long,
  ): ResponseEntity<PartAResponse> {
    log.info(normalizeSpace("Generate Part A document endpoint for recommendation id: $recommendationId"))
    val responseEntity = try {
      ResponseEntity(recommendationService.generatePartA(recommendationId), OK)
    } catch (e: UserAccessException) {
      ResponseEntity(PartAResponse(Gson().fromJson(e.message, UserAccessResponse::class.java)), FORBIDDEN)
    }
    return responseEntity
  }
}
