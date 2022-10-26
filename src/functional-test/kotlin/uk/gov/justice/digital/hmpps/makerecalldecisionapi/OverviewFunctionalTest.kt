package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OverviewTest() : FunctionalTest() {

  @Test
  fun `fetch overview details`() {

    // when
    lastResponse = RestAssured
      .given()
      .pathParam("crn", testCrn)
      .header("Authorization", token)
      .get("$path/cases/{crn}/overview")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)
    assertResponse(lastResponse, overviewExpectation())
  }
}

fun overviewExpectation() = """
{
    "userAccessResponse": null,
    "personalDetailsOverview": {
        "name": "Ikenberry Camploongo",
        "firstName": "Ikenberry",
        "middleNames": "ZZ",
        "surname": "Camploongo",
        "dateOfBirth": "1986-05-11",
        "age": 36,
        "gender": "Male",
        "crn": "D006296",
        "ethnicity": "",
        "croNumber": "",
        "mostRecentPrisonerNumber": "",
        "pncNumber": "",
        "nomsNumber": ""
    },
    "convictions": [
        {
            "active": true,
            "offences": [
                {
                    "mainOffence": true,
                    "description": "Endangering life at sea - 00700",
                    "code": "00700",
                    "offenceDate": "2014-02-07"
                }
            ],
            "sentenceDescription": "CJA - Std Determinate Custody",
            "sentenceOriginalLength": 12,
            "sentenceOriginalLengthUnits": "Months",
            "sentenceExpiryDate": null,
            "licenceExpiryDate": null,
            "isCustodial": true
        }
    ],
    "releaseSummary": {
        "lastRelease": null,
        "lastRecall": null
    },
    "risk": {
        "flags": [],
        "riskManagementPlan": {
            "assessmentStatusComplete": null,
            "lastUpdatedDate": null,
            "latestDateCompleted": null,
            "initiationDate": null,
            "contingencyPlans": null,
            "error": "SERVER_ERROR"
        },
        "assessments": {
            "error": "SERVER_ERROR",
            "lastUpdatedDate": null,
            "offenceDataFromLatestCompleteAssessment": null,
            "offencesMatch": null,
            "offenceDescription": null
        }
    },
    "activeRecommendation": null
}
""".trimIndent()
