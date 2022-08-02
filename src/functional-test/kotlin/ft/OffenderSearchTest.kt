package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class OffenderSearchTest() : FunctionalTest() {
  // TODO refactor to use mrd-functionsal-test app
  @Test
  fun `offender search, expected 200`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .given()
//      .queryParam("crn", testCrn)
//      .header("Authorization", token)
      .get("http://127.0.0.1:8080/search")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
  }
}
