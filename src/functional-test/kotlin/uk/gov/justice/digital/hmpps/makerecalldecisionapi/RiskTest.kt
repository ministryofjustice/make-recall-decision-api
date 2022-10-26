package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RiskTest() : FunctionalTest() {

  @Test
  fun `retrieve risk data`() {

    // when
    lastResponse = RestAssured
      .given()
      .pathParam("crn", testCrn)
      .header("Authorization", token)
      .get("$path/cases/{crn}/risk")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)
  }
}
