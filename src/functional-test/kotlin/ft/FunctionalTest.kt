package ft

import io.restassured.RestAssured
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.util.Base64
import kotlin.math.log

open class FunctionalTest {
  lateinit var lastResponse: Response
  val token = "Bearer ${getToken()}"
  val testCrn = "D006296"
  companion object {
    val BASE_URL = "https://make-recall-decision-api-dev.hmpps.service.justice.gov.uk/"
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    val client_id = System.getenv("SYSTEM_CLIENT_ID")
    val client_secret = System.getenv("SYSTEM_CLIENT_SECRET")
    val base64EncodedClientCreds = Base64.getEncoder().encodeToString("$client_id:$client_secret".toByteArray())
    val authHeaderValue = "Basic $base64EncodedClientCreds"
    val authPath = "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=client_credentials&username=${System.getenv("USER_NAME")}"

    @BeforeAll
    private fun getToken(): String {
      log.info("Running Functional Tests against $BASE_URL")
      val tokenResponse = RestAssured
        .given()
        .contentType(APPLICATION_JSON_VALUE)
        .header("Authorization", authHeaderValue)
        .post(authPath)
      log.info("Response from Auth on getting token:: ${tokenResponse.statusCode}")
      assertThat(tokenResponse.statusCode).isEqualTo(HttpStatus.OK.value())
      return JSONObject(tokenResponse.body().asString()).getString("access_token")
    }
  }
}
