package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

class RecommendationTest() : FunctionalTest() {
  @Test
  fun `make a recommendation, expected 201`() {
    // given
    val expected = HttpStatus.CREATED.value()

    // when
    lastResponse = RestAssured
      .given()
      .contentType(APPLICATION_JSON_VALUE)
      .header("Authorization", token)
      .body(recommendationRequest("X12345"))
      .post(BASE_URL + "recommendations")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
  }
}

fun recommendationRequest(crn: String) = """
  {
    "crn": "$crn"
  }
""".trimIndent()
