package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMapping
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMappingResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMappingSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.PpudUserMappingEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.PpudUserMappingService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class PpudUserMappingController(
  private val ppudUserMappingService: PpudUserMappingService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/user-mapping/search")
  @Operation(summary = "Calls Ppud User Mapping Service to search for mapped users.")
  suspend fun searchMappedUsers(
    @RequestBody request: PpudUserMappingSearchRequest,
  ): ResponseEntity<PpudUserMappingResponse> {
    log.info(normalizeSpace("Search PPUD user mapping endpoint hit for userName: ${request.userName}"))
    val response = ppudUserMappingService.findByUserNameIgnoreCase(request.userName)
    val ppudMapping = response?.let { PpudUserMapping(it) }
    return ResponseEntity(PpudUserMappingResponse(ppudMapping), HttpStatus.OK)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/user-mapping")
  @Operation(summary = "Gets all PPUD user mappings.")
  suspend fun getAllUserMappings(): ResponseEntity<List<PpudUserMappingEntity>> {
    log.info(normalizeSpace("Get all PPUD user mappings endpoint hit"))
    return ResponseEntity(ppudUserMappingService.getAllUserMappings(), HttpStatus.OK)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/user-mapping/{id}")
  @Operation(summary = "Gets a PPUD user mapping by ID.")
  suspend fun getUserMappingById(@PathVariable(required = true) id: Long): ResponseEntity<PpudUserMappingEntity> {
    log.info(normalizeSpace("Get PPUD user mapping endpoint hit for id: $id"))
    val response = ppudUserMappingService.findById(id)
    return if (response != null) {
      ResponseEntity(response, HttpStatus.OK)
    } else {
      ResponseEntity(HttpStatus.NOT_FOUND)
    }
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/user-mapping")
  @Operation(summary = "Creates a PPUD user mapping.")
  suspend fun createUserMapping(
    @RequestBody(required = true) ppudUserMappingEntity: PpudUserMappingEntity,
  ): ResponseEntity<PpudUserMappingEntity> =
    ResponseEntity(ppudUserMappingService.saveUserMapping(ppudUserMappingEntity), HttpStatus.CREATED)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PutMapping("/user-mapping/{id}")
  @Operation(summary = "Updates a PPUD user mapping.")
  suspend fun updateUserMapping(
    @PathVariable(required = true) id: String,
    @RequestBody(required = true) ppudUserMappingEntity: PpudUserMappingEntity,
  ): ResponseEntity<PpudUserMappingEntity> =
    ResponseEntity(ppudUserMappingService.updateUserMapping(id.toLong(), ppudUserMappingEntity), HttpStatus.OK)

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @DeleteMapping("/user-mapping/{id}")
  @Operation(summary = "Deletes a PPUD user mapping.")
  suspend fun deleteUserMapping(
    @PathVariable(required = true) id: String,
  ): ResponseEntity<Unit> {
    ppudUserMappingService.deleteUserMapping(id.toLong())
    return ResponseEntity(HttpStatus.NO_CONTENT)
  }
}
