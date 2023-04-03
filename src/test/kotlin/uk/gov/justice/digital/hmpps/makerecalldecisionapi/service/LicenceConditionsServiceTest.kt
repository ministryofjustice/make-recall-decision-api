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
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions.ConvictionWithLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions.LicenceCondition
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions.LicenceConditionCategory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.cvl.LicenceConditionSearch
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsCvlResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionsResponse
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class LicenceConditionsServiceTest : ServiceTestBase() {

  private lateinit var licenceConditionsService: LicenceConditionsService

  @BeforeEach
  fun setup() {
    personDetailsService = PersonDetailsService(deliusClient, userAccessValidator, recommendationService)
    licenceConditionsService = LicenceConditionsService(deliusClient, personDetailsService, userAccessValidator, createAndVaryALicenceService, recommendationService)

    given(communityApiClient.getUserAccess(anyString()))
      .willReturn(Mono.fromCallable { userAccessResponse(false, false, false) })
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

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          403, "Forbidden", null, excludedResponse().toByteArray(), null
        )
      )

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getUserAccess(crn)

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

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          404, "Forbidden", null, null, null
        )
      )

      val response = licenceConditionsService.getLicenceConditions(crn)

      then(communityApiClient).should().getUserAccess(crn)

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
            expectedCvlLicenceConditionsResponse()
          )
        )
      )
    }
  }

  private fun expectedOffenceWithLicenceConditionsResponse(licenceConditions: List<LicenceCondition>): List<ConvictionWithLicenceConditions> {
    return listOf(
      ConvictionWithLicenceConditions(
        number = "1",
        mainOffence = Offence(
          description = "Robbery (other than armed robbery)",
          code = "ABC123",
          date = LocalDate.parse("2022-08-26")
        ),
        additionalOffences = listOf(
          Offence(
            description = "Arson",
            code = "ZYX789",
            date = LocalDate.parse("2022-08-26")
          )
        ),
        sentence = Sentence(
          description = "CJA - Extended Sentence",
          isCustodial = true,
          custodialStatusCode = "ABC123",
          length = 6,
          lengthUnits = "Days",
          sentenceExpiryDate = LocalDate.parse("2022-06-10"),
          licenceExpiryDate = LocalDate.parse("2022-05-10"),
        ),
        licenceConditions = licenceConditions,
      )
    )
  }

  private val licenceConditions = listOf(
    LicenceCondition(
      notes = "Licence condition notes",
      mainCategory = LicenceConditionCategory(
        code = "NLC8",
        description = "Freedom of movement"
      ),
      subCategory = LicenceConditionCategory(
        code = "NSTT8",
        description = "To only attend places of worship which have been previously agreed with your supervising officer."
      )
    )
  )

  private fun expectedCvlLicenceConditionsResponse(): List<LicenceConditionResponse> {

    return listOf(
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
        additionalLicenceConditions = listOf(
          LicenceConditionDetail(
            text = "This is an additional licence condition",
            expandedText = "Expanded additional licence condition"
          )
        ),
        additionalPssConditions = listOf(
          LicenceConditionDetail(
            text = "This is an additional PSS licence condition",
            expandedText = "Expanded additional PSS licence condition"
          )
        ),
        bespokeConditions = listOf(LicenceConditionDetail(text = "This is a bespoke condition"))
      )
    )
  }
}
