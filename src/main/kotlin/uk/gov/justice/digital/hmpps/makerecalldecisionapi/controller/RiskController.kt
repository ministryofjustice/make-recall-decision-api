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

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class RiskController {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/risk")
  @Operation(summary = "WIP: Returns case summary risk information")
  fun risk(@PathVariable("crn") crn: Crn): String {
    log.info("Risk endpoint hit for CRN: $crn")
    return riskResponse()
  }
}

fun riskResponse() = """
{
  "personDetails": {
    "name": "Paula Smith",
    "dateOfBirth": "2000-11-09",
    "age": 21,
    "gender": "Male",
    "crn": "A12345"
  },
  "risk": {
    "riskOfSeriousHarm": {
      "overallRisk": "VERY_HIGH",
      "riskToChildren": "LOW",
      "riskToPublic": "VERY_HIGH",
      "riskToKnownAdult": "MEDIUM",
      "riskToStaff": "HIGH",
      "lastUpdated": "2021-10-09"
    },
    "predictorScores": {
      "current": {
        "RSR": {
          "level": "HIGH",
          "score": 11.34,
          "type": "RSR"
        },
        "OGRS": {
          "level": "LOW",
          "score": 3.45,
          "type": "RSR"
        },
        "OGP": {
          "level": "MEDIUM",
          "score": 5.3,
          "type": "RSR"
        },
        "OVP": {
          "level": "HIGH",
          "score": 2.8,
          "type": "RSR"
        }
      }
    }
  }
}
""".trimIndent()
