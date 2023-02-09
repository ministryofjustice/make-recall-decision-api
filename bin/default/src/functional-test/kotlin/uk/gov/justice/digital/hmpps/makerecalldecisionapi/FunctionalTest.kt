package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import io.restassured.RestAssured
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.BeforeAll
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import java.util.Base64

open class FunctionalTest {
  lateinit var lastResponse: Response
  val token = "Bearer ${getToken()}"
  val testCrn = "D006296"
  val expectedOk = HttpStatus.OK.value()
  val expectedCreated = HttpStatus.CREATED.value()

  companion object {
    val client_id = System.getenv("SYSTEM_CLIENT_ID")
    val client_secret = System.getenv("SYSTEM_CLIENT_SECRET")
    val base64EncodedClientCreds = Base64.getEncoder().encodeToString("$client_id:$client_secret".toByteArray())
    val authHeaderValue = "Basic $base64EncodedClientCreds"
    val authPath = "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=client_credentials&username=${System.getenv("USER_NAME")}"
    val path = "http://127.0.0.1:8080"

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

    fun assertResponse(restassuredResponse: Response, expectation: String) {
      val response = JSONObject(restassuredResponse.asString())
      assertThat(response.has("activeRecommendation")).isTrue
      response.remove("activeRecommendation")
      JSONAssert.assertEquals(response, JSONObject(expectation), JSONCompareMode.NON_EXTENSIBLE)
    }

    fun assertFullResponse(restassuredResponse: Response, expectation: String) {
      JSONAssert.assertEquals(JSONObject(restassuredResponse.asString()), JSONObject(expectation), JSONCompareMode.NON_EXTENSIBLE)
    }

    fun assertJsonArrayResponse(restassuredResponse: Response, expectation: String) {
      JSONAssert.assertEquals(JSONArray(restassuredResponse.asString()), JSONArray(expectation), JSONCompareMode.NON_EXTENSIBLE)
    }
  }
}
