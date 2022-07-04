package ft

import io.restassured.RestAssured
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class FunctionalTest {

  private lateinit var lastResponse: Response

  @Test
  fun `smoke test`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .get("http://127.0.0.1:8080/health")

    // then
    assertThat(lastResponse.getStatusCode()).isEqualTo(expected)
  }
}
