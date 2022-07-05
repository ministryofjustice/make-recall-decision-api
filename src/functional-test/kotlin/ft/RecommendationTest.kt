package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

class RecommendationTest() : FunctionalTest() {
  @Test
  fun `make a recommendation, expected 200`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .given()
      .pathParam("crn", testCrn)
      .contentType(APPLICATION_JSON_VALUE)
      .header("Authorization", token)
      .post("http://127.0.0.1:8080/cases/{crn}/recommendation")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expected)
  }
}
