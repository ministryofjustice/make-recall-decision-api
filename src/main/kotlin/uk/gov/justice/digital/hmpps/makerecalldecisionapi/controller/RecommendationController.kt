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
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreatePartARequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentRequestQuery
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentRequestType.Companion.fromString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.setFeatureFlags
import java.security.Principal

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class RecommendationController(
  private val recommendationService: RecommendationService,
  private val authenticationFacade: AuthenticationFacade
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/recommendations")
  @Operation(summary = "Creates a recommendation")
  suspend fun recommendation(
    @RequestBody recommendationRequest: CreateRecommendationRequest,
    @RequestHeader("X-Feature-Flags") featureFlags: String?,
    userLogin: Principal
  ): ResponseEntity<RecommendationResponse?> {
    log.info(normalizeSpace("Create recommendation details endpoint hit for CRN: ${recommendationRequest.crn}"))
    val flags: FeatureFlags? = setFeatureFlags(featureFlags)
    val username = userLogin.name
    val readableUserName = authenticationFacade.currentNameOfUser
    val responseEntity = try {
      ResponseEntity(recommendationService.createRecommendation(recommendationRequest, username, readableUserName, flags), CREATED)
    } catch (e: UserAccessException) {
      ResponseEntity(RecommendationResponse(Gson().fromJson(e.message, UserAccessResponse::class.java)), FORBIDDEN)
    }
    return responseEntity
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/recommendations")
  @Operation(summary = "Gets all recommendations for case")
  suspend fun getRecommendations(@PathVariable("crn") crn: String): RecommendationsResponse {
    log.info(normalizeSpace("Get all recommendations for CRN endpoint hit for crn: $crn"))
    return recommendationService.getRecommendations(crn)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/recommendations/{recommendationId}")
  @Operation(summary = "Gets a recommendation")
  suspend fun getRecommendation(@PathVariable("recommendationId") recommendationId: Long): RecommendationResponse {
    log.info(normalizeSpace("Get recommendation details endpoint hit for recommendation id: $recommendationId"))
    return recommendationService.getRecommendation(recommendationId)
  }

  @PatchMapping("/recommendations/{recommendationId}/manager-recall-decision")
  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION_SPO')")
  @Operation(summary = "Updates recommendation with manager recall decision")
  suspend fun updateRecommendationWithManagerRecallDecision(
    @PathVariable("recommendationId") recommendationId: Long,
    @RequestBody updateRecommendationJson: JsonNode,
    userLogin: Principal
  ): RecommendationResponse {
    log.info(normalizeSpace("Update recommendation with manager recall decision endpoint for recommendation id: $recommendationId"))
    val username = userLogin.name
    val readableUserName = authenticationFacade.currentNameOfUser
    return recommendationService.updateRecommendationWithManagerRecallDecision(updateRecommendationJson, recommendationId, username, readableUserName)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PatchMapping("/recommendations/{recommendationId}")
  @Operation(summary = "Updates a recommendation")
  suspend fun updateRecommendation(
    @PathVariable("recommendationId") recommendationId: Long,
    @RequestBody updateRecommendationJson: JsonNode,
    userLogin: Principal,
    @RequestParam("refreshProperty") refreshProperty: List<String>?,
    @RequestHeader("X-Feature-Flags") featureFlags: String?
  ): RecommendationResponse {
    log.info(normalizeSpace("Update recommendation details endpoint for recommendation id: $recommendationId"))
    val flags: FeatureFlags? = setFeatureFlags(featureFlags)
    val username = userLogin.name
    val readableUserName = authenticationFacade.currentNameOfUser
    return recommendationService.updateRecommendation(updateRecommendationJson, recommendationId, username, readableUserName, null, false, false, refreshProperty, flags)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/recommendations/{recommendationId}/part-a")
  @Operation(summary = "Generates a Part A document")
  suspend fun generatePartADocument(
    @RequestHeader("X-Feature-Flags") featureFlags: String?,
    @PathVariable("recommendationId") recommendationId: Long,
    @RequestBody createPartARequest: CreatePartARequest,
    userLogin: Principal
  ): ResponseEntity<DocumentResponse> {
    log.info(normalizeSpace("Generate Part A document endpoint for recommendation id: $recommendationId"))
    val responseEntity = try {
      ResponseEntity(recommendationService.generatePartA(recommendationId, userLogin.name, authenticationFacade.currentNameOfUser, createPartARequest.userEmail), OK)
    } catch (e: UserAccessException) {
      ResponseEntity(DocumentResponse(Gson().fromJson(e.message, UserAccessResponse::class.java)), FORBIDDEN)
    }
    return responseEntity
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/recommendations/{recommendationId}/no-recall-letter")
  @Operation(
    summary = "Generates a Decision not to Recall document",
    description = "This endpoint will either generate a preview of the Decision not to Recall letter or provide the letter ready for download. " +
      "To preview, set the 'format' field to 'preview'. To download, set the 'format' field to 'download-docx'"
  )
  suspend fun generateDntrDocument(
    @PathVariable("recommendationId") recommendationId: Long,
    @RequestBody documentRequestQuery: DocumentRequestQuery,
    @RequestHeader("X-Feature-Flags") featureFlags: String?,
    userLogin: Principal
  ): ResponseEntity<DocumentResponse> {
    log.info(normalizeSpace("Generate DNTR document endpoint for recommendation id: $recommendationId"))
    val flags: FeatureFlags? = setFeatureFlags(featureFlags)
    val responseEntity = try {
      ResponseEntity(recommendationService.generateDntr(recommendationId, userLogin.name, authenticationFacade.currentNameOfUser, fromString(documentRequestQuery.format), flags), OK)
    } catch (e: UserAccessException) {
      ResponseEntity(DocumentResponse(Gson().fromJson(e.message, UserAccessResponse::class.java)), FORBIDDEN)
    }
    return responseEntity
  }
}
