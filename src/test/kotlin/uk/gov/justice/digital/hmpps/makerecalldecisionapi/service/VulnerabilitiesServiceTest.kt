package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.VulnerabilitiesResponse
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class VulnerabilitiesServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    vulnerabilitiesService = VulnerabilitiesService(arnApiClient, userAccessValidator, recommendationService, personDetailsService)
  }

  @Test
  fun `retrieves risks and formats into vulnerabilities`() {
    runTest {
      given(communityApiClient.getAllOffenderDetails(anyString())).willReturn(Mono.fromCallable { allOffenderDetailsResponse() })
      given(arnApiClient.getRisksWithFullText(anyString())).willReturn(Mono.fromCallable { riskResponse() })

      val response = vulnerabilitiesService.getVulnerabilities(crn)

      val personalDetails = response.personalDetailsOverview!!
      val vulnerabilities = response.vulnerabilities!!

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse()))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(vulnerabilities.suicide?.previous).isEqualTo("Yes")
      assertThat(vulnerabilities.suicide?.previousConcernsText).isEqualTo("Previous risk of suicide concerns due to ...")
      assertThat(vulnerabilities.suicide?.current).isEqualTo("Yes")
      assertThat(vulnerabilities.suicide?.currentConcernsText).isEqualTo("Risk of suicide concerns due to ...")
      assertThat(vulnerabilities.selfHarm?.previous).isEqualTo("Yes")
      assertThat(vulnerabilities.selfHarm?.previousConcernsText).isEqualTo("Previous risk of self harm concerns due to ...")
      assertThat(vulnerabilities.selfHarm?.current).isEqualTo("Yes")
      assertThat(vulnerabilities.selfHarm?.currentConcernsText).isEqualTo("Risk of self harm concerns due to ...")
      assertThat(vulnerabilities.vulnerability?.previous).isEqualTo("Yes")
      assertThat(vulnerabilities.vulnerability?.previousConcernsText).isEqualTo("Previous risk of vulnerability concerns due to ...")
      assertThat(vulnerabilities.vulnerability?.current).isEqualTo("Yes")
      assertThat(vulnerabilities.vulnerability?.currentConcernsText).isEqualTo("Risk of vulnerability concerns due to ...")
      assertThat(vulnerabilities.custody?.previous).isEqualTo("Yes")
      assertThat(vulnerabilities.custody?.previousConcernsText).isEqualTo("Previous risk of custody concerns due to ...")
      assertThat(vulnerabilities.custody?.current).isEqualTo("Yes")
      assertThat(vulnerabilities.custody?.currentConcernsText).isEqualTo("Risk of custody concerns due to ...")
      assertThat(vulnerabilities.hostelSetting?.previous).isEqualTo("Yes")
      assertThat(vulnerabilities.hostelSetting?.previousConcernsText).isEqualTo("Previous risk of hostel setting concerns due to ...")
      assertThat(vulnerabilities.hostelSetting?.current).isEqualTo("Yes")
      assertThat(vulnerabilities.hostelSetting?.currentConcernsText).isEqualTo("Risk of hostel setting concerns due to ...")
      assertThat(vulnerabilities.lastUpdatedDate).isEqualTo("2022-11-23T00:01:50.000Z")

      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `given case user not found for user then return user access response details`() {
    runTest {

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          404, "Not found", null, null, null
        )
      )

      val response = vulnerabilitiesService.getVulnerabilities(crn)

      then(communityApiClient).should().getUserAccess(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          VulnerabilitiesResponse(
            userAccessResponse(false, false, true).copy(restrictionMessage = null, exclusionMessage = null), null, null, null
          )
        )
      )
    }
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          403, "Forbidden", null, excludedResponse().toByteArray(), null
        )
      )

      val response = vulnerabilitiesService.getVulnerabilities(crn)

      then(communityApiClient).should().getUserAccess(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          VulnerabilitiesResponse(
            userAccessResponse(true, false, false).copy(restrictionMessage = null), null, null, null
          )
        )
      )
    }
  }

  @ParameterizedTest(name = "given call to risk fails with {1} exception then set this in the error field response")
  @CsvSource("404,NOT_FOUND", "503,SERVER_ERROR", "999, SERVER_ERROR")
  fun `given call to risk fails with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String
  ) {
    runTest {
      given(communityApiClient.getAllOffenderDetails(anyString())).willReturn(Mono.fromCallable { allOffenderDetailsResponse() })

      given(arnApiClient.getRisksWithFullText(crn)).willThrow(
        WebClientResponseException(
          code,
          null,
          null,
          null,
          null
        )
      )

      val response = vulnerabilitiesService.getVulnerabilities(crn)

      assertThat(response.vulnerabilities?.error).isEqualTo(expectedErrorCode)
    }
  }
}
