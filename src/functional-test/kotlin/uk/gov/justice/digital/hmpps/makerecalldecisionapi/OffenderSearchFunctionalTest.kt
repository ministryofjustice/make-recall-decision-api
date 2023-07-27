package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OffenderSearchTest() : FunctionalTest() {

  @Test
  fun `fetch offender search`() {
    // when
    lastResponse = RestAssured
      .given()
      .queryParam("crn", testCrn)
      .queryParam("page", 0)
      .queryParam("pageSize", 1)
      .header("Authorization", token)
      .get("$path/paged-search")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)
    assertJsonArrayResponse(lastResponse, offenderSearchExpectation())
  }
}

fun offenderSearchExpectation() = """
{
  "content" = [
        {
            "userExcluded": null,
            "userRestricted": null,
            "name": "Ikenberry Camploongo",
            "crn": "D006296",
            "dateOfBirth": "1986-05-11"
        }
    ]
}
""".trimIndent()
