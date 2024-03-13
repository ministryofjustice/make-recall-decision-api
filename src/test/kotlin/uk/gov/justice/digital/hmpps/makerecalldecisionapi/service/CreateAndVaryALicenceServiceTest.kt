package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionSearch
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoCvlLicenceByIdException
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class CreateAndVaryALicenceServiceTest : ServiceTestBase() {

  @Test
  fun `build licence conditions from a CVL response with licence conditions`() {
    runTest {
      val licenceId = 444333
      val nomsId = "67876"

      given(
        cvlApiClient.getLicenceMatch(
          crn,
          LicenceConditionSearch(nomsId = listOf(nomsId), status = listOf("ACTIVE")),
        ),
      ).willReturn(Mono.fromCallable { licenceMatchedResponse(licenceId, crn) })
      given(cvlApiClient.getLicenceById(crn, licenceId)).willReturn(Mono.fromCallable { licenceByIdResponse() })

      val response = createAndVaryALicenceService.buildLicenceConditions(crn, nomsId)

      assertThat(response.size).isEqualTo(1)
      assertThat(response[0].conditionalReleaseDate).isEqualTo(LocalDate.parse("2022-06-10"))
      assertThat(response[0].actualReleaseDate).isEqualTo(LocalDate.parse("2022-06-11"))
      assertThat(response[0].sentenceStartDate).isEqualTo(LocalDate.parse("2022-06-12"))
      assertThat(response[0].sentenceEndDate).isEqualTo(LocalDate.parse("2022-06-13"))
      assertThat(response[0].licenceStartDate).isEqualTo(LocalDate.parse("2022-06-14"))
      assertThat(response[0].licenceExpiryDate).isEqualTo(LocalDate.parse("2022-06-15"))
      assertThat(response[0].topupSupervisionStartDate).isEqualTo(LocalDate.parse("2022-06-16"))
      assertThat(response[0].topupSupervisionExpiryDate).isEqualTo(LocalDate.parse("2022-06-17"))
      assertThat(response[0].standardLicenceConditions!![0].text).isEqualTo("This is a standard licence condition")
      assertThat(response[0].standardPssConditions!![0].text).isEqualTo("This is a standard PSS licence condition")
      assertThat(response[0].additionalLicenceConditions!![0].text).isEqualTo("This is an additional licence condition")
      assertThat(response[0].additionalLicenceConditions!![0].expandedText).isEqualTo("Expanded additional licence condition")
      assertThat(response[0].additionalPssConditions!![0].text).isEqualTo("This is an additional PSS licence condition")
      assertThat(response[0].additionalPssConditions!![0].expandedText).isEqualTo("Expanded additional PSS licence condition")
      assertThat(response[0].bespokeConditions!![0].text).isEqualTo("This is a bespoke condition")

      then(cvlApiClient).should().getLicenceMatch(crn, LicenceConditionSearch(nomsId = listOf(nomsId)))
      then(cvlApiClient).should().getLicenceById(crn, licenceId)
    }
  }

  @Test
  fun `handle response from CVL when there are no matched licences returned`() {
    runTest {
      val nomsId = "67876"

      given(
        cvlApiClient.getLicenceMatch(
          crn,
          LicenceConditionSearch(nomsId = listOf(nomsId), status = listOf("ACTIVE")),
        ),
      ).willReturn(Mono.fromCallable { emptyList() })

      val response = createAndVaryALicenceService.buildLicenceConditions(crn, nomsId)

      assertThat(response.size).isEqualTo(0)

      then(cvlApiClient).should().getLicenceMatch(crn, LicenceConditionSearch(nomsId = listOf(nomsId)))
    }
  }

  @Test
  fun `handle response from CVL when there are no licence conditions returned`() {
    runTest {
      val licenceId = 444333
      val nomsId = "67876"

      given(
        cvlApiClient.getLicenceMatch(
          crn,
          LicenceConditionSearch(nomsId = listOf(nomsId), status = listOf("ACTIVE")),
        ),
      ).willReturn(Mono.fromCallable { licenceMatchedResponse(licenceId, crn) })
      given(cvlApiClient.getLicenceById(crn, licenceId)).willReturn(Mono.fromCallable { LicenceConditionCvlResponse() })

      val response = createAndVaryALicenceService.buildLicenceConditions(crn, nomsId)

      assertThat(response.size).isEqualTo(1)
      assertThat(response[0].actualReleaseDate).isNull()

      then(cvlApiClient).should().getLicenceMatch(crn, LicenceConditionSearch(nomsId = listOf(nomsId)))
    }
  }

  @Test
  fun `filter out licences from CVL response when CRNs do not match`() {
    runTest {
      val licenceId = 444333
      val nomsId = "67876"

      given(
        cvlApiClient.getLicenceMatch(
          crn,
          LicenceConditionSearch(nomsId = listOf(nomsId), status = listOf("ACTIVE")),
        ),
      ).willReturn(Mono.fromCallable { licenceMatchedResponse(licenceId, "RandomCRN") })

      val response = createAndVaryALicenceService.buildLicenceConditions(crn, nomsId)

      assertThat(response.size).isEqualTo(0)
    }
  }

  @Test
  fun `given call to get licence by id fails on call to CVL with a 404 then return empty licence condition object`() {
    runTest {
      val licenceId = 444333
      val nomsId = "67876"

      given(
        cvlApiClient.getLicenceMatch(
          crn,
          LicenceConditionSearch(nomsId = listOf(nomsId), status = listOf("ACTIVE")),
        ),
      ).willReturn(Mono.fromCallable { licenceMatchedResponse(licenceId, crn) })

      given(cvlApiClient.getLicenceById(crn, licenceId)).willThrow(
        NoCvlLicenceByIdException("No licence id found for licence id: $licenceId"),
      )

      val response = createAndVaryALicenceService.buildLicenceConditions(crn, nomsId)

      assertThat(response.size).isEqualTo(1)
      assertThat(response[0].actualReleaseDate).isNull()
    }
  }
}
