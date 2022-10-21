package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CvlApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionSearch
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceMatchResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDate

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
      LicenceConditionResponse(
        conditionalReleaseDate = LocalDate.parse("2022-06-10"),
        actualReleaseDate = LocalDate.parse("2022-06-11"),
        sentenceStartDate = LocalDate.parse("2022-06-12"),
        sentenceEndDate = LocalDate.parse("2022-06-13"),
        licenceStartDate = LocalDate.parse("2022-06-14"),
        licenceExpiryDate = LocalDate.parse("2022-06-15"),
        topupSupervisionStartDate = LocalDate.parse("2022-06-16"),
        topupSupervisionExpiryDate = LocalDate.parse("2022-06-17"),
        standardLicenceConditions = listOf(LicenceConditionDetail(text = "This is a standard licence condition")),
        standardPssConditions = listOf(LicenceConditionDetail(text = "This is a standard PSS licence condition")),
        additionalLicenceConditions = listOf(LicenceConditionDetail(text = "This is an additional licence condition", expandedText = "Expanded additional licence condition")),
        additionalPssConditions = listOf(LicenceConditionDetail(text = "This is an additional PSS licence condition", expandedText = "Expanded additional PSS licence condition")),
        bespokeConditions = listOf(LicenceConditionDetail(text = "This is a bespoke condition"))
      )

    // when
    val actual = cvlApiClient.getLicenceById(crn, licenceId).block()

    // then
    assertThat(actual, equalTo(expected))
  }
}
