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
    "gender": "Male",
    "ethnicity": "",
    "croNumber": "",
    "dateOfBirth": "1986-05-11",
    "middleNames": "ZZ",
    "firstName": "Ikenberry",
    "nomsNumber": "",
    "surname": "Camploongo",
    "pncNumber": "",
    "name": "Ikenberry Camploongo",
    "age": 36,
    "crn": "D006296",
    "mostRecentPrisonerNumber": ""
  },
  "releaseSummary": {
    "lastRecall": null,
    "lastRelease": null
  },
  "risk": {
    "assessments": {
      "lastUpdatedDate": "2022-07-27T12:09:41.000Z",
      "offencesMatch": false,
      "error": null,
      "offenceDataFromLatestCompleteAssessment": false,
      "offenceDescription": "Holding a Gun"
    },
    "flags": [],
    "riskManagementPlan": {
      "lastUpdatedDate": "2022-07-27T12:10:58.000Z",
      "assessmentStatusComplete": false,
      "initiationDate": "2022-07-27T12:10:58.000Z",
      "contingencyPlans": "developing contingency plans consider what role increased supervision, additional monitoring\/control or greater intervention might play when a trigger is detected. Additionally consider whether following the trigger greater protection is needed for those named individuals or groups at risk.",
      "error": null,
      "latestDateCompleted": "2022-07-27T12:09:41.000Z"
    }
  },
  "convictions": [
    {
      "sentenceExpiryDate": null,
      "sentenceDescription": "CJA - Std Determinate Custody",
      "isCustodial": true,
      "offences": [
        {
          "code": "00700",
          "description": "Endangering life at sea - 00700",
          "offenceDate": "2014-02-07",
          "mainOffence": true
        }
      ],
      "licenceExpiryDate": null,
      "active": true,
      "sentenceOriginalLength": 12,
      "sentenceOriginalLengthUnits": "Months"
    }
  ]
}
""".trimIndent()
