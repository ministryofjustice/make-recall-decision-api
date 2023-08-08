package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CvlApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionCvlDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionSearch
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceMatchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase

@ActiveProfiles("test")
class CvlApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var cvlApiClient: CvlApiClient

  @Test
  fun `retrieves licence matches`() {
    // given
    val nomisId = "12345"

    cvlLicenceMatchResponse(nomisId, crn)

    // and
    val expected = listOf(
      LicenceMatchResponse(
        licenceId = 123344,
        licenceType = "AP",
        licenceStatus = "IN_PROGRESS",
        crn = crn
      )
    )

    // when
    val actual = cvlApiClient.getLicenceMatch(crn, LicenceConditionSearch(listOf(nomisId))).block()

    // then
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun `retrieves licence by id`() {
    // given
    val licenceId = 67868
    val nomisId = "12345"

    cvlLicenceByIdResponse(licenceId, nomisId, crn)

    // and
    val expected =
      LicenceConditionCvlResponse(
        conditionalReleaseDate = "10/06/2022",
        actualReleaseDate = "11/06/2022",
        sentenceStartDate = "12/06/2022",
        sentenceEndDate = "13/06/2022",
        licenceStartDate = "14/06/2022",
        licenceExpiryDate = "15/06/2022",
        topupSupervisionStartDate = "16/06/2022",
        topupSupervisionExpiryDate = "17/06/2022",
        standardLicenceConditions = listOf(LicenceConditionCvlDetail(code = "9ce9d594-e346-4785-9642-c87e764bee43", text = "This is a standard licence condition")),
        standardPssConditions = listOf(LicenceConditionCvlDetail(code = "9ce9d594-e346-4785-9642-c87e764bee44", text = "This is a standard PSS licence condition")),
        additionalLicenceConditions = listOf(LicenceConditionCvlDetail(code = "9ce9d594-e346-4785-9642-c87e764bee45", category = "Freedom of movement", text = "This is an additional licence condition", expandedText = "Expanded additional licence condition")),
        additionalPssConditions = listOf(LicenceConditionCvlDetail(code = "9ce9d594-e346-4785-9642-c87e764bee46", category = "Freedom of movement", text = "This is an additional PSS licence condition", expandedText = "Expanded additional PSS licence condition")),
        bespokeConditions = listOf(LicenceConditionCvlDetail(code = "9ce9d594-e346-4785-9642-c87e764bee47", text = "This is a bespoke condition"))
      )

    // when
    val actual = cvlApiClient.getLicenceById(crn, licenceId).block()

    // then
    assertThat(actual, equalTo(expected))
  }
}
