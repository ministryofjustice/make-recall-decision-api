package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateMinuteRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateSentenceResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudReferenceListResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.UploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.PpudService
import java.security.Principal

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class PpudController(
  private val ppudService: PpudService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/search")
  @Operation(summary = "Calls PPUD Automation service for a search.")
  suspend fun ppudSearch(
    @RequestBody request: PpudSearchRequest,
  ): PpudSearchResponse = ppudService.search(request)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/details/{id}")
  @Operation(summary = "Calls PPUD Automation service for a search.")
  suspend fun ppcsDetails(
    @PathVariable("id") id: String,
  ): PpudDetailsResponse = ppudService.details(id)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/reference/{name}")
  @Operation(summary = "Calls PPUD Automation service to retrieve reference list.")
  suspend fun retrieveList(
    @PathVariable("name") name: String,
  ): PpudReferenceListResponse = ppudService.retrieveList(name)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/offender")
  @Operation(summary = "Calls PPUD Automation service to create an offender.")
  suspend fun createOffender(
    @RequestBody request: PpudCreateOffenderRequest,
  ): PpudCreateOffenderResponse = ppudService.createOffender(request)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PutMapping("/ppud/offender/{offenderId}")
  @Operation(summary = "Calls PPUD Automation service to update an offender.")
  suspend fun updateOffender(
    @PathVariable(required = true) offenderId: String,
    @RequestBody request: PpudUpdateOffenderRequest,
  ) {
    ppudService.updateOffender(offenderId, request)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/offender/{offenderId}/sentence")
  @Operation(summary = "Calls PPUD Automation service to update a sentence.")
  suspend fun createSentence(
    @PathVariable(required = true) offenderId: String,
    @RequestBody request: PpudCreateOrUpdateSentenceRequest,
  ): PpudCreateSentenceResponse = ppudService.createSentence(offenderId, request)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PutMapping("/ppud/offender/{offenderId}/sentence/{sentenceId}")
  @Operation(summary = "Calls PPUD Automation service to update a sentence.")
  suspend fun updateSentence(
    @PathVariable(required = true) offenderId: String,
    @PathVariable(required = true) sentenceId: String,
    @RequestBody request: PpudCreateOrUpdateSentenceRequest,
  ) {
    ppudService.updateSentence(offenderId, sentenceId, request)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PutMapping("/ppud/offender/{offenderId}/sentence/{sentenceId}/offence")
  @Operation(summary = "Calls PPUD Automation service to update an offence.")
  suspend fun updateOffence(
    @PathVariable(required = true) offenderId: String,
    @PathVariable(required = true) sentenceId: String,
    @RequestBody request: PpudUpdateOffenceRequest,
  ) {
    ppudService.updateOffence(offenderId, sentenceId, request)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/offender/{offenderId}/sentence/{sentenceId}/release")
  @Operation(summary = "Calls PPUD Automation service to update a release.")
  suspend fun createOrUpdateRelease(
    @PathVariable(required = true) offenderId: String,
    @PathVariable(required = true) sentenceId: String,
    @RequestBody(required = true)
    createOrUpdateReleaseRequest: PpudCreateOrUpdateReleaseRequest,
  ): PpudCreateOrUpdateReleaseResponse = ppudService.createOrUpdateRelease(offenderId, sentenceId, createOrUpdateReleaseRequest)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("ppud/offender/{offenderId}/release/{releaseId}/recall")
  @Operation(summary = "Calls PPUD Automation service to create a recall.")
  suspend fun createRecall(
    @PathVariable(required = true) offenderId: String,
    @PathVariable(required = true) releaseId: String,
    @RequestBody(required = true)
    createRecallRequest: CreateRecallRequest,
    userLogin: Principal,
  ): PpudCreateRecallResponse = ppudService.createRecall(offenderId, releaseId, createRecallRequest, userLogin.name)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PutMapping("ppud/recall/{recallId}/upload-mandatory-document")
  @Operation(summary = "Calls PPUD Automation service to upload a mandatory file to ppud.")
  suspend fun uploadMandatoryDocument(
    @PathVariable(required = true) recallId: String,
    @RequestBody(required = true)
    uploadMandatoryDocumentRequest: UploadMandatoryDocumentRequest,
    userLogin: Principal,
  ) {
    ppudService.uploadMandatoryDocument(recallId, uploadMandatoryDocumentRequest, userLogin.name)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PutMapping("ppud/recall/{recallId}/upload-additional-document")
  @Operation(summary = "Calls PPUD Automation service to upload a additional file to ppud.")
  suspend fun uploadAdditionalDocument(
    @PathVariable(required = true) recallId: String,
    @RequestBody(required = true)
    uploadAdditionalDocumentRequest: UploadAdditionalDocumentRequest,
    userLogin: Principal,
  ) {
    ppudService.uploadAdditionalDocument(recallId, uploadAdditionalDocumentRequest, userLogin.name)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PutMapping("ppud/recall/{recallId}/minutes")
  @Operation(summary = "Calls PPUD Automation service to create a minute.")
  suspend fun createMinute(
    @PathVariable(required = true) recallId: String,
    @RequestBody(required = true)
    createMinuteRequest: CreateMinuteRequest,
    userLogin: Principal,
  ) {
    ppudService.createMinute(recallId, createMinuteRequest, userLogin.name)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/ppud/user/search")
  @Operation(summary = "Calls PPUD Automation service to search for active users.")
  suspend fun searchActiveUsers(
    @RequestBody request: PpudUserSearchRequest,
  ): PpudUserResponse = ppudService.searchActiveUsers(request)
}
