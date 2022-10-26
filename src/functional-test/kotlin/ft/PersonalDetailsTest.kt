package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class PersonalDetailsTest() : FunctionalTest() {
  @Test
  fun `fetch personal details`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .given()
      .pathParam("crn", testCrn)
      .header("Authorization", token)
      .get("http://127.0.0.1:8080/cases/{crn}/personal-details")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
    assertResponse(lastResponse, personalDetailsExpectation())
  }
}

fun personalDetailsExpectation() = """
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
    "addresses": [
        {
            "line1": "99 Oxford Road",
            "line2": "",
            "town": "Epsom",
            "postcode": "SW16 1AF",
            "noFixedAbode": false
        }
    ],
    "offenderManager": {
        "name": "HMPPS Auth",
        "phoneNumber": "01111111111",
        "email": "",
        "probationTeam": {
            "code": "N07CHT",
            "label": "Automation SPG",
            "localDeliveryUnitDescription": "All NPS London"
        },
        "probationAreaDescription": "NPS London"
    },
    "activeRecommendation": null
}
""".trimIndent()
