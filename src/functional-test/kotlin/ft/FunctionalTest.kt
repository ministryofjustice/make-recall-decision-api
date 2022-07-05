package ft

import io.restassured.RestAssured
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeAll
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.util.Base64

open class FunctionalTest {
  lateinit var lastResponse: Response
  val token = "Bearer ${getToken()}"
  val testCrn = "D006296"

  companion object {
    val client_id = "bill-sclater"
    val client_secret = "KbICZKm61'xNx+9y5Z63IvA-7(ohdu&Ubg0LF0m%\$q0suoF<P8S3E\$QV*;5h"
    val base64EncodedClientCreds = Base64.getEncoder().encodeToString("$client_id:$client_secret".toByteArray())
    val authHeaderValue = "Basic $base64EncodedClientCreds"
    val authPath = "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=client_credentials&username=BillSclater"

    @BeforeAll
    private fun getToken(): String {
      val tokenResponse = RestAssured
        .given()
        .contentType(APPLICATION_JSON_VALUE)
        .header("Authorization", authHeaderValue)
        .post(authPath)
      assertThat(tokenResponse.statusCode).isEqualTo(HttpStatus.OK.value())
      return JSONObject(tokenResponse.body().asString()).getString("access_token")
    }
  }
}
