package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LicenceConditionsFunctionalTest() : FunctionalTest() {

  @Test
  fun `retrieve licence conditions`() {

    // when
    lastResponse = RestAssured
      .given()
      .pathParam("crn", testCrn)
      .header("Authorization", token)
      .get("$path/cases/{crn}/licence-conditions")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)
    assertResponse(lastResponse, licenceConditionsExpectation())
  }

  fun licenceConditionsExpectation() = """
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
            "convictionId": 2500007681,
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
            "sentenceSecondLength": null,
            "sentenceSecondLengthUnits": null,
            "sentenceStartDate": "2014-11-17",
            "sentenceExpiryDate": null,
            "licenceExpiryDate": null,
            "postSentenceSupervisionEndDate": null,
            "statusCode": "A",
            "statusDescription": "Sentenced - In Custody",
            "licenceConditions": [],
            "licenceDocuments": [],
            "isCustodial": true
        }
    ],
    "releaseSummary": {
        "lastRelease": null,
        "lastRecall": null
    },
    "activeRecommendation": null
}
  """.trimIndent()
}
