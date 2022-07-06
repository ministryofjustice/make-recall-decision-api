package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RiskTest() : FunctionalTest() {
  @Test
  fun `retrieve risk data, expected 200`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .given()
      .pathParam("crn", testCrn)
      .header("Authorization", token)
      .get("http://127.0.0.1:8080/cases/{crn}/risk")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
  }
}
