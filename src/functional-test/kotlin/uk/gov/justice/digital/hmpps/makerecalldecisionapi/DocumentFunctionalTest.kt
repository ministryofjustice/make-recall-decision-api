//package uk.gov.justice.digital.hmpps.makerecalldecisionapi
//
//import io.restassured.RestAssured
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.Test
//
//class DocumentFunctionalTest() : FunctionalTest() {
//
//  @Test
//  fun `fetch document`() {
//
//    // when
//    lastResponse = RestAssured
//      .given()
//      .pathParam("crn", testCrn)
//      .header("Authorization", token)
//      .get("$path/cases/{crn}/documents/8821dae0-07a5-464e-8be3-e26207fa0279")
//
//    // then
//    assertThat(lastResponse.statusCode).isEqualTo(expectedOk)
//    assertThat(lastResponse.headers.get("Content-Disposition").value).isEqualTo("attachment; filename*=UTF-8''ikenberry-camploongo-d006296-66.pdf")
//    assertThat(lastResponse.headers.get("Content-Type").value).isEqualTo("application/pdf;charset=UTF-8")
//  }
//}
