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
      .get(BASE_URL+ "search")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
  }
}
