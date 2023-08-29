package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RiskTest() : FunctionalTest() {

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
          "level": "MEDIUM",
          "type": "RSR"
        },
        "OSPC": {
          "score": null,
          "level": "MEDIUM",
          "type": "OSP\/C"
        },
        "OSPI": {
          "score": null,
          "level": "MEDIUM",
          "type": "OSP\/I"
        },
        "OVP": {
          "oneYear": "12",
          "level": "LOW",
          "twoYears": "21",
          "type": "OVP"
        },
        "OGP": {
          "oneYear": "5",
          "level": "LOW",
          "twoYears": "8",
          "type": "OGP"
        },
        "OGRS": {
          "oneYear": "6",
          "level": "LOW",
          "twoYears": "12",
          "type": "OGRS"
        }
      }
    },
    "historical": [
      {
        "date": "2022-07-27",
        "scores": {
          "RSR": {
            "score": "4.12",
            "level": "MEDIUM",
            "type": "RSR"
          },
          "OSPC": {
            "score": null,
            "level": "MEDIUM",
            "type": "OSP\/C"
          },
          "OSPI": {
            "score": null,
            "level": "MEDIUM",
            "type": "OSP\/I"
          },
          "OVP": {
            "oneYear": "12",
            "level": "LOW",
            "twoYears": "21",
            "type": "OVP"
          },
          "OGP": {
            "oneYear": "5",
            "level": "LOW",
            "twoYears": "8",
            "type": "OGP"
          },
          "OGRS": {
            "oneYear": "6",
            "level": "LOW",
            "twoYears": "12",
            "type": "OGRS"
          }
        }
      },
      {
        "date": "2022-07-21",
        "scores": {
          "RSR": {
            "score": "4.12",
            "level": "MEDIUM",
            "type": "RSR"
          },
          "OSPC": {
            "score": null,
            "level": "MEDIUM",
            "type": "OSP\/C"
          },
          "OSPI": {
            "score": null,
            "level": "MEDIUM",
            "type": "OSP\/I"
          },
          "OVP": {
            "oneYear": "12",
            "level": "LOW",
            "twoYears": "21",
            "type": "OVP"
          },
          "OGP": {
            "oneYear": "5",
            "level": "LOW",
            "twoYears": "8",
            "type": "OGP"
          },
          "OGRS": {
            "oneYear": "6",
            "level": "LOW",
            "twoYears": "12",
            "type": "OGRS"
          }
        }
      },
      {
        "date": "2022-06-10",
        "scores": {
          "RSR": {
            "score": "4.12",
            "level": "MEDIUM",
            "type": "RSR"
          },
          "OSPC": {
            "score": null,
            "level": "MEDIUM",
            "type": "OSP\/C"
          },
          "OSPI": {
            "score": null,
            "level": "MEDIUM",
            "type": "OSP\/I"
          },
          "OVP": {
            "oneYear": "12",
            "level": "LOW",
            "twoYears": "21",
            "type": "OVP"
          },
          "OGP": {
            "oneYear": "5",
            "level": "LOW",
            "twoYears": "8",
            "type": "OGP"
          },
          "OGRS": {
            "oneYear": "6",
            "level": "LOW",
            "twoYears": "12",
            "type": "OGRS"
          }
        }
      },
      {
        "date": "2022-06-09",
        "scores": {
          "RSR": {
            "score": "4.12",
            "level": "MEDIUM",
            "type": "RSR"
          },
          "OSPC": {
            "score": null,
            "level": "MEDIUM",
            "type": "OSP\/C"
          },
          "OSPI": {
            "score": null,
            "level": "MEDIUM",
            "type": "OSP\/I"
          },
          "OVP": {
            "oneYear": "12",
            "level": "LOW",
            "twoYears": "21",
            "type": "OVP"
          },
          "OGP": {
            "oneYear": "5",
            "level": "LOW",
            "twoYears": "8",
            "type": "OGP"
          },
          "OGRS": {
            "oneYear": "6",
            "level": "LOW",
            "twoYears": "12",
            "type": "OGRS"
          }
        }
      },
      {
        "date": "2022-04-27",
        "scores": {
          "RSR": {
            "score": "0.32",
            "level": "LOW",
            "type": "RSR"
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
