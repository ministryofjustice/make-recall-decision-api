package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class OverviewTest() : FunctionalTest() {
  @Test
  fun `overview, expected 200`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .given()
      .post(BASE_URL + "overview")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expected)
    assertResponse(lastResponse, overviewExpectation())
  }
}

fun overviewExpectation() = """//TODO correct
{
    "userAccessResponse": null,
    "personalDetailsOverview": {
        "name": "Lee JarIce",
        "dateOfBirth": "1969-06-23",
        "age": 53,
        "gender": "Female",
        "crn": "X263655"
    },
    "convictions": [
        {
            "active": true,
            "offences": [
                {
                    "mainOffence": true,
                    "description": "Accident offences - 80500",
                    "code": "80500"
                }
            ],
            "sentenceDescription": "ORA Community Order",
            "sentenceOriginalLength": 18,
            "sentenceOriginalLengthUnits": "Months",
            "sentenceExpiryDate": null,
            "licenceExpiryDate": null,
            "isCustodial": false
        },
        {
            "active": true,
            "offences": [
                {
                    "mainOffence": true,
                    "description": "Accident offences - 80500",
                    "code": "80500"
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
        "flags": [
            "Suicide/Self Harm",
            "Hate Crime",
            "Street Gangs",
            "MAPPA"
        ]
    },
    "activeRecommendation": null
}
""".trimIndent()
