package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Crn
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.FullName

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class OverviewController {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/overview")
  @Operation(summary = "WIP: Returns an overview of the case details")
  fun overview(@PathVariable("crn") crn: Crn): String {
    log.info("Overview endpoint hit for CRN: $crn")
    return overviewResponse()
  }
}

data class OverviewResponse(
  val name: FullName
)

fun overviewResponse() = """
{
  "personDetails": {
    "name": "Paula Smith",
    "dateOfBirth": "2000-11-09",
    "age": 21,
    "gender": "Male",
    "crn": "A12345"
  },
  "currentAddress": {
    "line1": "5 Anderton Road",
    "line2": "Newham",
    "town": "London",
    "postcode": "E15 1UJ"
  },
  "offenderManager": {
    "name": "Jenny Eclair",
    "phoneNumber": "07824637629",
    "email": "jenny@probation.com",
    "probationTeam": {
      "code": "N07",
      "label": "NPS London"
    }
  },
  "risk": {
    "flags": ["Victim contact", "Mental health issues", "MAPPA"]
  },
  "offences": [
    {
      "mainOffence": true,
      "description": "Robbery (other than armed robbery)"
    },
    {
      "mainOffence": false,
      "description": "Shoplifting"
    }
  ]
}
""".trimIndent()
