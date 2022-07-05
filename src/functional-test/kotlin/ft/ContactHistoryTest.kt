package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ContactHistoryTest() : FunctionalTest() {
  @Test
  fun `fetch contact history, expected 200`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .given()
      .pathParam("crn", testCrn)
      .header("Authorization", token)
      .get("http://127.0.0.1:8080/cases/{crn}/contact-history")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
  }
}
