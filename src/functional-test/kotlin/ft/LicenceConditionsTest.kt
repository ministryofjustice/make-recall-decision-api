package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class LicenceConditionsTest() : FunctionalTest() {
  // TODO refactor to use mrd-functionsal-test app
  @Test
  fun `retrieve licence conditions, expected 200`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .given()
//      .pathParam("crn", testCrn)
//      .header("Authorization", token)
      .get("http://127.0.0.1:8080/cases/{crn}/licence-conditions")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
  }
}
