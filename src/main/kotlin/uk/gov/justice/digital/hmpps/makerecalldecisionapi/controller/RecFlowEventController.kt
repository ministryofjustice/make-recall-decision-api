package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecFlowEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecFlowEventService
import java.security.Principal

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class RecFlowEventController(
  val recFlowEventService: RecFlowEventService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @PostMapping("/recommendations/events")
  @Operation(summary = "Creates a recommendation flow event")
  suspend fun recommendation(
    @RequestBody recFlowEvent: RecFlowEvent,
    userLogin: Principal
  ): ResponseEntity<RecFlowEvent>? {
    log.info(normalizeSpace("Create recommendation flow event endpoint hit for CRN: ${recFlowEvent.crn}"))
    return ResponseEntity(recFlowEventService.createRecFlowEvent(recFlowEvent), CREATED)
  }
}
