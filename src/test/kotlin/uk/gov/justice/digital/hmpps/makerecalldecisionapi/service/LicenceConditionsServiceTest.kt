package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionSearch
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class LicenceConditionsServiceTest : ServiceTestBase() {

  private lateinit var licenceConditionsService: LicenceConditionsService

  @BeforeEach
  fun setup() {
    personDetailsService = PersonDetailsService(deliusClient, userAccessValidator, recommendationService)
    licenceConditionsService = LicenceConditionsService(deliusClient, personDetailsService, userAccessValidator, createAndVaryALicenceService, recommendationService, LicenceConditionsCoordinator())

    given(deliusClient.getUserAccess(anyString(), anyString()))
      .willReturn(userAccessResponse(false, false, false))
  }

  @Test
  fun `given an active conviction and licence conditions then return these details in the response`() {
    runTest {
      given(deliusClient.getLicenceConditions(anyString()))
        .willReturn(deliusLicenceConditionsResponse(listOf(custodialConviction().withLicenceConditions(licenceConditions))))

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(deliusClient).should().getLicenceConditions(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            null,
            expectedPersonDetailsResponse(),
            expectedOffenceWithLicenceConditionsResponse(licenceConditions)
          )
        )
      )
    }
  }

  @Test
  fun `given an active non custodial conviction then set custodial flag to false`() {
    runTest {
      given(deliusClient.getLicenceConditions(anyString()))
        .willReturn(deliusLicenceConditionsResponse(listOf(nonCustodialConviction().withLicenceConditions(licenceConditions))))

      val response = licenceConditionsService.getLicenceConditions(crn)

      Assertions.assertThat(response.activeConvictions[0].sentence?.isCustodial).isFalse
      then(deliusClient).should().getLicenceConditions(crn)
    }
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {

      given(deliusClient.getUserAccess(username, crn)).willReturn(excludedAccess())

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            userAccessResponse(true, false, false).copy(restrictionMessage = null), null, emptyList(), null
          )
        )
      )
    }
  }

  @Test
  fun `given user is not found for user then return user access response details`() {
    runTest {

      given(deliusClient.getUserAccess(username, crn)).willThrow(PersonNotFoundException("Not found"))

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            userAccessResponse(false, false, true).copy(restrictionMessage = null, exclusionMessage = null), null, emptyList(), null
          )
        )
      )
    }
  }

  @Test
  fun `given case is excluded for user then return user access response details V2`() {
    runTest {

      given(deliusClient.getUserAccess(username, crn)).willReturn(excludedAccess())

      val response = licenceConditionsService.getLicenceConditionsV2(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            userAccessResponse(true, false, false).copy(restrictionMessage = null), null, emptyList(), null
          )
        )
      )
    }
  }

  @Test
  fun `given user is not found for user then return user access response details V2`() {
    runTest {

      given(deliusClient.getUserAccess(username, crn)).willThrow(PersonNotFoundException("Not found"))

      val response = licenceConditionsService.getLicenceConditionsV2(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            userAccessResponse(false, false, true).copy(restrictionMessage = null, exclusionMessage = null), null, emptyList(), null
          )
        )
      )
    }
  }

  @Test
  fun `given no active licence conditions then still retrieve conviction details`() {
    runTest {
      given(deliusClient.getLicenceConditions(anyString()))
        .willReturn(deliusLicenceConditionsResponse(listOf(custodialConviction().withLicenceConditions(emptyList()))))

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(deliusClient).should().getLicenceConditions(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            null,
            expectedPersonDetailsResponse(),
            expectedOffenceWithLicenceConditionsResponse(emptyList())
          )
        )
      )
    }
  }

  @Test
  fun `given no offender details then still retrieve personal details`() {
    runTest {
      given(deliusClient.getLicenceConditions(anyString()))
        .willReturn(deliusLicenceConditionsResponse(emptyList()))

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(deliusClient).should().getLicenceConditions(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsResponse(
            null,
            expectedPersonDetailsResponse(),
            emptyList()
          )
        )
      )
    }
  }

  @Test
  fun `given licence conditions from CVL then return these details in the response`() {
    runTest {
      val licenceId = 444333
      val nomsId = "A1234CR"
      given(deliusClient.getPersonalDetails(anyString()))
        .willReturn(deliusPersonalDetailsResponse())
      given(cvlApiClient.getLicenceMatch(crn, LicenceConditionSearch(nomsId = listOf(nomsId))))
        .willReturn(Mono.fromCallable { licenceMatchedResponse(licenceId, crn) })
      given(cvlApiClient.getLicenceById(crn, licenceId))
        .willReturn(Mono.fromCallable { licenceByIdResponse() })

      val response = licenceConditionsService.getLicenceConditionsCvl(crn)

      then(deliusClient).should().getPersonalDetails(crn)
      then(cvlApiClient).should().getLicenceMatch(crn, LicenceConditionSearch(nomsId = listOf(nomsId)))
      then(cvlApiClient).should().getLicenceById(crn, licenceId)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          LicenceConditionsCvlResponse(
            null,
            expectedPersonDetailsResponse(),
            expectedCvlLicenceConditionsResponse(licenceStatus = "IN_PROGRESS")
          )
        )
      )
    }
  }
}
