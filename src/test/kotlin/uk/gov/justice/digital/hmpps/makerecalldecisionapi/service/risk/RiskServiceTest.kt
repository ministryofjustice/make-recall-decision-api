package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.risk

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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskManagementPlan
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.*
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.ServiceTestBase
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.TemplateReplacementService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.SCORE_NOT_APPLICABLE
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RiskServiceTest : ServiceTestBase() {

  @Mock
  lateinit var templateReplacementService2: TemplateReplacementService

  @BeforeEach
  fun setup() {
    recommendationService = RecommendationService(
      recommendationRepository,
      recommendationStatusRepository,
      personDetailsService,
      PrisonerApiService(prisonApiClient),
      templateReplacementService2,
      userAccessValidator,
      null,
      deliusClient,
      null,
    )
    riskService = RiskService(deliusClient, arnApiClient, userAccessValidator, recommendationService)
  }

  @Test
  fun `retrieves risk`() {
    runTest {
      given(deliusClient.getMappaAndRoshHistory(crn))
        .willReturn(deliusMappaAndRoshHistoryResponse())
      given(arnApiClient.getRiskSummary(crn))
        .willReturn(Mono.fromCallable { riskSummaryResponse })
      val latestRiskScoreDate = randomLocalDateTime()
      val latestRiskScoreResponse = riskScoreResponse(completedDate = latestRiskScoreDate.toString())
      val riskScoreResponseWithNullDate = riskScoreResponse(completedDate = null)
      val yearOldRiskScoreResponse = riskScoreResponse(completedDate = latestRiskScoreDate.minusYears(1).toString())
      val anotherRiskScoreResponseWithNullDate = riskScoreResponse(completedDate = null)
      val yearsOldRiskScoreResponse = riskScoreResponse(completedDate = latestRiskScoreDate.minusYears(2).toString())
      given(arnApiClient.getRiskScores(crn))
        .willReturn(
          Mono.fromCallable {
            listOf(
              latestRiskScoreResponse,
              riskScoreResponseWithNullDate,
              yearOldRiskScoreResponse,
              anotherRiskScoreResponseWithNullDate,
              yearsOldRiskScoreResponse,
            )
          },
        )
      given(arnApiClient.getAssessments(crn))
        .willReturn(
          Mono.fromCallable {
            AssessmentsResponse(
              crn,
              false,
              listOf(
                assessment(),
                assessment().copy(dateCompleted = null),
                assessment().copy(
                  dateCompleted = null,
                  initiationDate = null,
                ),
              ),
            )
          },
        )

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
      comparePredictorScores(historicalScores?.get(0), latestRiskScoreResponse)
      comparePredictorScores(historicalScores?.get(1), yearOldRiskScoreResponse)
      comparePredictorScores(historicalScores?.get(2), yearsOldRiskScoreResponse)
      assertThat(historicalScores?.get(3)?.date).isEqualTo(null)
      assertThat(historicalScores?.get(4)?.date).isEqualTo(null)
      comparePredictorScores(currentScores, latestRiskScoreResponse)
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
        .willReturn(Mono.fromCallable { listOf(riskScoreResponse()) })
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
        .willReturn(
          Mono.fromCallable {
            AssessmentsResponse(
              crn,
              false,
              listOf(assessment().copy(laterCompleteAssessmentExists = false)),
            )
          },
        )
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions)

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
        .willReturn(
          Mono.fromCallable {
            AssessmentsResponse(
              crn,
              false,
              listOf(assessment().copy(laterCompleteAssessmentExists = false, offence = null)),
            )
          },
        )
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions)

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
  fun `retrieves assessments when hideOffenceDetailsWhenNoMatch is false and offences do not match as no active conviction with main offence exists`() {
    runTest {
      // given
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { AssessmentsResponse(crn, false, listOf(assessment())) })

      // when
      val response = riskService.fetchAssessmentInfo(crn, emptyList())

      // then
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(response?.offenceDataFromLatestCompleteAssessment).isEqualTo(true)
      assertThat(response?.offencesMatch).isEqualTo(false)
      assertThat(response?.offenceDescription).isEqualTo("Juicy offence details.")
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
                      offenceDate = thisDateDoesNotMatchDateInDelius,
                    ),
                  ),
                ),
                assessment().copy(
                  laterCompleteAssessmentExists = true,
                  dateCompleted = "2022-08-26T15:00:08",
                  superStatus = "OPEN",
                ),
              ),
            )
          },
        )
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions)

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
                      offenceDate = thisDateDoesNotMatchDateInDelius,
                    ),
                  ),
                ),
                assessment().copy(
                  laterCompleteAssessmentExists = true,
                  dateCompleted = "2022-08-26T15:00:08",
                  superStatus = "OPEN",
                ),
              ),
            )
          },
        )
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions)

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
                      offenceDate = nullDateShouldBeHandledAsMismatch,
                    ),
                  ),
                ),
                assessment().copy(
                  laterCompleteAssessmentExists = true,
                  dateCompleted = "2022-08-26T15:00:08",
                  superStatus = "OPEN",
                ),
              ),
            )
          },
        )
      val convictions = List(2) { nonCustodialConviction() }

      // when
      val response = riskService.fetchAssessmentInfo(crn, convictions)

      // then
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
              historicalRiskScoreResponseWhereValuesNull,
            )
          },
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
              currentRiskScoreResponseWithOptionalFields.copy(
                riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(
                  "10",
                  "MEDIUM",
                ),
              ),
              historicalRiskScoreResponseWhereValuesNull.copy(
                riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(
                  "10",
                  "MEDIUM",
                ),
              ),
            )
          },
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
                  ospContactScoreLevel = SCORE_NOT_APPLICABLE,
                ),
              ),
            )
          },
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
              historicalRiskScoreResponseWithOptionalFields,
            )
          },
        )
      apiMocksWithAllFieldsEmpty()

      val response = riskService.getRisk(crn)

      val personalDetails = response.personalDetailsOverview!!
      val mappa = response.mappa
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current

      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth.until(LocalDate.now()).years
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
            assessments = emptyList(),
          )
        },
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
              low = null,
            ),
            riskInCustody = RiskScore(
              veryHigh = null,
              high = null,
              medium = null,
              low = null,
            ),
            assessedOn = ("2022-10-09T08:26:31"),
            overallRiskLevel = null,
          )
        },
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
            userAccessResponse(
              excluded = true,
              restricted = false,
              userNotFound = false,
            ).copy(restrictionMessage = null),
            null,
            null,
            null,
            null,
          ),
        ),
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
            userAccessResponse(excluded = false, restricted = false, userNotFound = true)
              .copy(exclusionMessage = null, restrictionMessage = null),
            null,
            null,
            null,
            null,
          ),
        ),
      )
    }
  }

  @ParameterizedTest
  @CsvSource(
    "COMPLETE, true",
    "INCOMPLETE, false",
  )
  fun `given multiple risk management plans then return latest with assessment status set`(
    status: String,
    assessmentStatusComplete: Boolean,
  ) {
    runTest {
      given(arnApiClient.getRiskManagementPlan(anyString()))
        .willReturn(
          Mono.fromCallable {
            riskManagementResponse(crn, status)
          },
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
          },
        )
      val response = riskService.getLatestRiskManagementPlan(crn)

      assertThat(response.contingencyPlans).isEqualTo("I am the contingency plan text")
      assertThat(response.latestDateCompleted).isEqualTo("2022-10-07T14:20:27.000Z")
      assertThat(response.initiationDate).isEqualTo("2022-10-02T14:20:27.000Z")
      assertThat(response.lastUpdatedDate).isEqualTo("2022-10-02T14:20:27.000Z")
      assertThat(response.assessmentStatusComplete).isEqualTo(true)
    }
  }

  @Test
  fun `given a null sequence of Risk Management Plans when getting the latest then empty plan is returned`() {
    runTest {
      val riskManagementResponse = RiskManagementResponse(
        crn = crn,
        limitedAccessOffender = true,
        riskManagementPlan = null,
      )
      given(arnApiClient.getRiskManagementPlan(anyString()))
        .willReturn(
          Mono.fromCallable {
            riskManagementResponse
          },
        )

      val response = riskService.getLatestRiskManagementPlan(crn)

      assertThat(response).isEqualTo(RiskManagementPlan(assessmentStatusComplete = false))
    }
  }

  @Test
  fun `given a single plan with null initiationDate when getting risk management plan then plan is retrieved`() {
    runTest {
      val riskManagementResponse = RiskManagementResponse(
        crn = crn,
        limitedAccessOffender = true,
        riskManagementPlan = listOf(
          RiskManagementPlanResponse(
            initiationDate = null,
            contingencyPlans = "I am the contingency plan text",
          ),
        ),
      )
      given(arnApiClient.getRiskManagementPlan(anyString()))
        .willReturn(
          Mono.fromCallable {
            riskManagementResponse
          },
        )

      val response = riskService.getLatestRiskManagementPlan(crn)

      assertThat(response.contingencyPlans).isEqualTo("I am the contingency plan text")
      assertThat(response.initiationDate).isNull()
    }
  }

  @Test
  fun `given multiple plans and at least one with null initiationDate when getting risk management plan then null plans are considered older`() {
    runTest {
      val riskManagementResponse = RiskManagementResponse(
        crn = crn,
        limitedAccessOffender = true,
        riskManagementPlan = listOf(
          RiskManagementPlanResponse(
            initiationDate = null,
            contingencyPlans = "I am a null contingency plan text",
          ),
          RiskManagementPlanResponse(
            initiationDate = "2023-10-02T14:00:00",
            contingencyPlans = "I am the latest contingency plan text",
          ),
          RiskManagementPlanResponse(
            initiationDate = "2020-10-02T14:00:00",
            contingencyPlans = "I am the middle contingency plan text",
          ),
          RiskManagementPlanResponse(
            initiationDate = null,
            contingencyPlans = "I am another null contingency plan text",
          ),
        ),
      )
      given(arnApiClient.getRiskManagementPlan(anyString()))
        .willReturn(
          Mono.fromCallable {
            riskManagementResponse
          },
        )

      val response = riskService.getLatestRiskManagementPlan(crn)

      assertThat(response.contingencyPlans).isEqualTo("I am the latest contingency plan text")
    }
  }

  @ParameterizedTest(name = "given call to fetch risk assessments fails with {1} exception then set this in the error field response")
  @CsvSource("404,NOT_FOUND", "503,SERVER_ERROR", "999, SERVER_ERROR")
  fun `given call to fetch risk assessments fails on call to arn api with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String,
  ) {
    runTest {
      given(arnApiClient.getAssessments(crn)).willThrow(WebClientResponseException(code, "", null, null, null))

      val response = riskService.fetchAssessmentInfo(crn, emptyList())

      assertThat(response?.error).isEqualTo(expectedErrorCode)
    }
  }

  @ParameterizedTest(name = "given call to fetch risk scores fails with {1} exception then set this in the error field response")
  @CsvSource("404,NOT_FOUND", "503,SERVER_ERROR", "999, SERVER_ERROR")
  fun `given call to fetch risk scores fails with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String,
  ) {
    runTest {
      given(arnApiClient.getRiskScores(crn)).willThrow(WebClientResponseException(code, "", null, null, null))

      val response = riskService.fetchPredictorScores(crn)

      assertThat(response.error).isEqualTo(expectedErrorCode)
    }
  }

  @ParameterizedTest(name = "given call to fetch risk summary fails with {1} exception then set this in the error field response")
  @CsvSource(
    "404,Offender not found for CRN,NOT_FOUND",
    "404,'Latest COMPLETE with types [LAYER_1, LAYER_3] type not found for crn X12345',NOT_FOUND_LATEST_COMPLETE",
    "503,This is a server error,SERVER_ERROR",
    "999,Random error,SERVER_ERROR",
  )
  fun `given call to fetch risk summary fails with given exception then set this in the error field response`(
    code: Int,
    exceptionMessage: String,
    expectedErrorCode: String,
  ) {
    runTest {
      given(arnApiClient.getRiskSummary(crn)).willThrow(
        WebClientResponseException(
          code,
          "",
          null,
          exceptionMessage.toByteArray(Charsets.UTF_8),
          null,
        ),
      )

      val response = riskService.getRoshSummary(crn)

      assertThat(response.error).isEqualTo(expectedErrorCode)
    }
  }

  @ParameterizedTest(name = "given call to risk management plan fails with {1} exception then set this in the error field response")
  @CsvSource("404,NOT_FOUND", "503,SERVER_ERROR", "999, SERVER_ERROR")
  fun `given call to risk management plan fails with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String,
  ) {
    runTest {
      given(arnApiClient.getRiskManagementPlan(crn))
        .willThrow(
          WebClientResponseException(code, "", null, null, null),
        )

      val response = riskService.getLatestRiskManagementPlan(crn)

      assertThat(response.error).isEqualTo(expectedErrorCode)
    }
  }

  private fun comparePredictorScores(predictorScores: PredictorScore?, riskScoreResponse: RiskScoreResponse) {
    assertThat(predictorScores?.date).isEqualTo(
      riskScoreResponse.completedDate?.let { LocalDateTime.parse(it).toLocalDate().toString() },
    )
    with(riskScoreResponse.riskOfSeriousRecidivismScore) {
      assertThat(predictorScores?.scores?.rsr?.level).isEqualTo(this?.scoreLevel)
      assertThat(predictorScores?.scores?.rsr?.score).isEqualTo(this?.percentageScore)
      assertThat(predictorScores?.scores?.rsr?.type).isEqualTo("RSR")
    }
    with(riskScoreResponse.sexualPredictorScore) {
      assertThat(predictorScores?.scores?.ospc?.level).isEqualTo(this?.ospContactScoreLevel)
      assertThat(predictorScores?.scores?.ospc?.score).isEqualTo(null)
      assertThat(predictorScores?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(predictorScores?.scores?.ospi?.level).isEqualTo(this?.ospIndecentScoreLevel)
      assertThat(predictorScores?.scores?.ospi?.score).isEqualTo(null)
      assertThat(predictorScores?.scores?.ospi?.type).isEqualTo("OSP/I")
    }
    with(riskScoreResponse.violencePredictorScore) {
      assertThat(predictorScores?.scores?.ovp?.oneYear).isEqualTo(this?.oneYear)
      assertThat(predictorScores?.scores?.ovp?.twoYears).isEqualTo(this?.twoYears)
      assertThat(predictorScores?.scores?.ovp?.level).isEqualTo(this?.ovpRisk)
      assertThat(predictorScores?.scores?.ovp?.type).isEqualTo("OVP")
    }
    with(riskScoreResponse.generalPredictorScore) {
      assertThat(predictorScores?.scores?.ogp?.oneYear).isEqualTo(this?.ogp1Year)
      assertThat(predictorScores?.scores?.ogp?.twoYears).isEqualTo(this?.ogp2Year)
      assertThat(predictorScores?.scores?.ogp?.level).isEqualTo(this?.ogpRisk)
      assertThat(predictorScores?.scores?.ogp?.type).isEqualTo("OGP")
    }
    with(riskScoreResponse.groupReconvictionScore) {
      assertThat(predictorScores?.scores?.ogrs?.level).isEqualTo(this?.scoreLevel)
      assertThat(predictorScores?.scores?.ogrs?.oneYear).isEqualTo(this?.oneYear)
      assertThat(predictorScores?.scores?.ogrs?.twoYears).isEqualTo(this?.twoYears)
      assertThat(predictorScores?.scores?.ogrs?.type).isEqualTo("OGRS")
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
        "Known adult",
      ),
      medium = listOf("Staff"),
      low = null,
    ),
    riskInCustody = RiskScore(
      veryHigh = listOf(
        "Staff",
        "Prisoners",
      ),
      high = listOf("Known adult"),
      medium = null,
      low = listOf(
        "Children",
        "Public",
      ),
    ),
    assessedOn = "2022-10-09T08:26:31",
    overallRiskLevel = "HIGH",
  )

  private val historicalRiskScoreResponseWhereValuesNull = RiskScoreResponse(
    completedDate = "2017-09-12T12:00:00.000",
    generalPredictorScore = GeneralPredictorScore(
      ogpStaticWeightedScore = null,
      ogpDynamicWeightedScore = null,
      ogpTotalWeightedScore = null,
      ogpRisk = null,
      ogp1Year = null,
      ogp2Year = null,
    ),
    riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = null, scoreLevel = null),
    sexualPredictorScore = SexualPredictorScore(
      ospIndecentPercentageScore = null,
      ospContactPercentageScore = null,
      ospIndecentScoreLevel = null,
      ospContactScoreLevel = null,
    ),
    groupReconvictionScore = GroupReconvictionScore(oneYear = null, twoYears = null, scoreLevel = null),
    violencePredictorScore = ViolencePredictorScore(
      ovpStaticWeightedScore = null,
      ovpDynamicWeightedScore = null,
      ovpTotalWeightedScore = null,
      ovpRisk = null,
      oneYear = null,
      twoYears = null,
    ),
  )

  private val currentRiskScoreResponseWithOptionalFields = RiskScoreResponse(
    completedDate = "2018-09-12T12:00:00.000",
    generalPredictorScore = null,
    riskOfSeriousRecidivismScore = null,
    sexualPredictorScore = null,
    groupReconvictionScore = null,
    violencePredictorScore = null,
  )

  private val historicalRiskScoreResponseWithOptionalFields = RiskScoreResponse(
    completedDate = "2017-09-12T12:00:00.000",
    generalPredictorScore = null,
    riskOfSeriousRecidivismScore = null,
    sexualPredictorScore = null,
    groupReconvictionScore = null,
    violencePredictorScore = null,
  )
}
