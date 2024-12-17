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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMappingResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserMappingSearchRequest
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
  @PostMapping("/user/search-mapped-users")
  @Operation(summary = "Calls Ppud User Mapping Service to search for mapped users.")
  suspend fun searchMappedUsers(
    @RequestBody request: PpudUserMappingSearchRequest,
  ): ResponseEntity<PpudUserMappingResponse> {
    log.info(normalizeSpace("Search PPUD user mapping endpoint hit for userName: ${request.userName}"))
    return ResponseEntity(PpudUserMappingResponse(ppudUserMappingService.findByUserNameIgnoreCase(request.userName)), HttpStatus.OK)
  }
}
