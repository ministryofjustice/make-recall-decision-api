package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.RiskResponse
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.Assessment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentOffenceDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.ContingencyPlanResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.CurrentScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.GeneralPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.HistoricalScoreResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCommunity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskInCustody
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskOfSeriousRecidivismScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskSummaryResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.SexualPredictorScore
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RiskServiceTest {
  private lateinit var riskService: RiskService

  @Mock
  private lateinit var communityApiClient: CommunityApiClient

  @Mock
  private lateinit var recommendationRepository: RecommendationRepository

  @Mock
  private lateinit var personDetailsService: PersonDetailsService

  @Mock
  private lateinit var convictionService: ConvictionService

  @Mock
  private lateinit var arnApiClient: ArnApiClient

  @Mock
  private lateinit var partATemplateReplacementService: PartATemplateReplacementService

  protected lateinit var userAccessValidator: UserAccessValidator

  protected lateinit var recommendationService: RecommendationService

  @BeforeEach
  fun setup() {
    userAccessValidator = UserAccessValidator(communityApiClient)
    recommendationService = RecommendationService(recommendationRepository, personDetailsService, partATemplateReplacementService, userAccessValidator, convictionService, null)
    riskService = RiskService(communityApiClient, arnApiClient, userAccessValidator, recommendationService)
  }

  @Test
  fun `retrieves risk`() {
    runTest {
      val crn = "my wonderful crn"
      given(communityApiClient.getAllOffenderDetails(anyString()))
        .willReturn(Mono.fromCallable { allOffenderDetailsResponse })
      given(arnApiClient.getRiskSummary(anyString()))
        .willReturn(Mono.fromCallable { riskSummaryResponse })
      given(arnApiClient.getHistoricalScores(anyString()))
        .willReturn(Mono.fromCallable { listOf(historicalScoresResponse) })
      given(arnApiClient.getCurrentScores(anyString()))
        .willReturn(Mono.fromCallable { listOf(currentScoreResponse) })
      given(arnApiClient.getContingencyPlan(anyString()))
        .willReturn(Mono.fromCallable { contingencyPlanResponse })
      given(communityApiClient.getAllMappaDetails(anyString()))
        .willReturn(Mono.fromCallable { mappaResponse })

      val response = riskService.getRisk(crn)

      val personalDetails = response.personalDetailsOverview!!
      val riskOfSeriousHarm = response.riskOfSeriousHarm!!
      val mappa = response.mappa!!
      val natureOfRisk = response.natureOfRisk
      val whoIsAtRisk = response.whoIsAtRisk
      val circumstancesIncreaseRisk = response.circumstancesIncreaseRisk
      val factorsToReduceRisk = response.factorsToReduceRisk
      val whenRiskHighest = response.whenRiskHighest
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current
      val contingencyPlan = response.contingencyPlan

      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age(allOffenderDetailsResponse))
      assertThat(personalDetails.gender).isEqualTo("Male")
      assertThat(personalDetails.dateOfBirth).isEqualTo(LocalDate.parse("1982-10-24"))
      assertThat(personalDetails.name).isEqualTo("John Smith")
      assertThat(riskOfSeriousHarm.overallRisk).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskToChildren).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskToPublic).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskToKnownAdult).isEqualTo("HIGH")
      assertThat(riskOfSeriousHarm.riskToStaff).isEqualTo("MEDIUM")
      assertThat(riskOfSeriousHarm.lastUpdated).isEqualTo("2021-10-09")
      assertThat(mappa.isNominal).isTrue() // TODO how is this derived?
      assertThat(mappa.level).isEqualTo(1)
      assertThat(mappa.category).isEqualTo(0)
      assertThat(mappa.lastUpdated).isEqualTo("10 May 2021")
      assertThat(natureOfRisk?.oasysHeading?.number).isEqualTo("10.2")
      assertThat(natureOfRisk?.oasysHeading?.description).isEqualTo("What is the nature of the risk?")
      assertThat(natureOfRisk?.description)
        .isEqualTo("The nature of the risk is X")
      assertThat(whoIsAtRisk?.oasysHeading?.number).isEqualTo("10.1")
      assertThat(whoIsAtRisk?.oasysHeading?.description).isEqualTo("Who is at risk?")
      assertThat(whoIsAtRisk?.description).isEqualTo("X, Y and Z are at risk")
      assertThat(circumstancesIncreaseRisk?.oasysHeading?.number).isEqualTo("10.4")
      assertThat(circumstancesIncreaseRisk?.oasysHeading?.description).isEqualTo("What circumstances are likely to increase the risk?")
      assertThat(circumstancesIncreaseRisk?.description).isEqualTo("If offender in situation X the risk can be higher")
      assertThat(factorsToReduceRisk?.oasysHeading?.number).isEqualTo("10.5")
      assertThat(factorsToReduceRisk?.oasysHeading?.description).isEqualTo("What factors are likely to reduce the risk?")
      assertThat(factorsToReduceRisk?.description).isEqualTo("Giving offender therapy in X will reduce the risk")
      assertThat(whenRiskHighest?.oasysHeading?.number).isEqualTo("10.3")
      assertThat(whenRiskHighest?.oasysHeading?.description).isEqualTo("When is the risk likely to be greatest?")
      assertThat(whenRiskHighest?.description).isEqualTo("the risk is imminent and more probably in X situation")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.score).isEqualTo("1")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.level).isEqualTo("LOW")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.type).isEqualTo("RSR")
      assertThat(historicalScores?.get(0)?.scores?.ospc?.score).isEqualTo("2")
      assertThat(historicalScores?.get(0)?.scores?.ospc?.level).isEqualTo("MEDIUM")
      assertThat(historicalScores?.get(0)?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.score).isEqualTo("3")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.level).isEqualTo("HIGH")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.type).isEqualTo("OSP/I")
      assertThat(currentScores?.rsr?.level).isEqualTo("MEDIUM")
      assertThat(currentScores?.rsr?.score).isEqualTo("2")
      assertThat(currentScores?.rsr?.type).isEqualTo("RSR")
      assertThat(currentScores?.ospc?.level).isEqualTo("HIGH")
      assertThat(currentScores?.ospc?.score).isEqualTo("2")
      assertThat(currentScores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(currentScores?.ospi?.level).isEqualTo("MEDIUM")
      assertThat(currentScores?.ospi?.score).isEqualTo("3")
      assertThat(currentScores?.ospi?.type).isEqualTo("OSP/I")
      assertThat(currentScores?.ogrs?.level).isEqualTo("LOW")
      assertThat(currentScores?.ogrs?.score).isEqualTo("1")
      assertThat(currentScores?.ogrs?.type).isEqualTo("OGRS")
      assertThat(contingencyPlan?.oasysHeading?.description).isEqualTo("Contingency plan")
      assertThat(contingencyPlan?.oasysHeading?.number).isEqualTo("10.1")
      assertThat(contingencyPlan?.description).isEqualTo(
        "key consideration for current situation\n" + "further consideration for current situation\n" + "supervision\n" + "monitoring and control\n" + "interventions and treatment\n" + "victim safety planning\n" + "contingency plans"
      )

      then(arnApiClient).should().getRiskSummary(crn)
      then(arnApiClient).should().getCurrentScores(crn)
      then(arnApiClient).should().getHistoricalScores(crn)
      then(arnApiClient).should().getContingencyPlan(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
      then(communityApiClient).should().getAllMappaDetails(crn)
    }
  }

  @Test
  fun `retrieves assessments`() {
    runTest {
      // given
      val crn = "my wonderful crn"
      given(arnApiClient.getAssessments(anyString()))
        .willReturn(Mono.fromCallable { assessmentResponse(crn) })
      given(communityApiClient.getActiveConvictions(anyString()))
        .willReturn(Mono.fromCallable { listOf(convictionResponse()) })

      // when
      val response = riskService.fetchIndexOffenceDetails(crn)

      // then
      assertThat(response).isEqualTo("Juicy offence details.")
      then(arnApiClient).should().getAssessments(crn)
      then(communityApiClient).should().getActiveConvictions(crn)
    }
  }

  @Test
  fun `retrieves risk with optional fields missing`() {
    runTest {
      val crn = "my wonderful crn"
      given(arnApiClient.getHistoricalScores(anyString()))
        .willReturn(
          Mono.fromCallable {
            listOf(historicalScoresResponseWithoutOptionalFields)
          }
        )
      given(arnApiClient.getCurrentScores(anyString()))
        .willReturn(
          Mono.fromCallable { listOf(currentScoreResponseWithOptionalFields) }
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
              riskInCommunity = RiskInCommunity(
                veryHigh = null,
                high = null,
                medium = null,
                low = null
              ),
              riskInCustody = RiskInCustody(
                veryHigh = null,
                high = null,
                medium = null,
                low = null
              ),
              assessedOn = LocalDateTime.parse("2021-10-09T08:26:31.349"),
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

      given(arnApiClient.getContingencyPlan(anyString()))
        .willReturn(
          Mono.fromCallable {
            ContingencyPlanResponse(
              assessments = emptyList()
            )
          }
        )

      given(communityApiClient.getAllMappaDetails(anyString()))
        .willReturn(
          Mono.fromCallable {
            mappaResponse.copy(level = null, levelDescription = null, reviewDate = null, category = null)
          }
        )

      val response = riskService.getRisk(crn)

      val personalDetails = response.personalDetailsOverview!!
      val riskOfSeriousHarm = response.riskOfSeriousHarm!!
      val mappa = response.mappa!!
      val natureOfRisk = response.natureOfRisk
      val whoIsAtRisk = response.whoIsAtRisk
      val circumstancesIncreaseRisk = response.circumstancesIncreaseRisk
      val factorsToReduceRisk = response.factorsToReduceRisk
      val whenRiskHighest = response.whenRiskHighest
      val historicalScores = response.predictorScores?.historical
      val currentScores = response.predictorScores?.current
      val contingencyPlan = response.contingencyPlan

      val dateOfBirth = LocalDate.parse("1982-10-24")
      val age = dateOfBirth?.until(LocalDate.now())?.years
      assertThat(personalDetails.crn).isEqualTo(crn)
      assertThat(personalDetails.age).isEqualTo(age)
      assertThat(personalDetails.gender).isEqualTo("")
      assertThat(personalDetails.dateOfBirth).isEqualTo(dateOfBirth)
      assertThat(personalDetails.name).isEqualTo("")
      assertThat(riskOfSeriousHarm.overallRisk).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskToChildren).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskToPublic).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskToKnownAdult).isEqualTo("")
      assertThat(riskOfSeriousHarm.riskToStaff).isEqualTo("")
      assertThat(riskOfSeriousHarm.lastUpdated).isEqualTo("2021-10-09")
      assertThat(mappa.isNominal).isTrue() // TODO how is this derived?
      assertThat(mappa.level).isEqualTo(null)
      assertThat(mappa.lastUpdated).isEqualTo("")
      assertThat(mappa.category).isNull()
      assertThat(natureOfRisk?.description).isEqualTo("")
      assertThat(whoIsAtRisk?.oasysHeading?.number).isEqualTo("10.1")
      assertThat(whoIsAtRisk?.oasysHeading?.description).isEqualTo("Who is at risk?")
      assertThat(whoIsAtRisk?.description).isEqualTo("")
      assertThat(circumstancesIncreaseRisk?.oasysHeading?.number).isEqualTo("10.4")
      assertThat(circumstancesIncreaseRisk?.oasysHeading?.description).isEqualTo("What circumstances are likely to increase the risk?")
      assertThat(circumstancesIncreaseRisk?.description).isEqualTo("")
      assertThat(factorsToReduceRisk?.oasysHeading?.number).isEqualTo("10.5")
      assertThat(factorsToReduceRisk?.oasysHeading?.description).isEqualTo("What factors are likely to reduce the risk?")
      assertThat(factorsToReduceRisk?.description).isEqualTo("")
      assertThat(whenRiskHighest?.oasysHeading?.number).isEqualTo("10.3")
      assertThat(whenRiskHighest?.oasysHeading?.description).isEqualTo("When is the risk likely to be greatest?")
      assertThat(whenRiskHighest?.description).isEqualTo("")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.score).isEqualTo("")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.level).isEqualTo("")
      assertThat(historicalScores?.get(0)?.scores?.rsr?.type).isEqualTo("RSR")
      assertThat(historicalScores?.get(0)?.scores?.ospc?.score).isEqualTo("")
      assertThat(historicalScores?.get(0)?.scores?.ospc?.level).isEqualTo("")
      assertThat(historicalScores?.get(0)?.scores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.score).isEqualTo("")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.level).isEqualTo("")
      assertThat(historicalScores?.get(0)?.scores?.ospi?.type).isEqualTo("OSP/I")
      assertThat(currentScores?.rsr?.level).isEqualTo("")
      assertThat(currentScores?.rsr?.score).isEqualTo("")
      assertThat(currentScores?.rsr?.type).isEqualTo("RSR")
      assertThat(currentScores?.ospc?.level).isEqualTo("")
      assertThat(currentScores?.ospc?.score).isEqualTo("")
      assertThat(currentScores?.ospc?.type).isEqualTo("OSP/C")
      assertThat(currentScores?.ospi?.level).isEqualTo("")
      assertThat(currentScores?.ospi?.score).isEqualTo("")
      assertThat(currentScores?.ospi?.type).isEqualTo("OSP/I")
      assertThat(currentScores?.ogrs?.level).isEqualTo("")
      assertThat(currentScores?.ogrs?.score).isEqualTo("")
      assertThat(currentScores?.ogrs?.type).isEqualTo("OGRS")
      assertThat(contingencyPlan?.oasysHeading?.description).isEqualTo("Contingency plan")
      assertThat(contingencyPlan?.oasysHeading?.number).isEqualTo("10.1")
      assertThat(contingencyPlan?.description).isEmpty()

      then(arnApiClient).should().getContingencyPlan(crn)
      then(arnApiClient).should().getCurrentScores(crn)
      then(arnApiClient).should().getHistoricalScores(crn)
      then(arnApiClient).should().getRiskSummary(crn)
      then(communityApiClient).should().getAllOffenderDetails(crn)
    }
  }

  @Test
  fun `given case is excluded for user then return user access response details`() {
    runTest {
      val crn = "12345"

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

  private fun userAccessResponse(excluded: Boolean, restricted: Boolean) = UserAccessResponse(
    userRestricted = restricted,
    userExcluded = excluded,
    exclusionMessage = "I am an exclusion message",
    restrictionMessage = null
  )

  fun age(offenderDetails: AllOffenderDetailsResponse) = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years

  private val riskSummaryResponse = RiskSummaryResponse(
    whoIsAtRisk = "X, Y and Z are at risk",
    natureOfRisk = "The nature of the risk is X",
    riskImminence = "the risk is imminent and more probably in X situation",
    riskIncreaseFactors = "If offender in situation X the risk can be higher",
    riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
    riskInCommunity = RiskInCommunity(
      veryHigh = null,
      high = listOf(
        "Children",
        "Public",
        "Known adult"
      ),
      medium = listOf("Staff"),
      low = listOf("Prisoners")
    ),
    riskInCustody = RiskInCustody(
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
    assessedOn = LocalDateTime.parse("2021-10-09T08:26:31.349"),
    overallRiskLevel = "HIGH"
  )

  private val contingencyPlanResponse = ContingencyPlanResponse(
    assessments = listOf(
      Assessment(
        dateCompleted = "2021-10-09T08:26:31.349",
        assessmentStatus = "COMPLETE",
        keyConsiderationsCurrentSituation = "key consideration for current situation\n",
        furtherConsiderationsCurrentSituation = "further consideration for current situation\n",
        supervision = "supervision\n",
        monitoringAndControl = "monitoring and control\n",
        interventionsAndTreatment = "interventions and treatment\n",
        victimSafetyPlanning = "victim safety planning\n",
        contingencyPlans = "contingency plans",
        offenceDetails = listOf(
          AssessmentOffenceDetail(
            type = "CURRENT",
            offenceCode = "12",
            offenceSubCode = "34"
          )
        ),
        offence = null,
        laterCompleteAssessmentExists = null,
        laterPartCompSignedAssessmentExists = null,
        laterPartCompUnsignedAssessmentExists = null,
        laterSignLockAssessmentExists = null,
        laterWIPAssessmentExists = null,
        superStatus = null
      )
    )
  )

  private val currentScoreResponse = CurrentScoreResponse(
    completedDate = "2018-09-12T12:00:00.000",
    generalPredictorScore = GeneralPredictorScore(ogpStaticWeightedScore = "", ogpDynamicWeightedScore = "", ogpTotalWeightedScore = "1", ogpRisk = "LOW"),
    riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "2", scoreLevel = "MEDIUM"),
    sexualPredictorScore = SexualPredictorScore(ospIndecentPercentageScore = "3", ospContactPercentageScore = "2", ospIndecentScoreLevel = "MEDIUM", ospContactScoreLevel = "HIGH")
  )

  private val currentScoreResponseWithOptionalFields = CurrentScoreResponse(
    completedDate = "",
    generalPredictorScore = GeneralPredictorScore(ogpStaticWeightedScore = "", ogpDynamicWeightedScore = "", ogpTotalWeightedScore = "", ogpRisk = ""),
    riskOfSeriousRecidivismScore = RiskOfSeriousRecidivismScore(percentageScore = "", scoreLevel = ""),
    sexualPredictorScore = SexualPredictorScore(ospIndecentPercentageScore = "", ospContactPercentageScore = "", ospIndecentScoreLevel = "", ospContactScoreLevel = "")
  )

  private val historicalScoresResponse = HistoricalScoreResponse(
    rsrPercentageScore = "1",
    rsrScoreLevel = "LOW",
    ospcPercentageScore = "2",
    ospcScoreLevel = "MEDIUM",
    ospiPercentageScore = "3",
    ospiScoreLevel = "HIGH",
    calculatedDate = "2018-09-12T12:00:00.000"
  )

  private val historicalScoresResponseWithoutOptionalFields = HistoricalScoreResponse(
    rsrPercentageScore = "",
    rsrScoreLevel = "",
    ospcPercentageScore = "",
    ospcScoreLevel = "",
    ospiPercentageScore = "",
    ospiScoreLevel = "",
    calculatedDate = null
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

  private fun assessmentResponse(crn: String): AssessmentsResponse {
    return AssessmentsResponse(crn, false, listOf(assessment()))
  }

  private fun assessment() = Assessment(
    dateCompleted = "2022-08-26T15:00:08",
    assessmentStatus = "COMPLETE",
    keyConsiderationsCurrentSituation = null,
    furtherConsiderationsCurrentSituation = null,
    supervision = null,
    monitoringAndControl = null,
    interventionsAndTreatment = null,
    victimSafetyPlanning = null,
    contingencyPlans = null,
    offenceDetails = listOf(
      AssessmentOffenceDetail(
        type = "CURRENT",
        offenceCode = "ABC123",
        offenceSubCode = ""
      )
    ),
    offence = "Juicy offence details.",
    laterCompleteAssessmentExists = false,
    laterPartCompSignedAssessmentExists = false,
    laterPartCompUnsignedAssessmentExists = false,
    laterSignLockAssessmentExists = false,
    laterWIPAssessmentExists = false,
    superStatus = "COMPLETE"
  )

  private val allOffenderDetailsResponse = AllOffenderDetailsResponse(
    dateOfBirth = LocalDate.parse("1982-10-24"),
    firstName = "John",
    surname = "Smith",
    middleNames = listOf("Homer", "Bart"),
    gender = "Male",
    otherIds = OtherIds(crn = null, croNumber = "123456/04A", mostRecentPrisonerNumber = "G12345", nomsNumber = "A1234CR", pncNumber = "2004/0712343H"),
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
