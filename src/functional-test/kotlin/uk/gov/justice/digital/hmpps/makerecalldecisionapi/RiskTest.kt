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
    "whoIsAtRisk": "X, Y and Z are at risk",
    "lastUpdatedDate": "2022-05-19T08:26:31.000Z",
    "riskImminence": "the risk is imminent and more probably in X situation",
    "natureOfRisk": "The nature of the risk is X",
    "riskIncreaseFactors": "If offender in situation X the risk can be higher",
    "riskOfSeriousHarm": {
      "riskInCommunity": {
        "riskToChildren": "HIGH",
        "riskToPublic": "HIGH",
        "riskToKnownAdult": "HIGH",
        "riskToStaff": "MEDIUM",
        "riskToPrisoners": ""
      },
      "riskInCustody": {
        "riskToChildren": "LOW",
        "riskToPublic": "LOW",
        "riskToKnownAdult": "HIGH",
        "riskToStaff": "VERY_HIGH",
        "riskToPrisoners": "VERY_HIGH"
      },
      "overallRisk": "HIGH"
    },
    "riskMitigationFactors": "Giving offender therapy in X will reduce the risk",
    "error": null
  },
  "userAccessResponse": null,
  "personalDetailsOverview": {
    "gender": "Male",
    "name": "Ikenberry Camploongo",
    "dateOfBirth": "1986-05-11",
    "age": 36,
    "crn": "A12345"
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
  },
  "activeRecommendation": {
    "recallType": {
      "allOptions": [
        {
          "text": "Fixed term",
          "value": "FIXED_TERM"
        },
        {
          "text": "Standard",
          "value": "STANDARD"
        },
        {
          "text": "No recall",
          "value": "NO_RECALL"
        }
      ],
      "selected": {
        "details": "My details",
        "value": "FIXED_TERM"
      }
    },
    "lastModifiedDate": "2022-10-28T10:39:36.390Z",
    "lastModifiedBy": "SOME_USER",
    "recommendationId": 888238631
  }
}
"""
