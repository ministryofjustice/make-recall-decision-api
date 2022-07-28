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
      .pathParam("crn", testCrn)
      .header("Authorization", token)
      .get(BASE_URL + "cases/{crn}/overview")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
  }
}
