package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateSupportingDocumentRequest
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
  suspend fun getSupportingDocuments(@PathVariable("recommendationId") recommendationId: Long): List<SupportingDocumentResponse> {
    return supportingDocumentService.fetchSupportingDocuments(recommendationId)
  }

  @PreAuthorize("hasAnyRole('ROLE_MAKE_RECALL_DECISION_PPCS')")
  @PostMapping("/recommendations/{recommendationId}/documents")
  @Operation(summary = "Gets recommendation supporting documents")
  suspend fun postSupportingDocument(
    @PathVariable("recommendationId") recommendationId: Long,
    @RequestBody createSupportingDocumentRequest: CreateSupportingDocumentRequest,
    @RequestHeader("X-Feature-Flags") featureFlags: String?,
    userLogin: Principal,
  ) {
    val flags: FeatureFlags = setFeatureFlags(featureFlags)
    val createdBy = userLogin.name
    val createdByFullName = authenticationFacade.currentNameOfUser
    supportingDocumentService.uploadNewSupportingDocument(
      recommendationId,
      createSupportingDocumentRequest.type,
      createSupportingDocumentRequest.mimetype,
      createSupportingDocumentRequest.filename,
      DateTimeHelper.utcNowDateTimeString(),
      createdBy,
      createdByFullName,
      createSupportingDocumentRequest.data,
      flags,
    )
  }
}
