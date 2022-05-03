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

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
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
  "riskOfSeriousHarm": {
    "overallRisk": "VERY_HIGH",
    "riskToChildren": "LOW",
    "riskToPublic": "VERY_HIGH",
    "riskToKnownAdult": "MEDIUM",
    "riskToStaff": "HIGH",
    "lastUpdated": "2021-10-09"
  },
  "mappa": {
    "level": "CAT 2/LEVEL 1",
    "isNominal": false,
    "lastUpdated": "10th October 2021"
  },
  "predictorScores": {
    "current": {
      "RSR": {
        "level": "HIGH",
        "score": 23,
        "type": "RSR"
      },
      "OSPC": {
        "level": "LOW",
        "score": 3.45,
        "type": "OSP/C"
      },
      "OSPI": {
        "level": "MEDIUM",
        "score": 5.3,
        "type": "OSP/I"
      },
      "OGRS": {
        "level": "LOW",
        "score": 12,
        "type": "RSR"
      }
    },
    "historical": [
      {
        "date": "14 May 2019 at 12:00",
        "scores": {
          "RSR": {
            "level": "HIGH",
            "score": 18,
            "type": "RSR"
          },
          "OSPC": {
            "level": "LOW",
            "score": 6.8,
            "type": "OSP/C"
          },
          "OSPI": {
            "level": "MEDIUM",
            "score": 8.1,
            "type": "OSP/I"
          },
          "OGRS": {
            "level": "LOW",
            "score": 5.43,
            "type": "OGRS"
          }
        }
      },
      {
        "date": "12 September 2018 at 12:00",
        "scores": {
          "RSR": {
            "level": "MEDIUM",
            "score": 12,
            "type": "RSR"
          },
          "OSPC": {
            "level": "LOW",
            "score": 6.2,
            "type": "OSP/C"
          },
          "OSPI": {
            "level": "MEDIUM",
            "score": 8.6,
            "type": "OSP/I"
          },
          "OGRS": {
            "level": "MEDIUM",
            "score": 40,
            "type": "OGRS"
          }
        }
      }
    ]
  }
}
""".trimIndent()
