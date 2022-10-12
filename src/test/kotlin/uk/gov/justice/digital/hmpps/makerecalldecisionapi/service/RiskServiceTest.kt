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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AddressStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.AllOffenderDetailsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ContactDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Conviction
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Custody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.EstablishmentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Institution
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.KeyDates
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LocalDeliveryUnit
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.MappaResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OffenderProfile
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Officer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OrderManager
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.OtherIds
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProbationArea
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.ProviderEmployee
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Sentence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.SentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Staff
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.Team
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.TrustOfficer
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GroupReconvictionScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.SexualPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ViolencePredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.SCORE_NOT_APPLICABLE
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RiskServiceTest : ServiceTestBase() {

  @Mock
  lateinit var convictionService2: ConvictionService

  @Mock
  lateinit var templateReplacementService2: TemplateReplacementService

  @BeforeEach
  fun setup() {
    recommendationService = RecommendationService(
      recommendationRepository,
      personDetailsService,
      templateReplacementService2,
      userAccessValidator,
      convictionService2,
      null
    )
    riskService = RiskService(communityApiClient, arnApiClient, userAccessValidator, recommendationService)
  }

  @Test
  fun `retrieves risk`() {
    runTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse })
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
      given(communityApiClient.getAllMappaDetails(anyString()))
        .willReturn(Mono.fromCallable { mappaResponse })
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { assessmentResponse(crn) })

      val response = riskService.getRisk(crn)

      val personalDetails = response.personalDetailsOverview!!
      val riskOfSeriousHarm = response.roshSummary?.riskOfSeriousHarm!!
      val mappa = response.mappa!!
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse))
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
      assertThat(mappa.lastUpdatedDate).isEqualTo("2021-05-10")
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
      assertThat(historicalScores?.get(0)?.scores?.ospc?.score).isEqualTo("2")
      assertThat(historicalScores?.get(0)?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.level).isEqualTo("MEDIUM")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.score).isEqualTo("3")
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
      assertThat(historicalScores?.get(1)?.scores?.ospc?.score).isEqualTo("2")
      assertThat(historicalScores?.get(1)?.scores?.ospc?.level).isEqualTo("HIGH")
      assertThat(historicalScores?.get(1)?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(historicalScores?.get(1)?.scores?.ospi?.score).isEqualTo("3")
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
      assertThat(historicalScores?.get(2)?.scores?.ospc?.score).isEqualTo("2")
      assertThat(historicalScores?.get(2)?.scores?.ospc?.level).isEqualTo("HIGH")
      assertThat(historicalScores?.get(2)?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(historicalScores?.get(2)?.scores?.ospi?.score).isEqualTo("3")
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
      assertThat(currentScores?.scores?.ospc?.score).isEqualTo("2")
      assertThat(currentScores?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(currentScores?.scores?.ospi?.level).isEqualTo("MEDIUM")
      assertThat(currentScores?.scores?.ospi?.score).isEqualTo("3")
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
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).should().getAllMappaDetails(crn)
    }
  }

  @Test
  fun `retrieves risk when assessment status is incomplete`() {
    runTest {
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse })
      given(arnApiClient.getRiskSummary(anyString()))
        .willReturn(Mono.fromCallable { riskSummaryResponse })
      given(arnApiClient.getRiskScores(anyString()))
        .willReturn(Mono.fromCallable { listOf(currentRiskScoreResponse) })
      given(communityApiClient.getAllMappaDetails(anyString()))
        .willReturn(Mono.fromCallable { mappaResponse })
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { assessmentResponse(crn).copy(assessments = listOf(assessment().copy(superStatus = "BLA"))) })

      val response = riskService.getRisk(crn)

      assertThat(response.assessmentStatus).isEqualTo("INCOMPLETE")
      then(arnApiClient).should().getAssessments(crn)
    }
  }

  @Test
  fun `retrieves assessments`() {
    runTest {
      // given
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { assessmentResponse(crn) })
      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { listOf(convictionResponse()) })

      // when
      val response = riskService.fetchAssessmentInfo(crn)

      // then
      assertThat(response?.lastUpdatedDate).isEqualTo("2022-08-26T15:00:08.000Z")
      assertThat(response?.offenceDataFromLatestCompleteAssessment).isEqualTo(true)
      assertThat(response?.offenceCodesMatch).isEqualTo(true)
      assertThat(response?.offenceDescription).isEqualTo("Juicy offence details.")
      then(arnApiClient).should().getAssessments(crn)
      then(communityApiClient).should().getActiveConvictions(crn)
    }
  }

  @Test
  fun `retrieves risk with null risk score values`() {
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
      communityApiMocksWithAllFieldsEmpty()

      val response = riskService.getRisk(crn)
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current

      assertThat(historicalScores?.get(0)?.date).isEqualTo("2018-09-12")
      assertThat(historicalScores?.get(0)?.scores?.rsr).isEqualTo(null)
      assertThat(historicalScores?.get(0)?.scores?.ospc).isEqualTo(null)
      assertThat(historicalScores?.get(0)?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.date).isEqualTo("2018-09-12")
      assertThat(currentScores?.scores?.rsr).isEqualTo(null)
      assertThat(currentScores?.scores?.ospc).isEqualTo(null)
      assertThat(currentScores?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.scores?.ogrs).isEqualTo(null)
      assertThat(response.assessmentStatus).isEqualTo("INCOMPLETE")

      then(arnApiClient).should().getAssessments(crn)
      then(arnApiClient).should().getRiskScores(crn)
      then(arnApiClient).should().getRiskSummary(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
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
                  ospIndecentPercentageScore = SCORE_NOT_APPLICABLE,
                  ospContactPercentageScore = "0",
                  ospIndecentScoreLevel = "0",
                  ospContactScoreLevel = SCORE_NOT_APPLICABLE
                )
              )
            )
          }
        )
      communityApiMocksWithAllFieldsEmpty()

      val response = riskService.getRisk(crn)
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current

      assertThat(historicalScores?.get(0)?.date).isEqualTo("2018-09-12")
      assertThat(historicalScores?.get(0)?.scores?.rsr).isEqualTo(null)
      assertThat(historicalScores?.get(0)?.scores?.ospc).isEqualTo(null)
      assertThat(historicalScores?.get(0)?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.date).isEqualTo("2018-09-12")
      assertThat(currentScores?.scores?.rsr).isEqualTo(null)
      assertThat(currentScores?.scores?.ospc).isEqualTo(null)
      assertThat(currentScores?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.scores?.ogrs).isEqualTo(null)
      assertThat(response.assessmentStatus).isEqualTo("INCOMPLETE")

      then(arnApiClient).should().getAssessments(crn)
      then(arnApiClient).should().getRiskScores(crn)
      then(arnApiClient).should().getRiskSummary(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
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
      communityApiMocksWithAllFieldsEmpty()

      val response = riskService.getRisk(crn)

      val personalDetails = response.personalDetailsOverview!!
      val riskOfSeriousHarm = response.roshSummary?.riskOfSeriousHarm!!
      val mappa = response.mappa!!
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current

      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("")
      assertThat(riskOfSeriousHarm.overallRisk).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToChildren).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToPublic).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToKnownAdult).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToStaff).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCommunity?.riskToPrisoners).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToChildren).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToPublic).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToKnownAdult).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToStaff).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskInCustody?.riskToPrisoners).isEqualTo("")
      assertThat(mappa.level).isEqualTo(null)
      assertThat(mappa.lastUpdatedDate).isEqualTo("")
      assertThat(mappa.category).isNull()
      assertThat(response.roshSummary?.lastUpdatedDate).isEqualTo("2022-10-09T08:26:31.000Z")
      assertThat(response.roshSummary?.natureOfRisk).isEqualTo("")
      assertThat(response.roshSummary?.whoIsAtRisk).isEqualTo("")
      assertThat(response.roshSummary?.riskIncreaseFactors).isEqualTo("")
      assertThat(response.roshSummary?.riskMitigationFactors).isEqualTo("")
      assertThat(response.roshSummary?.riskImminence).isEqualTo("")
      assertThat(historicalScores?.get(0)?.date).isEqualTo("2018-09-12")
      assertThat(historicalScores?.get(0)?.scores?.rsr).isEqualTo(null)
      assertThat(historicalScores?.get(0)?.scores?.ospc).isEqualTo(null)
      assertThat(historicalScores?.get(0)?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.date).isEqualTo("2018-09-12")
      assertThat(currentScores?.scores?.rsr).isEqualTo(null)
      assertThat(currentScores?.scores?.ospc).isEqualTo(null)
      assertThat(currentScores?.scores?.ospi).isEqualTo(null)
      assertThat(currentScores?.scores?.ogrs).isEqualTo(null)
      assertThat(response.assessmentStatus).isEqualTo("INCOMPLETE")

      then(arnApiClient).should().getAssessments(crn)
      then(arnApiClient).should().getRiskScores(crn)
      then(arnApiClient).should().getRiskSummary(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  private fun communityApiMocksWithAllFieldsEmpty() {
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

    given(communityApiClient.getAllOffenderDetails(anyString()))
      .willReturn(
        Mono.fromCallable {
          allOffenderDetailsResponse.copy(
            firstName = null,
            surname = null,
            gender = null,
            contactDetails = ContactDetails(
              addresses = listOf(
                Address(
                  postcode = null,
                  district = null,
                  addressNumber = null,
                  buildingName = null,
                  town = null,
                  county = null, status = AddressStatus(code = "ABC123", description = "Main")
                )
              )
            ),
            offenderManagers = listOf(
              OffenderManager(
                active = true,
                probationArea = null,
                trustOfficer = TrustOfficer(forenames = null, surname = null),
                staff = Staff(forenames = null, surname = null),
                providerEmployee = ProviderEmployee(forenames = null, surname = null),
                team = Team(
                  telephone = null,
                  emailAddress = null,
                  code = null,
                  description = null,
                  localDeliveryUnit = null
                )
              )
            )
          )
        }
      )

    given(communityApiClient.getAllMappaDetails(anyString()))
      .willReturn(
        Mono.fromCallable {
          mappaResponse.copy(level = null, levelDescription = null, reviewDate = null, category = null)
        }
      )
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {

      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          403, "Forbidden", null, excludedResponse().toByteArray(), null
        )
      )

      val response = riskService.getRisk(crn)

      then(communityApiClient).should().getUserAccess(crn)

      com.natpryce.hamkrest.assertion.assertThat(
        response,
        equalTo(
          RiskResponse(
            userAccessResponse(true, false).copy(restrictionMessage = null), null, null, null, null
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
  fun `given call to fetch risk assessments fails on call to community api with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String
  ) {
    runTest {
      given(communityApiClient.getActiveConvictions(crn)).willThrow(WebClientResponseException(code, null, null, null, null))

      val response = riskService.fetchAssessmentInfo(crn)

      assertThat(response?.error).isEqualTo(expectedErrorCode)
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

      val response = riskService.fetchAssessmentInfo(crn)

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
  @CsvSource("404,NOT_FOUND", "503,SERVER_ERROR", "999, SERVER_ERROR")
  fun `given call to fetch risk summary fails with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String
  ) {
    runTest {
      given(arnApiClient.getRiskSummary(crn)).willThrow(WebClientResponseException(code, null, null, null, null))

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

  @ParameterizedTest(name = "given call to mappa fails with {1} exception then set this in the error field response")
  @CsvSource("404,NOT_FOUND", "503,SERVER_ERROR", "999, SERVER_ERROR")
  fun `given call to maapa fails with given exception then set this in the error field response`(
    code: Int,
    expectedErrorCode: String
  ) {
    runTest {
      given(communityApiClient.getAllMappaDetails(crn)).willThrow(
        WebClientResponseException(
          code,
          null,
          null,
          null,
          null
        )
      )

      val response = riskService.getMappa(crn)

      assertThat(response.error).isEqualTo(expectedErrorCode)
    }
  }

  fun age(offenderDetails: AllOffenderDetailsResponse) = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years

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

  private val allOffenderDetailsResponse = AllOffenderDetailsResponse(
    dateOfBirth = LocalDate.parse("1982-10-24"),
    firstName = "John",
    surname = "Smith",
    middleNames = listOf("Homer", "Bart"),
    gender = "Male",
    otherIds = OtherIds(
      crn = null,
      croNumber = "123456/04A",
      mostRecentPrisonerNumber = "G12345",
      nomsNumber = "A1234CR",
      pncNumber = "2004/0712343H"
    ),
    contactDetails = ContactDetails(
      addresses = listOf(
        Address(
          postcode = "S3 7BS",
          district = "Sheffield City Centre",
          addressNumber = "32",
          buildingName = "HMPPS Digital Studio",
          town = "Sheffield",
          county = "South Yorkshire", status = AddressStatus(code = "ABC123", description = "Main")
        ),
        Address(
          town = "Sheffield",
          county = "South Yorkshire",
          buildingName = "HMPPS Digital Studio",
          district = "Sheffield City Centre",
          status = AddressStatus(code = "ABC123", description = "Not Main"),
          postcode = "S3 7BS",
          addressNumber = "33"
        )
      )
    ),
    offenderManagers = listOf(
      OffenderManager(
        active = true,
        probationArea = ProbationArea(code = "N01", description = "NPS North West"),
        trustOfficer = TrustOfficer(forenames = "Sheila Linda", surname = "Hancock"),
        staff = Staff(forenames = "Sheila Linda", surname = "Hancock"),
        providerEmployee = ProviderEmployee(forenames = "Sheila Linda", surname = "Hancock"),
        team = Team(
          telephone = "09056714321",
          emailAddress = "first.last@digital.justice.gov.uk",
          code = "C01T04",
          description = "OMU A",
          localDeliveryUnit = LocalDeliveryUnit(code = "ABC123", description = "Local delivery unit description")
        )
      ),
      OffenderManager(
        active = false,
        probationArea = ProbationArea(code = "N01", description = "NPS North West"),
        trustOfficer = TrustOfficer(forenames = "Dua", surname = "Lipa"),
        staff = Staff(forenames = "Sheila Linda", surname = "Hancock"),
        providerEmployee = ProviderEmployee(forenames = "Sheila Linda", surname = "Hancock"),
        team = Team(
          telephone = "123",
          emailAddress = "dua.lipa@digital.justice.gov.uk",
          code = "C01T04",
          description = "OMU A",
          localDeliveryUnit = LocalDeliveryUnit(code = "ABC123", description = "Local delivery unit description")
        )
      )
    ),
    offenderProfile = OffenderProfile(ethnicity = "Ainu")
  )

  private fun convictionResponse(sentenceDescription: String = "CJA - Extended Sentence") = Conviction(
    convictionDate = LocalDate.parse("2021-06-10"),
    sentence = Sentence(
      startDate = LocalDate.parse("2022-04-26"),
      terminationDate = LocalDate.parse("2022-04-26"),
      expectedSentenceEndDate = LocalDate.parse("2022-04-26"),
      description = sentenceDescription,
      originalLength = 6,
      originalLengthUnits = "Days",
      secondLength = 10,
      secondLengthUnits = "Months",
      sentenceType = SentenceType(code = "ABC123")
    ),
    active = true,
    offences = listOf(
      Offence(
        mainOffence = true,
        offenceDate = LocalDate.parse("2022-08-26"),
        detail = OffenceDetail(
          mainCategoryDescription = "string", subCategoryDescription = "string",
          description = "Robbery (other than armed robbery)",
          code = "ABC123",
        )
      ),
      Offence(
        mainOffence = false,
        offenceDate = LocalDate.parse("2022-08-26"),
        detail = OffenceDetail(
          mainCategoryDescription = "string", subCategoryDescription = "string",
          description = "Arson",
          code = "ZYX789"
        )
      )
    ),
    convictionId = 2500614567,
    orderManagers =
    listOf(
      OrderManager(
        dateStartOfAllocation = LocalDateTime.parse("2022-04-26T20:39:47.778"),
        name = "string",
        staffCode = "STFFCDEU",
        gradeCode = "string"
      )
    ),
    custody = Custody(
      bookingNumber = "12345",
      institution = Institution(
        code = "COMMUN",
        description = "In the Community",
        establishmentType = EstablishmentType(
          code = "E",
          description = "Prison"
        ),
        institutionId = 156,
        institutionName = "In the Community",
        isEstablishment = true,
        isPrivate = false,
        nomsPrisonInstitutionCode = "AB124",
      ),
      status = CustodyStatus(code = "ABC123", description = "custody status"),
      keyDates = KeyDates(
        conditionalReleaseDate = LocalDate.parse("2020-06-20"),
        expectedPrisonOffenderManagerHandoverDate = LocalDate.parse("2020-06-21"),
        expectedPrisonOffenderManagerHandoverStartDate = LocalDate.parse("2020-06-22"),
        expectedReleaseDate = LocalDate.parse("2020-06-23"),
        hdcEligibilityDate = LocalDate.parse("2020-06-24"),
        licenceExpiryDate = LocalDate.parse("2022-05-10"),
        paroleEligibilityDate = LocalDate.parse("2020-06-26"),
        sentenceExpiryDate = LocalDate.parse("2022-06-10"),
        postSentenceSupervisionEndDate = LocalDate.parse("2022-05-11"),
      ),
      sentenceStartDate = LocalDate.parse("2022-04-26")
    )
  )
}
