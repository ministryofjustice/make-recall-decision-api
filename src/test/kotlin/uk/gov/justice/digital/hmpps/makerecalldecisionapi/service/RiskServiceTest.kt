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
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CodeDescriptionItem
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.MappaResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Officer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProbationArea
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Registration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.RegistrationsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentOffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GroupReconvictionScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.SexualPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ViolencePredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.SCORE_NOT_APPLICABLE
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RiskServiceTest : ServiceTestBase() {

  @Mock
  lateinit var templateReplacementService2: TemplateReplacementService

  @BeforeEach
  fun setup() {
    recommendationService = RecommendationService(
      recommendationRepository,
      personDetailsService,
      templateReplacementService2,
      userAccessValidator,
      null,
      deliusClient,
      null
    )
    riskService = RiskService(deliusClient, arnApiClient, userAccessValidator, recommendationService)
  }

  @Test
  fun `retrieves risk`() {
    runTest {
      given(deliusClient.getMappaAndRoshHistory(anyString()))
        .willReturn(deliusMappaAndRoshHistoryResponse())
      given(arnApiClient.getRiskSummary(anyString()))
        .willReturn(Mono.fromCallable { riskSummaryResponse })
      given(arnApiClient.getRiskScores(anyString()))
        .willReturn(
          Mono.fromCallable {
            listOf(
              currentRiskScoreResponse,
              historicalRiskScoreResponse,
              historicalRiskScoreResponse.copy(completedDate = "2016-09-12T12:00:00.000")
            )
          }
        )
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { AssessmentsResponse(crn, false, listOf(assessment(), assessment().copy(dateCompleted = null))) })

      val response = riskService.getRisk(crn)

      val personalDetails = response.personalDetailsOverview!!
      val riskOfSeriousHarm = response.roshSummary?.riskOfSeriousHarm!!
      val mappa = response.mappa!!
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current
      val roshHistory = response.roshHistory
      assertThat(roshHistory?.registrations?.get(0)?.active).isEqualTo(true)
      assertThat(roshHistory?.registrations?.get(0)?.type?.code).isEqualTo("ABC123")
      assertThat(roshHistory?.registrations?.get(0)?.type?.description).isEqualTo("Victim contact")
      assertThat(roshHistory?.registrations?.get(0)?.startDate).isEqualTo("2021-01-30")
      assertThat(roshHistory?.registrations?.get(0)?.notes).isEqualTo("Notes on case")
      assertThat(roshHistory?.registrations?.size).isEqualTo(1)
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(deliusPersonalDetailsResponse()))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(riskOfSeriousHarm.overallRisk).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToChildren).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToPublic).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToKnownAdult).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToStaff).isEqualTo("MEDIUM")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToPrisoners).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToChildren).isEqualTo("LOW")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToPublic).isEqualTo("LOW")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToKnownAdult).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToStaff).isEqualTo("VERY_HIGH")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToPrisoners).isEqualTo("VERY_HIGH")
      assertThat(mappa.level).isEqualTo(1)
      assertThat(mappa.category).isEqualTo(0)
      assertThat(mappa.lastUpdatedDate).isEqualTo("2021-02-10")
      assertThat(response.roshSummary?.lastUpdatedDate).isEqualTo("2022-10-09T08:26:31.000Z")
      assertThat(response.roshSummary?.natureOfRisk).isEqualTo("The nature of the risk is X")
      assertThat(response.roshSummary?.whoIsAtRisk).isEqualTo("X, Y and Z are at risk")
      assertThat(response.roshSummary?.riskIncreaseFactors).isEqualTo("If offender in situation X the risk can be higher")
      assertThat(response.roshSummary?.riskMitigationFactors).isEqualTo("Giving offender therapy in X will reduce the risk")
      assertThat(response.roshSummary?.riskImminence).isEqualTo("the risk is imminent and more probably in X situation")
      assertThat(historicalScores?.get(0)?.date).isEqualTo("2018-09-12")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.level).isEqualTo("MEDIUM")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.score).isEqualTo("2")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.type).isEqualTo("RSR")
      assertThat(historicalScores?.get(0)?.scores?.ospc?.level).isEqualTo("HIGH")
      assertThat(historicalScores?.get(0)?.scores?.ospc?.score).isEqualTo(null)
      assertThat(historicalScores?.get(0)?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.level).isEqualTo("MEDIUM")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.score).isEqualTo(null)
      assertThat(historicalScores?.get(0)?.scores?.ospi?.type).isEqualTo("OSP/I")
      assertThat(historicalScores?.get(0)?.scores?.ovp?.oneYear).isEqualTo("0")
      assertThat(historicalScores?.get(0)?.scores?.ovp?.twoYears).isEqualTo("0")
      assertThat(historicalScores?.get(0)?.scores?.ogp?.oneYear).isEqualTo("0")
      assertThat(historicalScores?.get(0)?.scores?.ogp?.twoYears).isEqualTo("0")
      assertThat(historicalScores?.get(0)?.scores?.ogrs?.level).isEqualTo("HIGH")
      assertThat(historicalScores?.get(0)?.scores?.ogrs?.oneYear).isEqualTo("0")
      assertThat(historicalScores?.get(0)?.scores?.ogrs?.twoYears).isEqualTo("0")
      assertThat(historicalScores?.get(0)?.scores?.ogrs?.type).isEqualTo("OGRS")
      assertThat(historicalScores?.get(1)?.date).isEqualTo("2017-09-12")
      assertThat(historicalScores?.get(1)?.scores?.ospc?.score).isEqualTo(null)
      assertThat(historicalScores?.get(1)?.scores?.ospc?.level).isEqualTo("HIGH")
      assertThat(historicalScores?.get(1)?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(historicalScores?.get(1)?.scores?.ospi?.score).isEqualTo(null)
      assertThat(historicalScores?.get(1)?.scores?.ospi?.level).isEqualTo("MEDIUM")
      assertThat(historicalScores?.get(1)?.scores?.ospi?.type).isEqualTo("OSP/I")
      assertThat(historicalScores?.get(1)?.scores?.ogp?.oneYear).isEqualTo("0")
      assertThat(historicalScores?.get(1)?.scores?.ogp?.twoYears).isEqualTo("0")
      assertThat(historicalScores?.get(1)?.scores?.ogp?.level).isEqualTo("LOW")
      assertThat(historicalScores?.get(1)?.scores?.rsr?.score).isEqualTo("1")
      assertThat(historicalScores?.get(1)?.scores?.rsr?.level).isEqualTo("LOW")
      assertThat(historicalScores?.get(1)?.scores?.ovp?.level).isEqualTo("LOW")
      assertThat(historicalScores?.get(1)?.scores?.ovp?.oneYear).isEqualTo("0")
      assertThat(historicalScores?.get(1)?.scores?.ovp?.twoYears).isEqualTo("0")
      assertThat(historicalScores?.get(1)?.scores?.ogrs?.level).isEqualTo("HIGH")
      assertThat(historicalScores?.get(1)?.scores?.ogrs?.type).isEqualTo("OGRS")
      assertThat(historicalScores?.get(1)?.scores?.ogrs?.oneYear).isEqualTo("0")
      assertThat(historicalScores?.get(1)?.scores?.ogrs?.twoYears).isEqualTo("0")
      assertThat(historicalScores?.get(2)?.date).isEqualTo("2016-09-12")
      assertThat(historicalScores?.get(2)?.scores?.ospc?.score).isEqualTo(null)
      assertThat(historicalScores?.get(2)?.scores?.ospc?.level).isEqualTo("HIGH")
      assertThat(historicalScores?.get(2)?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(historicalScores?.get(2)?.scores?.ospi?.score).isEqualTo(null)
      assertThat(historicalScores?.get(2)?.scores?.ospi?.level).isEqualTo("MEDIUM")
      assertThat(historicalScores?.get(2)?.scores?.ospi?.type).isEqualTo("OSP/I")
      assertThat(historicalScores?.get(2)?.scores?.ogp?.oneYear).isEqualTo("0")
      assertThat(historicalScores?.get(2)?.scores?.ogp?.twoYears).isEqualTo("0")
      assertThat(historicalScores?.get(2)?.scores?.ogp?.level).isEqualTo("LOW")
      assertThat(historicalScores?.get(2)?.scores?.rsr?.score).isEqualTo("1")
      assertThat(historicalScores?.get(2)?.scores?.rsr?.level).isEqualTo("LOW")
      assertThat(historicalScores?.get(2)?.scores?.ovp?.level).isEqualTo("LOW")
      assertThat(historicalScores?.get(2)?.scores?.ovp?.oneYear).isEqualTo("0")
      assertThat(historicalScores?.get(2)?.scores?.ovp?.twoYears).isEqualTo("0")
      assertThat(historicalScores?.get(2)?.scores?.ogrs?.level).isEqualTo("HIGH")
      assertThat(historicalScores?.get(2)?.scores?.ogrs?.type).isEqualTo("OGRS")
      assertThat(historicalScores?.get(2)?.scores?.ogrs?.oneYear).isEqualTo("0")
      assertThat(historicalScores?.get(2)?.scores?.ogrs?.twoYears).isEqualTo("0")
      assertThat(currentScores?.date).isEqualTo("2018-09-12")
      assertThat(currentScores?.scores?.rsr?.level).isEqualTo("MEDIUM")
      assertThat(currentScores?.scores?.rsr?.score).isEqualTo("2")
      assertThat(currentScores?.scores?.rsr?.type).isEqualTo("RSR")
      assertThat(currentScores?.scores?.ospc?.level).isEqualTo("HIGH")
      assertThat(currentScores?.scores?.ospc?.score).isEqualTo(null)
      assertThat(currentScores?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(currentScores?.scores?.ospi?.level).isEqualTo("MEDIUM")
      assertThat(currentScores?.scores?.ospi?.score).isEqualTo(null)
      assertThat(currentScores?.scores?.ospi?.type).isEqualTo("OSP/I")
      assertThat(currentScores?.scores?.ovp?.oneYear).isEqualTo("0")
      assertThat(currentScores?.scores?.ovp?.twoYears).isEqualTo("0")
      assertThat(currentScores?.scores?.ogp?.oneYear).isEqualTo("0")
      assertThat(currentScores?.scores?.ogp?.twoYears).isEqualTo("0")
      assertThat(currentScores?.scores?.ogrs?.level).isEqualTo("HIGH")
      assertThat(currentScores?.scores?.ogrs?.oneYear).isEqualTo("0")
      assertThat(currentScores?.scores?.ogrs?.twoYears).isEqualTo("0")
      assertThat(currentScores?.scores?.ogrs?.type).isEqualTo("OGRS")
      assertThat(response.assessmentStatus).isEqualTo("COMPLETE")

      then(arnApiClient).should().getAssessments(crn)
      then(arnApiClient).should().getRiskSummary(crn)
      then(arnApiClient).should().getRiskScores(crn)
      then(deliusClient).should().getMappaAndRoshHistory(crn)
    }
  }

  @Test
  fun `retrieves risk when assessment status is incomplete`() {
    runTest {
      given(deliusClient.getMappaAndRoshHistory(anyString()))
        .willReturn(deliusMappaAndRoshHistoryResponse())
      given(arnApiClient.getRiskSummary(anyString()))
        .willReturn(Mono.fromCallable { riskSummaryResponse })
      given(arnApiClient.getRiskScores(anyString()))
        .willReturn(Mono.fromCallable { listOf(currentRiskScoreResponse) })
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { assessmentResponse(crn).copy(assessments = listOf(assessment().copy(superStatus = "BLA"))) })

      val response = riskService.getRisk(crn)

      assertThat(response.assessmentStatus).isEqualTo("INCOMPLETE")
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves assessments information when hideOffenceDetailsWhenNoMatch is false and at least one active conviction from Delius with a main offence matches the offence from OaSys`() {
    runTest {
      // given
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { AssessmentsResponse(crn, false, listOf(assessment().copy(laterCompleteAssessmentExists = false))) })
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions, hideOffenceDetailsWhenNoMatch = false)

      // then
      val shouldBeTrueBecauseNoLaterCompleteAssessmentExists = response?.offenceDataFromLatestCompleteAssessment
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(shouldBeTrueBecauseNoLaterCompleteAssessmentExists).isEqualTo(true)
      assertThat(response?.offencesMatch).isEqualTo(true)
      assertThat(response?.offenceDescription).isEqualTo("Juicy offence details.")
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves assessments information and returns not found error`() {
    runTest {
      // given
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { AssessmentsResponse(crn, false, listOf(assessment().copy(laterCompleteAssessmentExists = false, offence = null))) })
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions, hideOffenceDetailsWhenNoMatch = false)

      // then
      val shouldBeTrueBecauseNoLaterCompleteAssessmentExists = response?.offenceDataFromLatestCompleteAssessment
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(shouldBeTrueBecauseNoLaterCompleteAssessmentExists).isEqualTo(true)
      assertThat(response?.offencesMatch).isEqualTo(true)
      assertThat(response?.offenceDescription).isEqualTo(null)
      assertThat(response?.error).isEqualTo("NOT_FOUND")
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves assessments when hideOffenceDetailsWhenNoMatch is true and offences do not match as a later complete assessment exists`() {
    runTest {
      // given
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { AssessmentsResponse(crn, false, listOf(assessment().copy(laterCompleteAssessmentExists = true))) })
      val convictions = listOf(convictionResponse())

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions, hideOffenceDetailsWhenNoMatch = true)

      // then
      val shouldBeFalseBecauseLaterCompleteAssessmentExists = response?.offenceDataFromLatestCompleteAssessment
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(shouldBeFalseBecauseLaterCompleteAssessmentExists).isEqualTo(false)
      assertThat(response?.offencesMatch).isEqualTo(true)
      assertThat(response?.offenceDescription).isEqualTo(null)
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves assessments when hideOffenceDetailsWhenNoMatch is false and offences do not match as no active conviction with main offence exists`() {
    runTest {
      // given
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { AssessmentsResponse(crn, false, listOf(assessment())) })

      // when
      val response = riskService.fetchAssessmentInfo(crn, emptyList(), hideOffenceDetailsWhenNoMatch = false)

      // then
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(response?.offenceDataFromLatestCompleteAssessment).isEqualTo(true)
      assertThat(response?.offencesMatch).isEqualTo(false)
      assertThat(response?.offenceDescription).isEqualTo("Juicy offence details.")
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves assessments when hideOffenceDetailsWhenNoMatch is false and offences do not match as a later complete assessment exists`() {
    runTest {
      // given
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { AssessmentsResponse(crn, false, listOf(assessment().copy(laterCompleteAssessmentExists = true))) })

      // when
      val response = riskService.fetchAssessmentInfo(crn, listOf(convictionResponse()), hideOffenceDetailsWhenNoMatch = true)

      // then
      val shouldBeFalseBecauseLaterCompleteAssessmentExists = response?.offenceDataFromLatestCompleteAssessment
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(shouldBeFalseBecauseLaterCompleteAssessmentExists).isEqualTo(false)
      assertThat(response?.offencesMatch).isEqualTo(true)
      assertThat(response?.offenceDescription).isEqualTo(null)
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves assessments when hideOffenceDetailsWhenNoMatch is false and offences do not match because just the dates do not match`() {
    runTest {
      // given
      val thisDateDoesNotMatchDateInDelius = "2000-08-26T12:00:00.000"
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(
          Mono.fromCallable {
            AssessmentsResponse(
              crn,
              false,
              listOf(
                assessment().copy(
                  offenceDetails = listOf(
                    AssessmentOffenceDetail(
                      type = "CURRENT",
                      offenceCode = "ABC123",
                      offenceSubCode = "",
                      offenceDate = thisDateDoesNotMatchDateInDelius
                    )
                  )
                ),
                assessment().copy(
                  laterCompleteAssessmentExists = true,
                  dateCompleted = "2022-08-26T15:00:08",
                  superStatus = "OPEN"
                )
              )
            )
          }
        )
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions, hideOffenceDetailsWhenNoMatch = false)

      // then
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(response?.offenceDataFromLatestCompleteAssessment).isEqualTo(true)
      assertThat(response?.offencesMatch).isEqualTo(false)
      assertThat(response?.offenceDescription).isEqualTo("Juicy offence details.")
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves assessments when hideOffenceDetailsWhenNoMatch is false and offences do not match because a later complete assessment exists and the dates and codes do not match`() {
    runTest {
      // given
      val thisDateDoesNotMatchDateInDelius = "2000-08-26T12:00:00.000"
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(
          Mono.fromCallable {
            AssessmentsResponse(
              crn,
              false,
              listOf(
                assessment().copy(
                  laterCompleteAssessmentExists = true,
                  offenceDetails = listOf(
                    AssessmentOffenceDetail(
                      type = "CURRENT",
                      offenceCode = "NO_MATCH",
                      offenceSubCode = "",
                      offenceDate = thisDateDoesNotMatchDateInDelius
                    )
                  )
                ),
                assessment().copy(
                  laterCompleteAssessmentExists = true,
                  dateCompleted = "2022-08-26T15:00:08",
                  superStatus = "OPEN"
                )
              )
            )
          }
        )
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions, hideOffenceDetailsWhenNoMatch = false)

      // then
      val shouldBeFalseBecauseLaterCompleteAssessmentExists = response?.offenceDataFromLatestCompleteAssessment
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(shouldBeFalseBecauseLaterCompleteAssessmentExists).isEqualTo(false)
      assertThat(response?.offencesMatch).isEqualTo(false)
      assertThat(response?.offenceDescription).isEqualTo("Juicy offence details.")
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves assessments when hideOffenceDetailsWhenNoMatch is false and offences do not match because offence date from OaSys is null`() {
    runTest {
      // given
      val nullDateShouldBeHandledAsMismatch = null
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(
          Mono.fromCallable {
            AssessmentsResponse(
              crn,
              false,
              listOf(
                assessment().copy(
                  offenceDetails = listOf(
                    AssessmentOffenceDetail(
                      type = "CURRENT",
                      offenceCode = "ABC123",
                      offenceSubCode = "",
                      offenceDate = nullDateShouldBeHandledAsMismatch
                    )
                  )
                ),
                assessment().copy(
                  laterCompleteAssessmentExists = true,
                  dateCompleted = "2022-08-26T15:00:08",
                  superStatus = "OPEN"
                )
              )
            )
          }
        )
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions, hideOffenceDetailsWhenNoMatch = false)

      // then
      val shouldBeFalseBecauseLaterCompleteAssessmentExists = response?.offenceDataFromLatestCompleteAssessment
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(response?.offenceDataFromLatestCompleteAssessment).isEqualTo(true)
      assertThat(response?.offencesMatch).isEqualTo(false)
      assertThat(response?.offenceDescription).isEqualTo("Juicy offence details.")
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves risk with null predictor score field`() {
    runTest {
      given(arnApiClient.getRiskScores(anyString()))
        .willReturn(
          Mono.fromCallable {
            listOf(
              currentRiskScoreResponseWithOptionalFields,
              historicalRiskScoreResponseWhereValuesNull
            )
          }
        )
      apiMocksWithAllFieldsEmpty()

      val response = riskService.getRisk(crn)
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current

      assertThat(historicalScores).isEmpty()
      assertThat(currentScores?.date).isEqualTo(null)
      assertThat(currentScores?.scores?.rsr).isEqualTo(null)
      assertThat(currentScores?.scores?.ospc).isEqualTo(null)
      assertThat(currentScores?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.scores?.ogrs).isEqualTo(null)
      assertThat(response.assessmentStatus).isEqualTo("INCOMPLETE")
      assertThat(response.roshSummary?.error).isEqualTo("MISSING_DATA")

      then(arnApiClient).should().getAssessments(crn)
      then(arnApiClient).should().getRiskScores(crn)
      then(arnApiClient).should().getRiskSummary(crn)
      then(deliusClient).should().getMappaAndRoshHistory(crn)
    }
  }

  @Test
  fun `retrieves risk with null scores except rsr`() {
    runTest {
      given(arnApiClient.getRiskScores(anyString()))
        .willReturn(
          Mono.fromCallable {
            listOf(
              currentRiskScoreResponseWithOptionalFields.copy(riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore("10", "MEDIUM")),
              historicalRiskScoreResponseWhereValuesNull.copy(riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore("10", "MEDIUM"))
            )
          }
        )
      apiMocksWithAllFieldsEmpty()

      val response = riskService.getRisk(crn)
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current

      assertThat(historicalScores?.get(0)?.date).isEqualTo("2018-09-12")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.score).isEqualTo("10")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.level).isEqualTo("MEDIUM")
      assertThat(historicalScores?.get(0)?.scores?.ospc).isEqualTo(null)
      assertThat(historicalScores?.get(0)?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.date).isEqualTo("2018-09-12")
      assertThat(currentScores?.scores?.rsr?.score).isEqualTo("10")
      assertThat(currentScores?.scores?.rsr?.level).isEqualTo("MEDIUM")
      assertThat(currentScores?.scores?.ospc).isEqualTo(null)
      assertThat(currentScores?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.scores?.ogrs).isEqualTo(null)
      assertThat(response.assessmentStatus).isEqualTo("INCOMPLETE")
      assertThat(response.roshSummary?.error).isEqualTo("MISSING_DATA")

      then(arnApiClient).should().getAssessments(crn)
      then(arnApiClient).should().getRiskScores(crn)
      then(arnApiClient).should().getRiskSummary(crn)
      then(deliusClient).should().getMappaAndRoshHistory(crn)
    }
  }

  @Test
  fun `retrieves risk with empty OSPC risk score values`() {
    runTest {
      given(arnApiClient.getRiskScores(anyString()))
        .willReturn(
          Mono.fromCallable {
            listOf(
              currentRiskScoreResponseWithOptionalFields,
              historicalRiskScoreResponseWhereValuesNull.copy(
                sexualPredictorScore = SexualPredictorScore(
                  ospIndecentPercentageScore = "0",
                  ospContactPercentageScore = "0",
                  ospIndecentScoreLevel = SCORE_NOT_APPLICABLE,
                  ospContactScoreLevel = SCORE_NOT_APPLICABLE
                )
              )
            )
          }
        )
      apiMocksWithAllFieldsEmpty()

      val response = riskService.getRisk(crn)
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current

      assertThat(historicalScores).isEmpty()
      assertThat(currentScores?.date).isEqualTo(null)
      assertThat(currentScores?.scores?.rsr).isEqualTo(null)
      assertThat(currentScores?.scores?.ospc).isEqualTo(null)
      assertThat(currentScores?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.scores?.ogrs).isEqualTo(null)
      assertThat(response.assessmentStatus).isEqualTo("INCOMPLETE")

      then(arnApiClient).should().getAssessments(crn)
      then(arnApiClient).should().getRiskScores(crn)
      then(arnApiClient).should().getRiskSummary(crn)
      then(deliusClient).should().getMappaAndRoshHistory(crn)
    }
  }

  @Test
  fun `retrieves risk with optional fields missing`() {
    runTest {
      given(arnApiClient.getRiskScores(anyString()))
        .willReturn(
          Mono.fromCallable {
            listOf(
              currentRiskScoreResponseWithOptionalFields,
              historicalRiskScoreResponseWithOptionalFields
            )
          }
        )
      apiMocksWithAllFieldsEmpty()

      val response = riskService.getRisk(crn)

      val personalDetails = response.personalDetailsOverview!!
      val mappa = response.mappa
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current

      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(mappa).isEqualTo(null)
      assertThat(response.roshSummary?.error).isEqualTo("MISSING_DATA")
      assertThat(historicalScores).isEmpty()
      assertThat(currentScores?.date).isEqualTo(null)
      assertThat(currentScores?.scores?.rsr).isEqualTo(null)
      assertThat(currentScores?.scores?.ospc).isEqualTo(null)
      assertThat(currentScores?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.scores?.ogrs).isEqualTo(null)
      assertThat(response.assessmentStatus).isEqualTo("INCOMPLETE")

      then(arnApiClient).should().getAssessments(crn)
      then(arnApiClient).should().getRiskScores(crn)
      then(arnApiClient).should().getRiskSummary(crn)
      then(deliusClient).should().getMappaAndRoshHistory(crn)
    }
  }

  private fun apiMocksWithAllFieldsEmpty() {
    given(arnApiClient.getAssessments(anyString()))
      .willReturn(
        Mono.fromCallable {
          AssessmentsResponse(
            crn = null,
            limitedAccessOffender = null,
            assessments = emptyList()
          )
        }
      )
    given(arnApiClient.getRiskSummary(anyString()))
      .willReturn(
        Mono.fromCallable {
          riskSummaryResponse.copy(
            whoIsAtRisk = null,
            natureOfRisk = null,
            riskImminence = null,
            riskIncreaseFactors = null,
            riskMitigationFactors = null,
            riskInCommunity = RiskScore(
              veryHigh = null,
              high = null,
              medium = null,
              low = null
            ),
            riskInCustody = RiskScore(
              veryHigh = null,
              high = null,
              medium = null,
              low = null
            ),
            assessedOn = ("2022-10-09T08:26:31"),
            overallRiskLevel = null
          )
        }
      )

    given(deliusClient.getMappaAndRoshHistory(anyString()))
      .willReturn(deliusMappaAndRoshHistoryResponse(mappa = null, roshHistory = emptyList()))
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {

      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(excludedAccess())

      val response = riskService.getRisk(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          RiskResponse(
            userAccessResponse(true, false, false).copy(restrictionMessage = null), null, null, null, null
          )
        )
      )
    }
  }

  @Test
  fun `given user not found then return user access response details`() {
    runTest {

      given(deliusClient.getUserAccess(anyString(), anyString())).willThrow(PersonNotFoundException("Not found"))

      val response = riskService.getRisk(crn)

      then(deliusClient).should().getUserAccess(username, crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          RiskResponse(
            userAccessResponse(false, false, true).copy(exclusionMessage = null, restrictionMessage = null), null, null, null, null
          )
        )
      )
    }
  }

  @ParameterizedTest()
  @CsvSource(
    "COMPLETE, true",
    "INCOMPLETE, false"
  )
  fun `given multiple risk management plans then return latest with assessment status set`(
    status: String,
    assessmentStatusComplete: Boolean
  ) {
    runTest {

      given(arnApiClient.getRiskManagementPlan(anyString()))
        .willReturn(
          Mono.fromCallable {
            riskManagementResponse(crn, status)
          }
        )
      val response = riskService.getLatestRiskManagementPlan(crn)

      assertThat(response.contingencyPlans).isEqualTo("I am the contingency plan text")
      assertThat(response.latestDateCompleted).isEqualTo("2022-10-07T14:20:27.000Z")
      assertThat(response.initiationDate).isEqualTo("2022-10-02T14:20:27.000Z")
      assertThat(response.lastUpdatedDate).isEqualTo("2022-10-01T14:20:27.000Z")
      assertThat(response.assessmentStatusComplete).isEqualTo(assessmentStatusComplete)
    }
  }

  @Test
  fun `given risk management plan completed date not set then use initiation date`() {
    runTest {

      given(arnApiClient.getRiskManagementPlan(anyString()))
        .willReturn(
          Mono.fromCallable {
            riskManagementResponse(crn, "COMPLETE", null)
          }
        )
      val response = riskService.getLatestRiskManagementPlan(crn)

      assertThat(response.contingencyPlans).isEqualTo("I am the contingency plan text")
      assertThat(response.latestDateCompleted).isEqualTo("2022-10-07T14:20:27.000Z")
      assertThat(response.initiationDate).isEqualTo("2022-10-02T14:20:27.000Z")
      assertThat(response.lastUpdatedDate).isEqualTo("2022-10-02T14:20:27.000Z")
      assertThat(response.assessmentStatusComplete).isEqualTo(true)
    }
  }

  @ParameterizedTest(name = "given call to fetch risk assessments fails with {1} exception then set this in the error field response")
  @CsvSource("404,NOT_FOUND", "503,SERVER_ERROR", "999, SERVER_ERROR")
  fun `given call to fetch risk assessments fails on call to arn api with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String
  ) {
    runTest {
      given(arnApiClient.getAssessments(crn)).willThrow(WebClientResponseException(code, null, null, null, null))

      val response = riskService.fetchAssessmentInfo(crn, emptyList(), true)

      assertThat(response?.error).isEqualTo(expectedErrorCode)
    }
  }

  @ParameterizedTest(name = "given call to fetch risk scores fails with {1} exception then set this in the error field response")
  @CsvSource("404,NOT_FOUND", "503,SERVER_ERROR", "999, SERVER_ERROR")
  fun `given call to fetch risk scores fails with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String
  ) {
    runTest {
      given(arnApiClient.getRiskScores(crn)).willThrow(WebClientResponseException(code, null, null, null, null))

      val response = riskService.fetchPredictorScores(crn)

      assertThat(response.error).isEqualTo(expectedErrorCode)
    }
  }

  @ParameterizedTest(name = "given call to fetch risk summary fails with {1} exception then set this in the error field response")
  @CsvSource("404,Offender not found for CRN,NOT_FOUND", "404,'Latest COMPLETE with types [LAYER_1, LAYER_3] type not found for crn X12345',NOT_FOUND_LATEST_COMPLETE", "503,This is a server error,SERVER_ERROR", "999,Random error,SERVER_ERROR")
  fun `given call to fetch risk summary fails with given exception then set this in the error field response`(
    code: Int,
    exceptionMessage: String,
    expectedErrorCode: String
  ) {
    runTest {
      given(arnApiClient.getRiskSummary(crn)).willThrow(WebClientResponseException(code, null, null, exceptionMessage.toByteArray(Charsets.UTF_8), null))

      val response = riskService.getRoshSummary(crn)

      assertThat(response.error).isEqualTo(expectedErrorCode)
    }
  }

  @ParameterizedTest(name = "given call to risk management plan fails with {1} exception then set this in the error field response")
  @CsvSource("404,NOT_FOUND", "503,SERVER_ERROR", "999, SERVER_ERROR")
  fun `given call to risk management plan fails with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String
  ) {
    runTest {
      given(arnApiClient.getRiskManagementPlan(crn)).willThrow(WebClientResponseException(code, null, null, null, null))

      val response = riskService.getLatestRiskManagementPlan(crn)

      assertThat(response.error).isEqualTo(expectedErrorCode)
    }
  }

  private val riskSummaryResponse = RiskSummaryResponse(
    whoIsAtRisk = "X, Y and Z are at risk",
    natureOfRisk = "The nature of the risk is X",
    riskImminence = "the risk is imminent and more probably in X situation",
    riskIncreaseFactors = "If offender in situation X the risk can be higher",
    riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
    riskInCommunity = RiskScore(
      veryHigh = null,
      high = listOf(
        "Children",
        "Public",
        "Known adult"
      ),
      medium = listOf("Staff"),
      low = null
    ),
    riskInCustody = RiskScore(
      veryHigh = listOf(
        "Staff",
        "Prisoners"
      ),
      high = listOf("Known adult"),
      medium = null,
      low = listOf(
        "Children",
        "Public"
      )
    ),
    assessedOn = "2022-10-09T08:26:31",
    overallRiskLevel = "HIGH"
  )

  private val currentRiskScoreResponse = RiskScoreResponse(
    completedDate = "2018-09-12T12:00:00.000",
    generalPredictorScore = GeneralPredictorScore(
      ogpStaticWeightedScore = "",
      ogpDynamicWeightedScore = "",
      ogpTotalWeightedScore = "1",
      ogpRisk = "LOW",
      ogp1Year = "0",
      ogp2Year = "0"
    ),
    riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "2", scoreLevel = "MEDIUM"),
    sexualPredictorScore = SexualPredictorScore(
      ospIndecentPercentageScore = "3",
      ospContactPercentageScore = "2",
      ospIndecentScoreLevel = "MEDIUM",
      ospContactScoreLevel = "HIGH"
    ),
    groupReconvictionScore = GroupReconvictionScore(oneYear = "0", twoYears = "0", scoreLevel = "HIGH"),
    violencePredictorScore = ViolencePredictorScore(
      ovpStaticWeightedScore = "0",
      ovpDynamicWeightedScore = "0",
      ovpTotalWeightedScore = "0",
      ovpRisk = "LOW",
      oneYear = "0",
      twoYears = "0"
    )
  )

  private val historicalRiskScoreResponse = RiskScoreResponse(
    completedDate = "2017-09-12T12:00:00.000",
    generalPredictorScore = GeneralPredictorScore(
      ogpStaticWeightedScore = "",
      ogpDynamicWeightedScore = "10",
      ogpTotalWeightedScore = "1",
      ogpRisk = "LOW",
      ogp1Year = "0",
      ogp2Year = "0"
    ),
    riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "1", scoreLevel = "LOW"),
    sexualPredictorScore = SexualPredictorScore(
      ospIndecentPercentageScore = "3",
      ospContactPercentageScore = "2",
      ospIndecentScoreLevel = "MEDIUM",
      ospContactScoreLevel = "HIGH"
    ),
    groupReconvictionScore = GroupReconvictionScore(oneYear = "0", twoYears = "0", scoreLevel = "HIGH"),
    violencePredictorScore = ViolencePredictorScore(
      ovpStaticWeightedScore = "0",
      ovpDynamicWeightedScore = "0",
      ovpTotalWeightedScore = "0",
      ovpRisk = "LOW",
      oneYear = "0",
      twoYears = "0"
    )
  )

  private val historicalRiskScoreResponseWhereValuesNull = RiskScoreResponse(
    completedDate = "2017-09-12T12:00:00.000",
    generalPredictorScore = GeneralPredictorScore(
      ogpStaticWeightedScore = null,
      ogpDynamicWeightedScore = null,
      ogpTotalWeightedScore = null,
      ogpRisk = null,
      ogp1Year = null,
      ogp2Year = null
    ),
    riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = null, scoreLevel = null),
    sexualPredictorScore = SexualPredictorScore(
      ospIndecentPercentageScore = null,
      ospContactPercentageScore = null,
      ospIndecentScoreLevel = null,
      ospContactScoreLevel = null
    ),
    groupReconvictionScore = GroupReconvictionScore(oneYear = null, twoYears = null, scoreLevel = null),
    violencePredictorScore = ViolencePredictorScore(
      ovpStaticWeightedScore = null,
      ovpDynamicWeightedScore = null,
      ovpTotalWeightedScore = null,
      ovpRisk = null,
      oneYear = null,
      twoYears = null
    )
  )

  private val currentRiskScoreResponseWithOptionalFields = RiskScoreResponse(
    completedDate = "2018-09-12T12:00:00.000",
    generalPredictorScore = null,
    riskOfSeriousRecidivismScore = null,
    sexualPredictorScore = null,
    groupReconvictionScore = null,
    violencePredictorScore = null
  )

  private val historicalRiskScoreResponseWithOptionalFields = RiskScoreResponse(
    completedDate = "2017-09-12T12:00:00.000",
    generalPredictorScore = null,
    riskOfSeriousRecidivismScore = null,
    sexualPredictorScore = null,
    groupReconvictionScore = null,
    violencePredictorScore = null
  )

  private val registrationsResponse = RegistrationsResponse(
    registrations = listOf(
      Registration(
        registrationId = "2500064995",
        active = true,
        register = CodeDescriptionItem(code = "1", description = "RoSH"),
        type = CodeDescriptionItem(code = "ABC123", description = "Victim contact"),
        startDate = LocalDate.parse("2021-01-30"),
        notes = "Notes on case"
      ),
      Registration(
        registrationId = "2500064995",
        active = false,
        register = CodeDescriptionItem(code = "ABC123", description = "Some description"),
        type = CodeDescriptionItem(code = "ABC124", description = "Mental health issues"),
        startDate = LocalDate.parse("2021-01-30"),
        notes = "string"
      )
    )
  )

  private val mappaResponse = MappaResponse(
    level = 1,
    levelDescription = "MAPPA Level 1",
    category = 0,
    categoryDescription = "All - Category to be determined",
    startDate = LocalDate.parse("2021-02-10"),
    reviewDate = LocalDate.parse("2021-05-10"),
    team = Team(
      code = "N07CHT",
      description = "Automation SPG",
      emailAddress = null,
      telephone = null,
      localDeliveryUnit = null
    ),
    officer = Officer(
      code = "N07A060",
      forenames = "NDelius26",
      surname = "Anderson",
      unallocated = false
    ),
    probationArea = ProbationArea(
      code = "N07",
      description = "NPS London"
    ),
    notes = "Please Note - Category 3 offenders require multi-agency management at Level 2 or 3 and should not be recorded at Level 1.\nNote\nnew note"
  )

  private fun convictionResponse(sentenceDescription: String = "CJA - Extended Sentence") = DeliusClient.Conviction(
    sentence = DeliusClient.Sentence(
      description = sentenceDescription,
      length = 6,
      lengthUnits = "Days",
      isCustodial = true,
      custodialStatusCode = "ABC123",
      licenceExpiryDate = LocalDate.parse("2022-05-10"),
      sentenceExpiryDate = LocalDate.parse("2022-06-10"),
    ),
    mainOffence = DeliusClient.Offence(
      date = LocalDate.parse("2022-08-26"),
      code = "ABC123",
      description = "Robbery (other than armed robbery)",
    ),
    additionalOffences = listOf(
      DeliusClient.Offence(
        date = LocalDate.parse("2022-08-26"),
        code = "ZYX789",
        description = "Arson"
      )
    )
  )

  private fun nonCustodialConviction(sentenceDescription: String = "CJA - Extended Sentence") = convictionResponse(sentenceDescription)
    .let { it.copy(sentence = it.sentence!!.copy(custodialStatusCode = null, isCustodial = false)) }
}
