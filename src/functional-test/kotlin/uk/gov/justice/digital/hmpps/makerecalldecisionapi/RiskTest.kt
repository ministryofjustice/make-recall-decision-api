package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.FourLevelRiskScoreLevel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OGP
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OGRS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPC
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OSPI
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.OVP
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreType.RSR
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ThreeLevelRiskScoreLevel

class RiskTest : FunctionalTest() {

  @Test
  fun `retrieve risk data`() {
    // when
    lastResponse = RestAssured
      .given()
      .pathParam("crn", testCrn)
      .header("Authorization", token)
      .get("$path/cases/{crn}/risk")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)
    assertResponse(lastResponse, riskExpectation())
  }
}

fun riskExpectation() = """
{
  "mappa": {
    "lastUpdatedDate": null,
    "level": null,
    "category": null,
    "error": "NOT_FOUND"
  },
  "roshSummary": {
    "whoIsAtRisk": "TBA",
    "lastUpdatedDate": "2022-07-27T12:09:41.000Z",
    "riskImminence": "TBA",
    "natureOfRisk": "TBA",
    "riskIncreaseFactors": "TBA",
    "riskOfSeriousHarm": {
      "riskInCommunity": {
        "riskToChildren": "LOW",
        "riskToPublic": "MEDIUM",
        "riskToKnownAdult": "HIGH",
        "riskToStaff": "VERY_HIGH",
        "riskToPrisoners": ""
      },
      "riskInCustody": {
        "riskToChildren": "LOW",
        "riskToPublic": "MEDIUM",
        "riskToKnownAdult": "HIGH",
        "riskToStaff": "VERY_HIGH",
        "riskToPrisoners": "VERY_HIGH"
      },
      "overallRisk": "VERY_HIGH"
    },
    "riskMitigationFactors": "TBA",
    "error": null
  },
  "userAccessResponse": null,
  "personalDetailsOverview": {
    "gender": "Male",
    "name": "Ikenberry Camploongo",
    "dateOfBirth": "1986-05-11",
    "age": 36,
    "crn": "D006296"
  },
  "assessmentStatus": "INCOMPLETE",
  "predictorScores": {
    "current": {
      "date": "2022-07-27",
      "scores": {
        "RSR": {
          "score": "4.12",
          "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
          "type": ${RSR.printName}
        },
        "OSPC": {
          "score": null,
          "level": "${FourLevelRiskScoreLevel.MEDIUM}",
          "type": ${OSPC.printName}
        },
        "OSPI": {
          "score": null,
          "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
          "type": ${OSPI.printName}
        },
        "OVP": {
          "oneYear": "12",
          "level": "${FourLevelRiskScoreLevel.LOW}",
          "twoYears": "21",
          "type": ${OVP.printName}
        },
        "OGP": {
          "oneYear": "5",
          "level": "${FourLevelRiskScoreLevel.LOW}",
          "twoYears": "8",
          "type": ${OGP.printName}
        },
        "OGRS": {
          "oneYear": "6",
          "level": "${FourLevelRiskScoreLevel.LOW}",
          "twoYears": "12",
          "type": ${OGRS.printName}
        }
      }
    },
    "historical": [
      {
        "date": "2025-12-24",
        "scores": {
          "RSR": {
            "score": "4.12",
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${RSR.printName}
          },
          "OSPC": {
            "score": null,
            "level": "${FourLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPC.printName}
          },
          "OSPI": {
            "score": null,
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPI.printName}
          },
          "OVP": {
            "oneYear": "12",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "21",
            "type": ${OVP.printName}
          },
          "OGP": {
            "oneYear": "5",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "8",
            "type": ${OGP.printName}
          },
          "OGRS": {
            "oneYear": "6",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "12",
            "type": ${OGRS.printName}
          }
        }
      },
      {
        "date": "2022-07-27",
        "scores": {
          "RSR": {
            "score": "4.12",
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${RSR.printName}
          },
          "OSPC": {
            "score": null,
            "level": "${FourLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPC.printName}
          },
          "OSPI": {
            "score": null,
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPI.printName}
          },
          "OVP": {
            "oneYear": "12",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "21",
            "type": ${OVP.printName}
          },
          "OGP": {
            "oneYear": "5",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "8",
            "type": ${OGP.printName}
          },
          "OGRS": {
            "oneYear": "6",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "12",
            "type": ${OGRS.printName}
          }
        }
      },
      {
        "date": "2022-07-21",
        "scores": {
          "RSR": {
            "score": "4.12",
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${RSR.printName}
          },
          "OSPC": {
            "score": null,
            "level": "${FourLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPC.printName}
          },
          "OSPI": {
            "score": null,
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPI.printName}
          },
          "OVP": {
            "oneYear": "12",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "21",
            "type": ${OVP.printName}
          },
          "OGP": {
            "oneYear": "5",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "8",
            "type": ${OGP.printName}
          },
          "OGRS": {
            "oneYear": "6",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "12",
            "type": ${OGRS.printName}
          }
        }
      },
      {
        "date": "2022-06-10",
        "scores": {
          "RSR": {
            "score": "4.12",
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${RSR.printName}
          },
          "OSPC": {
            "score": null,
            "level": "${FourLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPC.printName}
          },
          "OSPI": {
            "score": null,
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPI.printName}
          },
          "OVP": {
            "oneYear": "12",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "21",
            "type": ${OVP.printName}
          },
          "OGP": {
            "oneYear": "5",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "8",
            "type": ${OGP.printName}
          },
          "OGRS": {
            "oneYear": "6",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "12",
            "type": ${OGRS.printName}
          }
        }
      },
      {
        "date": "2022-06-09",
        "scores": {
          "RSR": {
            "score": "4.12",
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${RSR.printName}
          },
          "OSPC": {
            "score": null,
            "level": "${FourLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPC.printName}
          },
          "OSPI": {
            "score": null,
            "level": "${ThreeLevelRiskScoreLevel.MEDIUM}",
            "type": ${OSPI.printName}
          },
          "OVP": {
            "oneYear": "12",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "21",
            "type": ${OVP.printName}
          },
          "OGP": {
            "oneYear": "5",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "8",
            "type": ${OGP.printName}
          },
          "OGRS": {
            "oneYear": "6",
            "level": "${FourLevelRiskScoreLevel.LOW}",
            "twoYears": "12",
            "type": ${OGRS.printName}
          }
        }
      },
      {
        "date": "2022-04-27",
        "scores": {
          "RSR": {
            "score": "0.32",
            "level": "${ThreeLevelRiskScoreLevel.LOW}",
            "type": ${RSR.printName}
          },
          "OSPC": null,
          "OSPI": null,
          "OVP": null,
          "OGP": null,
          "OGRS": null
        }
      }
    ],
    "error": ""
  }
}
"""
