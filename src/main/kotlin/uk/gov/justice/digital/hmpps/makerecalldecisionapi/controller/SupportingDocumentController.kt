package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateSupportingDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ReplaceSupportingDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SupportingDocumentMetaDataResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SupportingDocumentResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.SupportingDocumentService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.setFeatureFlags
import java.security.Principal

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class SupportingDocumentController(
  private val supportingDocumentService: SupportingDocumentService,
  private val authenticationFacade: AuthenticationFacade,
) {
  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION_PPCS')")
  @GetMapping("/recommendations/{recommendationId}/documents")
  @Operation(summary = "Gets recommendation supporting documents")
  suspend fun getSupportingDocuments(@PathVariable("recommendationId") recommendationId: Long): List<SupportingDocumentMetaDataResponse> {
    return supportingDocumentService.fetchSupportingDocuments(recommendationId)
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION_PPCS')")
  @PostMapping("/recommendations/{recommendationId}/documents")
  @Operation(summary = "Uploads supporting documents")
  suspend fun createSupportingDocument(
    @PathVariable("recommendationId") recommendationId: Long,
    @RequestBody createSupportingDocumentRequest: CreateSupportingDocumentRequest,
    @RequestHeader("X-Feature-Flags") featureFlags: String?,
    userLogin: Principal,
  ): SupportingDocumentResponse {
    val flags: FeatureFlags = setFeatureFlags(featureFlags)
    val createdBy = userLogin.name
    val createdByFullName = authenticationFacade.currentNameOfUser
    val id = supportingDocumentService.uploadNewSupportingDocument(
      recommendationId = recommendationId,
      type = createSupportingDocumentRequest.type,
      title = createSupportingDocumentRequest.title,
      mimetype = createSupportingDocumentRequest.mimetype,
      filename = createSupportingDocumentRequest.filename,
      created = DateTimeHelper.utcNowDateTimeString(),
      createdBy = createdBy,
      createdByUserFullName = createdByFullName,
      data = createSupportingDocumentRequest.data,
      flags = flags,
    )

    return SupportingDocumentResponse(
      id = id,
      filename = createSupportingDocumentRequest.filename,
      type = createSupportingDocumentRequest.type,
      recommendationId = recommendationId,
      data = createSupportingDocumentRequest.data,
    )
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION_PPCS')")
  @GetMapping("/recommendations/{recommendationId}/documents/{id}")
  @Operation(summary = "Get recommendation supporting document")
  suspend fun getSupportingDocument(
    @PathVariable("recommendationId") recommendationId: Long,
    @PathVariable("id") id: Long,
    @RequestHeader("X-Feature-Flags") featureFlags: String?,
    userLogin: Principal,
  ): SupportingDocumentResponse {
    val flags: FeatureFlags = setFeatureFlags(featureFlags)
    return supportingDocumentService.getSupportingDocument(id, flags)
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION_PPCS')")
  @PatchMapping("/recommendations/{recommendationId}/documents/{id}")
  @Operation(summary = "Updates supporting document")
  suspend fun updateSupportingDocument(
    @PathVariable("recommendationId") recommendationId: Long,
    @PathVariable("id") id: Long,
    @RequestBody request: ReplaceSupportingDocumentRequest,
    @RequestHeader("X-Feature-Flags") featureFlags: String?,
    userLogin: Principal,
  ) {
    val flags: FeatureFlags = setFeatureFlags(featureFlags)
    val uploadedBy = userLogin.name
    val uploadedByUserFullName = authenticationFacade.currentNameOfUser

    supportingDocumentService.replaceSupportingDocument(
      id = id,
      title = request.title,
      mimetype = request.mimetype,
      filename = request.filename,
      uploaded = DateTimeHelper.utcNowDateTimeString(),
      uploadedBy = uploadedBy,
      uploadedByUserFullName = uploadedByUserFullName,
      data = request.data,
      flags = flags,
    )
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION_PPCS')")
  @DeleteMapping("/recommendations/{recommendationId}/documents/{id}")
  @Operation(summary = "Deletes supporting document")
  suspend fun deleteSupportingDocument(
    @PathVariable("recommendationId") recommendationId: Long,
    @PathVariable("id") id: Long,
    @RequestHeader("X-Feature-Flags") featureFlags: String?,
  ) {
    val flags: FeatureFlags = setFeatureFlags(featureFlags)

    supportingDocumentService.removeSupportingDocument(id = id, flags = flags)
  }
}
