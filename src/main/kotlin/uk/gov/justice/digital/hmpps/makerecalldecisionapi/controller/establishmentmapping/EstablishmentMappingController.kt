package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller.establishmentmapping

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.establishmentmapping.EstablishmentMappingService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class EstablishmentMappingController(
  private val establishmentMappingService: EstablishmentMappingService,
) {

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION_PPCS')")
  @GetMapping("/establishment-mappings")
  @Operation(summary = "Gets all NOMIS to PPUD establishment mappings")
  suspend fun getEstablishmentMappings(): ResponseEntity<Map<String, String>> {
    val mappings = establishmentMappingService.getEstablishmentMappings()
    return ResponseEntity(mappings, HttpStatus.OK)
  }
}
