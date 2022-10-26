package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

class RecommendationTest() : FunctionalTest() {

  @Test
  fun `make a recommendation, expected 201`() {

    // when
    lastResponse = RestAssured
      .given()
      .contentType(APPLICATION_JSON_VALUE)
      .header("Authorization", token)
      .body(recommendationRequest("X12345"))
      .post("$path/recommendations")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedCreated)
  }
}

fun recommendationRequest(crn: String) = """
  {
    "crn": "$crn"
  }
""".trimIndent()
