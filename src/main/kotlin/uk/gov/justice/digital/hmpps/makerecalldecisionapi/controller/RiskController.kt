package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import io.swagger.v3.oas.annotations.Operation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RiskService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
internal class RiskController(
  private val riskService: RiskService
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @PreAuthorize("hasRole('ROLE_MAKE_RECALL_DECISION')")
  @GetMapping("/cases/{crn}/risk")
  @Operation(summary = "WIP: Returns case summary risk information")
  suspend fun risk(@PathVariable("crn") crn: String): RiskResponse {
    log.info("Risk endpoint hit for CRN: $crn")
    return riskService.getRisk(crn)
  }
}

fun riskResponse() = """
{
  "personalDetailsOverview": {
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
  },
  "natureOfRisk": {
    "oasysHeading": {
      "number": "10.2",
      "description": "What is the nature of the risk?"
    },
    "description": "Mr Edwin has a pattern of resorting to violent behaviour if he feels humiliated/threatened/stressed. He has been known to use weapons or opportunistically looks for a way to cause the victim maximum harm. His risk increases greatly if he has been taking alcohol or drugs, which is likely to do if he feels like he is 'out of control' of his life."
  },
  "contingencyPlan": {
    "oasysHeading": {
      "number": "11.9",
      "description": "Contingency plan"
    },
    "description": "If Mr Edwin enters enters pubs in Enfield Town - issue licence compliance letter\nIf Mr Edwin associates with Mr Daniels, Mr Moreland or Mr Barksdale - issue decision not to recall letter or recall. Supervision session to discuss reasons for association.\nIf Mr Edwin loses his accommodation, refer to housing support. \nIf Mr Edwin loses his employment, refer to ETE services to establish alternative employment\nIf Mr Edwin returns to drinking or taking drugs, cosndier increase in MAPPA level, refer to CGL support, increase reporting or recall."
  },
  "whoIsAtRisk": {
    "oasysHeading": {
      "number": "10.1",
      "description": "Who is at risk?"
    },
    "description": "Public:\nMr Edwin poses a very high risk to members of the public, especially if he believes they have mocked him, he has been drinking/taking drugs and is surrounded by his associates in the Westside Gang.\n\nKnown adult:\nMr Edwin poses a medium risk to his victim Wallce Stanfield. He has shown no desire for retribution but is unpredictable and resolves problems through violence if taking drugs and surrounded by peers.\n\nStaff:\nMr Edwin poses a high risk to probation staff as he resorts to violence if he feels undermined or not listed to."
  },
  "circumstancesIncreaseRisk": {
    "oasysHeading": {
      "number": "10.4",
      "description": "What circumstances are likely to increase the risk?"
    },
    "description": "Factors include:\n- relationship breakdown\n- unstable accommodation situation/lack of stable housing\n- association with criminal peers/associates\n- a lack of steady employment which causes him to return to drug dealing to make money\n- drug and alcohol use"
  },
  "factorsToReduceRisk": {
    "oasysHeading": {
      "number": "10.5",
      "description": "What factors are likely to reduce the risk?"
    },
    "description": "Factors include:\n- engagement in interventions related to mood/behaviour control\n- avoidance of drugs and alcohol\n- steady employment\n- stable accommodation\n- sources of support and value that are separate from being part of a gang/maintaining reputation e.g. family engagement, job, education"
  },
  "whenRiskHighest": {
    "oasysHeading": {
      "number": "10.3",
      "description": "When is the risk likely to be greatest?"
    },
    "description": "If Mr Edwin has had recent changes in his life that cause him to take drugs or drink; if he is surrounded by criminal peers/associates; if he is in a public place where he feels like he needs to protect his reputation."
  }
}
""".trimIndent()
