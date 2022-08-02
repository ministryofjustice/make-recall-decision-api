package ft

import io.restassured.response.Response
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

open class FunctionalTest {
  val BASE_URL = "https://mrd-functional-test-dev.hmpps.service.justice.gov.uk/"
  lateinit var lastResponse: Response
  fun assertResponse(restassuredResponse: Response, expectation: String) {
    JSONAssert.assertEquals(JSONObject(lastResponse.asString()), JSONObject(expectation), JSONCompareMode.LENIENT)
  }
}
