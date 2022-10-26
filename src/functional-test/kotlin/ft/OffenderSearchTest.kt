package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class OffenderSearchTest() : FunctionalTest() {
  @Test
  fun `offender search, expected 200`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .given()
      .queryParam("crn", testCrn)
      .header("Authorization", token)
      .get("http://127.0.0.1:8080/search")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
    assertJsonArrayResponse(lastResponse, offenderSearchExpectation())
  }
}

fun offenderSearchExpectation() = """
[
    {
        "userExcluded": null,
        "userRestricted": null,
        "name": "Ikenberry Camploongo",
        "crn": "D006296",
        "dateOfBirth": "1986-05-11"
    }
]
""".trimIndent()
