package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTimeUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.times
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.willReturn
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.MrdTestDataBuilder
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentRequestType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.MrdEvent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceTypeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecision
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecisionTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ManagerRecallDecisionTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallConsidered
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecallValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.YesNoNotApplicableOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.toPersonOnProbationDto
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.toPersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.AssessmentsResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RecommendationUpdateException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UpdateExceptionTypes.RECOMMENDATION_UPDATE_FAILED
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationStatusEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader.CustomMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationServiceTest.MockitoHelper.anyObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RecommendationServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    DateTimeUtils.setCurrentMillisFixed(1658828907443)
  }

  @Mock
  protected lateinit var riskServiceMocked: RiskService

  @Captor
  private lateinit var recommendationCaptor: ArgumentCaptor<RecommendationEntity>

  @Mock
  protected lateinit var mrdEmitterMocked: MrdEventsEmitter

  @Mock
  protected lateinit var templateReplacementServiceMocked: TemplateReplacementService

  @ParameterizedTest()
  @CsvSource("RECOMMENDATION_STARTED", "RECALL_CONSIDERED", "NO_FLAGS")
  fun `create recommendation with and without recall considered flag`(featureFlag: String) {
    runTest {
      // given
      val recommendationToSave = RecommendationEntity(
        data = RecommendationModel(
          crn = crn,
          status = Status.DRAFT,
          lastModifiedBy = "Bill",
          region = "London",
          localDeliveryUnit = "LDU London",
          personOnProbation = PersonOnProbation(
            name = "John Smith",
            gender = "Male",
            ethnicity = "Ainu",
            primaryLanguage = "English",
            dateOfBirth = LocalDate.parse("1982-10-24"),
            croNumber = "123456/04A",
            pncNumber = "2004/0712343H",
            mostRecentPrisonerNumber = "G12345",
            nomsNumber = "A1234CR",
            addresses = listOf(
              Address(
                line1 = "Line 1 address",
                line2 = "Line 2 address",
                town = "Town address",
                postcode = "TS1 1ST",
                noFixedAbode = false,
              ),
            ),
          ),
        ),
      )

      // and
      given(recommendationRepository.save(any())).willReturn(recommendationToSave)
      recommendationService = RecommendationService(
        recommendationRepository,
        recommendationStatusRepository,
        mockPersonDetailService,
        templateReplacementService,
        userAccessValidator,
        riskServiceMocked,
        deliusClient,
        mrdEmitterMocked,
      )

      // and
      val featureFlags = when (featureFlag) {
        "RECALL_CONSIDERED" -> FeatureFlags(flagConsiderRecall = true)
        "RECOMMENDATION_STARTED" -> FeatureFlags(
          flagDomainEventRecommendationStarted = true,
          flagConsiderRecall = false,
        )

        else -> null
      }
      val recallConsidereDetail = if (featureFlag == "RECALL_CONSIDERED") "Juicy details" else null

      // when
      val response = recommendationService.createRecommendation(
        CreateRecommendationRequest(crn, recallConsidereDetail),
        "UserBill",
        "Bill",
        featureFlags,
      )

      // then
      assertThat(response?.id).isNotNull
      assertThat(response?.status).isEqualTo(Status.DRAFT)
      assertThat(response?.personOnProbation).isEqualTo(recommendationToSave.data.personOnProbation?.toPersonOnProbationDto())

      val captor = argumentCaptor<RecommendationEntity>()
      then(recommendationRepository).should().save(captor.capture())
      val recommendationEntity = captor.firstValue
      val expectedStatus = if (featureFlag == "RECALL_CONSIDERED") Status.RECALL_CONSIDERED else Status.DRAFT

      assertThat(recommendationEntity.id).isNotNull()
      assertThat(recommendationEntity.data.crn).isEqualTo(crn)
      assertThat(recommendationEntity.data.status).isEqualTo(expectedStatus)
      assertThat(recommendationEntity.data.personOnProbation).isEqualTo(
        PersonOnProbation(
          name = "John Smith",
          firstName = "John",
          middleNames = "Homer Bart",
          surname = "Smith",
          gender = "Male",
          ethnicity = "Ainu",
          primaryLanguage = "English",
          hasBeenReviewed = false,
          dateOfBirth = LocalDate.parse("1982-10-24"),
          croNumber = "123456/04A",
          mostRecentPrisonerNumber = "G12345",
          nomsNumber = "A1234CR",
          pncNumber = "2004/0712343H",
          addresses = listOf(
            Address(
              line1 = "Line 1 address",
              line2 = "Line 2 address",
              town = "Town address",
              postcode = "TS1 1ST",
              noFixedAbode = false,
            ),
          ),
        ),
      )
      assertThat(recommendationEntity.data.lastModifiedBy).isEqualTo("UserBill")
      assertThat(recommendationEntity.data.lastModifiedByUserName).isEqualTo("Bill")
      assertThat(recommendationEntity.data.lastModifiedDate).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(recommendationEntity.data.createdBy).isEqualTo("UserBill")
      assertThat(recommendationEntity.data.createdDate).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(recommendationEntity.data.createdByUserFullName).isEqualTo("Bill")
      assertThat(recommendationEntity.data.region).isEqualTo("Probation area description")
      assertThat(recommendationEntity.data.localDeliveryUnit).isEqualTo("LDU description")
      assertThat(recommendationEntity.data.userNamePartACompletedBy).isNull()
      assertThat(recommendationEntity.data.lastPartADownloadDateTime).isNull()

      when (featureFlag) {
        "RECALL_CONSIDERED" -> {
          assertThat(recommendationEntity.data.recallConsideredList?.get(0)?.recallConsideredDetail).isEqualTo("Juicy details")
          assertThat(recommendationEntity.data.recallConsideredList?.get(0)?.userName).isEqualTo("Bill")
          assertThat(recommendationEntity.data.recallConsideredList?.get(0)?.userId).isEqualTo("UserBill")
          assertThat(recommendationEntity.data.recallConsideredList?.get(0)?.createdDate).isNotBlank
          assertThat(recommendationEntity.data.recallConsideredList?.get(0)?.id).isNotNull()
          assertThat(recommendationEntity.data.recommendationStartedDomainEventSent).isEqualTo(false)
          then(mrdEmitterMocked).shouldHaveNoInteractions()
        }

        "RECOMMENDATION_STARTED" -> {
          then(mrdEmitterMocked).should().sendEvent(org.mockito.kotlin.any())
          assertThat(recommendationEntity.data.recommendationStartedDomainEventSent).isEqualTo(true)
        }

        else -> {
          assertThat(recommendationEntity.data.recallConsideredList).isNull()
          assertThat(recommendationEntity.data.recommendationStartedDomainEventSent).isEqualTo(false)
          then(mrdEmitterMocked).shouldHaveNoInteractions()
        }
      }
    }
  }

  @Disabled // originally for mrd-1089
  @Test
  fun `return existing recommendation when one in progress already exists for case and create endpoint hit`() {
    runTest {
      // given
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          recallConsideredList = listOf(
            RecallConsidered(
              createdDate = "2022-11-01T15:22:24.567Z",
              userName = "Harry",
              userId = "harry",
              recallConsideredDetail = "Recall considered",
            ),
          ),
          status = Status.RECALL_CONSIDERED,
          personOnProbation = PersonOnProbation(name = "John Smith"),
          lastModifiedBy = "Jack",
          lastModifiedDate = "2022-07-01T15:22:24.567Z",
          createdBy = "Jack",
          createdDate = "2022-07-01T15:22:24.567Z",
          recommendationStartedDomainEventSent = null,
        ),
      )
      given(recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name)))
        .willReturn(listOf(existingRecommendation))

      // when
      val response =
        recommendationService.createRecommendation(CreateRecommendationRequest(crn, null), "UserBill", "Bill", null)

      // then
      assertThat(response?.id).isEqualTo(1)
      assertThat(response?.status).isEqualTo(Status.RECALL_CONSIDERED)
      assertThat(response?.personOnProbation).isEqualTo(existingRecommendation.data.personOnProbation?.toPersonOnProbationDto())

      // and
      then(recommendationRepository).should()
        .findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name))
      then(recommendationRepository).shouldHaveNoMoreInteractions()
    }
  }

  @ParameterizedTest()
  @CsvSource("RECOMMENDATION_STARTED_EVENT_ALREADY_SENT", "RECOMMENDATION_STARTED", "NO_FLAGS")
  fun `updates a recommendation to the database`(scenario: String) {
    runTest {
      // given
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          recallConsideredList = listOf(
            RecallConsidered(
              createdDate = "2022-11-01T15:22:24.567Z",
              userName = "Harry",
              userId = "harry",
              recallConsideredDetail = "Recall considered",
            ),
          ),
          status = Status.RECALL_CONSIDERED,
          personOnProbation = PersonOnProbation(name = "John Smith"),
          lastModifiedBy = "Jack",
          lastModifiedDate = "2022-07-01T15:22:24.567Z",
          createdBy = "Jack",
          createdDate = "2022-07-01T15:22:24.567Z",
          recommendationStartedDomainEventSent = if (scenario == "RECOMMENDATION_STARTED_EVENT_ALREADY_SENT") true else null,
        ),
      )

      // and
      var updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)
      updateRecommendationRequest =
        updateRecommendationRequest.copy(recommendationStartedDomainEventSent = existingRecommendation.data.recommendationStartedDomainEventSent)

      // and
      val recommendationToSave =
        existingRecommendation.copy(
          id = existingRecommendation.id,
          data = RecommendationModel(
            crn = existingRecommendation.data.crn,
            personOnProbation = updateRecommendationRequest.personOnProbation,
            recallType = updateRecommendationRequest.recallType,
            custodyStatus = updateRecommendationRequest.custodyStatus,
            responseToProbation = updateRecommendationRequest.responseToProbation,
            triggerLeadingToRecall = updateRecommendationRequest.triggerLeadingToRecall,
            whatLedToRecall = updateRecommendationRequest.whatLedToRecall,
            isThisAnEmergencyRecall = updateRecommendationRequest.isThisAnEmergencyRecall,
            isIndeterminateSentence = updateRecommendationRequest.isIndeterminateSentence,
            isExtendedSentence = updateRecommendationRequest.isExtendedSentence,
            activeCustodialConvictionCount = updateRecommendationRequest.activeCustodialConvictionCount,
            hasVictimsInContactScheme = updateRecommendationRequest.hasVictimsInContactScheme,
            indeterminateSentenceType = updateRecommendationRequest.indeterminateSentenceType,
            dateVloInformed = updateRecommendationRequest.dateVloInformed,
            hasArrestIssues = updateRecommendationRequest.hasArrestIssues,
            hasContrabandRisk = updateRecommendationRequest.hasContrabandRisk,
            status = Status.DRAFT,
            lastModifiedDate = "2022-07-26T09:48:27.443Z",
            lastModifiedBy = "bill",
            lastModifiedByUserName = "Bill",
            createdBy = existingRecommendation.data.createdBy,
            createdDate = existingRecommendation.data.createdDate,
            indexOffenceDetails = updateRecommendationRequest.indexOffenceDetails,
            alternativesToRecallTried = updateRecommendationRequest.alternativesToRecallTried,
            licenceConditionsBreached = updateRecommendationRequest.licenceConditionsBreached,
            underIntegratedOffenderManagement = updateRecommendationRequest.underIntegratedOffenderManagement,
            localPoliceContact = updateRecommendationRequest.localPoliceContact,
            vulnerabilities = updateRecommendationRequest.vulnerabilities,
            convictionDetail = updateRecommendationRequest.convictionDetail?.copy(hasBeenReviewed = true),
            fixedTermAdditionalLicenceConditions = updateRecommendationRequest.fixedTermAdditionalLicenceConditions,
            indeterminateOrExtendedSentenceDetails = updateRecommendationRequest.indeterminateOrExtendedSentenceDetails,
            mainAddressWherePersonCanBeFound = updateRecommendationRequest.mainAddressWherePersonCanBeFound,
            whyConsideredRecall = updateRecommendationRequest.whyConsideredRecall,
            reasonsForNoRecall = updateRecommendationRequest.reasonsForNoRecall,
            nextAppointment = updateRecommendationRequest.nextAppointment,
            offenceAnalysis = "This is the offence analysis",
            hasBeenReviewed = null,
            previousReleases = updateRecommendationRequest.previousReleases,
            previousRecalls = updateRecommendationRequest.previousRecalls,
            recallConsideredList = updateRecommendationRequest.recallConsideredList,
            currentRoshForPartA = updateRecommendationRequest.currentRoshForPartA,
            isOver18 = updateRecommendationRequest.isOver18,
            isMappaLevelAbove1 = updateRecommendationRequest.isMappaLevelAbove1,
            isSentenceUnder12Months = updateRecommendationRequest.isSentenceUnder12Months,
            hasBeenConvictedOfSeriousOffence = updateRecommendationRequest.hasBeenConvictedOfSeriousOffence,
          ),
        )

      // and
      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // and
      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      recommendationService = RecommendationService(
        recommendationRepository,
        recommendationStatusRepository,
        mockPersonDetailService,
        templateReplacementService,
        userAccessValidator,
        RiskService(deliusClient, arnApiClient, userAccessValidator, null),
        deliusClient,
        mrdEmitterMocked,
      )

      val featureFlags = when (scenario) {
        "RECOMMENDATION_STARTED" -> FeatureFlags(flagDomainEventRecommendationStarted = true, flagConsiderRecall = true)
        "RECOMMENDATION_STARTED_EVENT_ALREADY_SENT" -> FeatureFlags(
          flagDomainEventRecommendationStarted = true,
          flagConsiderRecall = true,
        )

        else -> null
      }

      // when
      recommendationService.updateRecommendation(
        recommendationJsonNode,
        1L,
        "bill",
        "Bill",
        null,
        false,
        false,
        emptyList(),
        featureFlags,
      )

      // then
      recallConsideredIdWorkaround(recommendationToSave)

      when (scenario) {
        "RECOMMENDATION_STARTED" -> {
          recommendationToSave.data.recommendationStartedDomainEventSent = true
          then(recommendationRepository).should().save(recommendationToSave)
          then(mrdEmitterMocked).should().sendEvent(org.mockito.kotlin.any())
        }

        "RECOMMENDATION_STARTED_EVENT_ALREADY_SENT" -> {
          recommendationToSave.data.recommendationStartedDomainEventSent = true
          then(recommendationRepository).should().save(recommendationToSave)
          then(mrdEmitterMocked).shouldHaveNoInteractions()
        }

        else -> {
          then(recommendationRepository).should().save(recommendationToSave)
          then(mrdEmitterMocked).shouldHaveNoInteractions()
        }
      }

      then(recommendationRepository).should().save(recommendationToSave)
      then(recommendationRepository).should().findById(1)
    }
  }

  @Test
  fun `updates a recommendation to the database when mappa is null`() {
    runTest {
      // given
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
        ),
      )

      // and
      var updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)
      updateRecommendationRequest =
        updateRecommendationRequest.copy(personOnProbation = PersonOnProbation(name = "John Smith", mappa = null))

      // and
      val recommendationToSave =
        existingRecommendation.copy(
          id = existingRecommendation.id,
          data = RecommendationModel(
            crn = existingRecommendation.data.crn,
            personOnProbation = PersonOnProbation(
              name = "John Smith",
              hasBeenReviewed = true,
              mappa = Mappa(hasBeenReviewed = true),
            ),
          ),
        )

      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // and
      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      // when
      recommendationService.updateRecommendation(
        recommendationJsonNode,
        1L,
        "bill",
        "Bill",
        null,
        false,
        false,
        emptyList(),
        null,
      )

      then(recommendationRepository).should().findById(1)
    }
  }

  @Test
  fun `given invalid recall type should throw invalid request exception on an update with manager recall decision`() {
    runTest {
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          recallType = RecallType(
            selected = RecallTypeSelectedValue(RecallTypeValue.NO_RECALL),
            allOptions = listOf(TextValueOption("NO_RECALL")),
          ),
        ),
      )

      val updateRecommendationRequest = RecommendationModel(
        crn = crn,
        recallType = existingRecommendation.data.recallType?.copy(
          selected = RecallTypeSelectedValue(RecallTypeValue.FIXED_TERM),
          allOptions = listOf(TextValueOption("NO_RECALL")),
        ),
        status = Status.DRAFT,
        managerRecallDecision = ManagerRecallDecision(
          selected = ManagerRecallDecisionTypeSelectedValue(
            value = ManagerRecallDecisionTypeValue.RECALL,
            details = "Recall",
          ),
          allOptions = listOf(
            TextValueOption(value = "NO_RECALL", text = "Do not recall"),
          ),
          isSentToDelius = false,
        ),
      )
      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      try {
        recommendationService.updateRecommendationWithManagerRecallDecision(recommendationJsonNode, 1L, "", "")
      } catch (e: InvalidRequestException) {
        // nothing to do here!!
      }
      then(recommendationRepository).shouldHaveNoMoreInteractions()
    }
  }

  @Test
  fun `given save to db not successful on update, should throw recommendationUpdateException`() {
    runTest {
      // given
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
        ),
      )

      // and
      val updateRecommendationRequest =
        MrdTestDataBuilder.updateRecommendationWithManagerRecallDecisionRequestData(existingRecommendation, "false")

      // and
      Mockito.`when`(recommendationRepository.save(any())).doThrow(RuntimeException())

      // and
      given(recommendationRepository.findById(any())).willReturn(Optional.of(existingRecommendation))

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      // and
      recommendationService = RecommendationService(
        recommendationRepository,
        recommendationStatusRepository,
        mockPersonDetailService,
        templateReplacementService,
        userAccessValidator,
        RiskService(deliusClient, arnApiClient, userAccessValidator, null),
        deliusClient,
        mrdEmitterMocked,
      )

      try {
        recommendationService.updateRecommendationWithManagerRecallDecision(recommendationJsonNode, 1L, "", "")
      } catch (e: RecommendationUpdateException) {
        assertThat(e.error).isEqualTo(RECOMMENDATION_UPDATE_FAILED.toString())
      }
      then(recommendationRepository).shouldHaveNoMoreInteractions()
    }
  }

  @ParameterizedTest()
  @CsvSource("true", "false")
  fun `updates a recommendation with delete recommendation rationale to the database`(sendSpoDeleteRationaleToDelius: String) {
    runTest {
      // given
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          recallConsideredList = listOf(
            RecallConsidered(
              createdDate = "2022-11-01T15:22:24.567Z",
              userName = "Harry",
              userId = "harry",
              recallConsideredDetail = "Recall considered",
            ),
          ),
          status = Status.RECALL_CONSIDERED,
          personOnProbation = PersonOnProbation(name = "John Smith"),
          lastModifiedBy = "Jack",
          lastModifiedDate = "2022-07-01T15:22:24.567Z",
          createdBy = "Jack",
          createdDate = "2022-07-01T15:22:24.567Z",
        ),
      )

      // and
      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationWithDeleteRecallDecisionRequestData(
        existingRecommendation,
        sendSpoDeleteRationaleToDelius,
      )

      // and
      val recommendationToSave =
        existingRecommendation.copy(
          id = existingRecommendation.id,
          data = RecommendationModel(
            crn = existingRecommendation.data.crn,
            sensitive = null,
            recallConsideredList = null,
            recallType = null,
            spoRecallType = "RECALL",
            spoRecallRationale = "Recall",
            sendSpoDeleteRationaleToDelius = sendSpoDeleteRationaleToDelius.toBoolean(),
            spoDeleteRecommendationRationale = updateRecommendationRequest.spoDeleteRecommendationRationale,
            status = Status.DRAFT,
            createdBy = existingRecommendation.data.createdBy,
            createdDate = existingRecommendation.data.createdDate,
            lastModifiedByUserName = "Bill",
            lastModifiedBy = "bill",
            lastModifiedDate = "2022-07-26T09:48:27.443Z",
          ),
        )

      // and
      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // and
      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      // and
      recommendationService = RecommendationService(
        recommendationRepository,
        recommendationStatusRepository,
        mockPersonDetailService,
        templateReplacementService,
        userAccessValidator,
        RiskService(deliusClient, arnApiClient, userAccessValidator, null),
        deliusClient,
        mrdEmitterMocked,
      )

      // when
      recommendationService.updateRecommendation(
        jsonRequest = recommendationJsonNode,
        recommendationId = 1L,
        userId = "bill",
        readableUserName = "Bill",
        featureFlags = null,
        isDntrDownloaded = false,
        isPartADownloaded = false,
        userEmail = null,
        pageRefreshIds = emptyList(),
      )

      // then
      then(recommendationRepository).should().save(recommendationToSave)
      then(recommendationRepository).should().findById(1)
      if (sendSpoDeleteRationaleToDelius == "true") {
        val captor = argumentCaptor<MrdEvent>()
        then(mrdEmitterMocked).should().sendEvent(captor.capture())
        val mrdEvent = captor.firstValue
        assertThat(mrdEvent.message?.personReference?.identifiers?.get(0)?.value).isEqualTo(crn)
        assertThat(mrdEvent.message?.additionalInformation?.contactOutcome).isEqualTo("DECISION_TO_RECALL")
        assertThat(mrdEvent.message?.additionalInformation?.recommendationUrl).isNotNull
        assertThat(mrdEvent.type).isEqualTo("Notification")
        assertThat(mrdEvent.messageId).isNotNull
        assertThat(mrdEvent.topicArn).isEqualTo("arn:aws:sns:eu-west-2:000000000000:hmpps-domain")
        assertThat(mrdEvent.message?.eventType).isEqualTo("prison-recall.recommendation.deleted")
        assertThat(mrdEvent.message?.version).isEqualTo(1)
        assertThat(mrdEvent.message?.description).isEqualTo("Deleted recommendation in 'Consider a recall'")
        assertThat(mrdEvent.message?.occurredAt).isNotNull
        assertThat(mrdEvent.timeStamp).isNotNull
        assertThat(mrdEvent.signingCertURL).isEqualTo(null) // handled by receiver
        assertThat(mrdEvent.subscribeUrl).isEqualTo(null) // handled by receiver
        assertThat(mrdEvent.messageAttributes?.eventType?.type).isEqualTo("String")
        assertThat(mrdEvent.messageAttributes?.eventType?.value).isEqualTo("prison-recall.recommendation.deleted")
      } else {
        then(mrdEmitterMocked).shouldHaveNoInteractions()
      }
    }
  }

  @ParameterizedTest()
  @CsvSource("true", "false")
  fun `updates a recommendation with manager recall decision to the database`(sentToDelius: String) {
    runTest {
      // given
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          recallConsideredList = listOf(
            RecallConsidered(
              createdDate = "2022-11-01T15:22:24.567Z",
              userName = "Harry",
              userId = "harry",
              recallConsideredDetail = "Recall considered",
            ),
          ),
          status = Status.RECALL_CONSIDERED,
          personOnProbation = PersonOnProbation(name = "John Smith"),
          lastModifiedBy = "Jack",
          lastModifiedDate = "2022-07-01T15:22:24.567Z",
          createdBy = "Jack",
          createdDate = "2022-07-01T15:22:24.567Z",
        ),
      )

      // and
      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationWithManagerRecallDecisionRequestData(
        existingRecommendation,
        sentToDelius,
      )

      // and
      val recommendationToSave =
        existingRecommendation.copy(
          id = existingRecommendation.id,
          data = RecommendationModel(
            crn = existingRecommendation.data.crn,
            sensitive = null,
            recallConsideredList = null,
            recallType = null,
            spoRecallType = "RECALL",
            spoRecallRationale = "Recall",
            sendSpoRationaleToDelius = sentToDelius.toBoolean(),
            managerRecallDecision = updateRecommendationRequest.managerRecallDecision,
            status = Status.DRAFT,
            createdBy = existingRecommendation.data.createdBy,
            createdDate = existingRecommendation.data.createdDate,
            lastModifiedByUserName = "Bill",
            lastModifiedBy = "bill",
            lastModifiedDate = "2022-07-26T09:48:27.443Z",
          ),
        )

      // and
      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // and
      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      // and
      recommendationService = RecommendationService(
        recommendationRepository,
        recommendationStatusRepository,
        mockPersonDetailService,
        templateReplacementService,
        userAccessValidator,
        RiskService(deliusClient, arnApiClient, userAccessValidator, null),
        deliusClient,
        mrdEmitterMocked,
      )

      // when
      recommendationService.updateRecommendation(
        jsonRequest = recommendationJsonNode,
        recommendationId = 1L,
        userId = "bill",
        readableUserName = "Bill",
        featureFlags = null,
        isDntrDownloaded = false,
        isPartADownloaded = false,
        userEmail = null,
        pageRefreshIds = emptyList(),
      )

      // then
      then(recommendationRepository).should().save(recommendationToSave)
      then(recommendationRepository).should().findById(1)
      if (sentToDelius == "true") {
        val captor = argumentCaptor<MrdEvent>()
        then(mrdEmitterMocked).should().sendEvent(captor.capture())
        val mrdEvent = captor.firstValue
        assertThat(mrdEvent.message?.personReference?.identifiers?.get(0)?.value).isEqualTo(crn)
        assertThat(mrdEvent.message?.additionalInformation?.contactOutcome).isEqualTo("DECISION_TO_RECALL")
        assertThat(mrdEvent.message?.additionalInformation?.recommendationUrl).isNotNull
        assertThat(mrdEvent.type).isEqualTo("Notification")
        assertThat(mrdEvent.messageId).isNotNull
        assertThat(mrdEvent.topicArn).isEqualTo("arn:aws:sns:eu-west-2:000000000000:hmpps-domain")
        assertThat(mrdEvent.message?.eventType).isEqualTo("prison-recall.recommendation.management-oversight")
        assertThat(mrdEvent.message?.version).isEqualTo(1)
        assertThat(mrdEvent.message?.description).isEqualTo("Management Oversight - Recall")
        assertThat(mrdEvent.message?.occurredAt).isNotNull
        assertThat(mrdEvent.timeStamp).isNotNull
        assertThat(mrdEvent.signingCertURL).isEqualTo(null) // handled by receiver
        assertThat(mrdEvent.subscribeUrl).isEqualTo(null) // handled by receiver
        assertThat(mrdEvent.messageAttributes?.eventType?.type).isEqualTo("String")
        assertThat(mrdEvent.messageAttributes?.eventType?.value).isEqualTo("prison-recall.recommendation.management-oversight")
      } else {
        then(mrdEmitterMocked).shouldHaveNoInteractions()
      }
    }
  }

  private fun recallConsideredIdWorkaround(recommendationToSave: RecommendationEntity) {
    val captor = argumentCaptor<RecommendationEntity>()
    then(recommendationRepository).should().save(captor.capture())
    val savedRecommendationEntity = captor.firstValue

    // Workaround to get the recallConsideredId as it changes every test
    val recallConsideredId = savedRecommendationEntity.data.recallConsideredList?.get(0)?.id
    recommendationToSave.data.recallConsideredList?.get(0)?.id = recallConsideredId!!
  }

  @Test
  fun `update recommendation with previous release details from Delius when previousReleases page refresh received`() {
    runTest {
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
        ),
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))
      given(deliusClient.getRecommendationModel(anyString())).willReturn(deliusRecommendationModelResponse())

      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      given(recommendationRepository.save(recommendationCaptor.capture())).willReturn(existingRecommendation)

      recommendationService.updateRecommendation(
        recommendationJsonNode,
        1L,
        "bill",
        "Bill",
        null,
        false,
        false,
        listOf("previousReleases"),
        null,
      )

      then(deliusClient).should(times(1)).getRecommendationModel(anyString())

      val recommendationEntity = recommendationCaptor.firstValue

      assertThat(recommendationEntity.data.previousReleases?.lastReleaseDate).isEqualTo(LocalDate.parse("2017-09-15"))
      assertThat(recommendationEntity.data.previousReleases?.lastReleasingPrisonOrCustodialEstablishment).isEqualTo("In the Community")
      assertThat(recommendationEntity.data.previousReleases?.hasBeenReleasedPreviously).isEqualTo(true)
      assertThat(recommendationEntity.data.previousReleases?.previousReleaseDates).isEqualTo(listOf(LocalDate.parse("2020-02-01")))
    }
  }

  @Test
  fun `update recommendation with latest previous recall details from Delius when previousRecall page refresh received`() {
    runTest {
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
        ),
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))
      given(deliusClient.getRecommendationModel(anyString())).willReturn(deliusRecommendationModelResponse())

      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      given(recommendationRepository.save(recommendationCaptor.capture())).willReturn(existingRecommendation)

      recommendationService.updateRecommendation(
        recommendationJsonNode,
        1L,
        "bill",
        "Bill",
        null,
        false,
        false,
        listOf("previousRecalls"),
        null,
      )

      then(deliusClient).should(times(1)).getRecommendationModel(anyString())

      val recommendationEntity = recommendationCaptor.firstValue

      assertThat(recommendationEntity.data.previousRecalls?.lastRecallDate).isEqualTo(LocalDate.parse("2020-10-15"))
      assertThat(recommendationEntity.data.previousRecalls?.hasBeenRecalledPreviously).isEqualTo(true)
      assertThat(recommendationEntity.data.previousRecalls?.previousRecallDates).isEqualTo(listOf(LocalDate.parse("2021-06-01")))
    }
  }

  @Test
  fun `update recommendation with mappa details from Delius when mappa page refresh received`() {
    runTest {
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          personOnProbation = PersonOnProbation(firstName = "Alan", surname = "Smith"),
        ),
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))
      given(deliusClient.getRecommendationModel(anyString())).willReturn(deliusRecommendationModelResponse())

      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      given(recommendationRepository.save(recommendationCaptor.capture())).willReturn(existingRecommendation)

      recommendationService.updateRecommendation(
        recommendationJsonNode,
        1L,
        "bill",
        "Bill",
        null,
        false,
        false,
        listOf("mappa"),
        null,
      )

      then(deliusClient).should(times(1)).getRecommendationModel(anyString())

      val recommendationEntity = recommendationCaptor.firstValue

      assertThat(recommendationEntity.data.personOnProbation?.mappa?.level).isEqualTo(1)
      assertThat(recommendationEntity.data.personOnProbation?.mappa?.category).isEqualTo(0)
      assertThat(recommendationEntity.data.personOnProbation?.mappa?.lastUpdatedDate).isEqualTo("2021-02-10")
      assertThat(recommendationEntity.data.personOnProbation?.mappa?.hasBeenReviewed).isEqualTo(true)
      assertThat(recommendationEntity.data.personOnProbation?.firstName).isEqualTo("Alan")
    }
  }

  @Test
  fun `update recommendation with rosh summary from Delius when riskOfSeriousHarm page refresh received`() {
    runTest {
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          personOnProbation = PersonOnProbation(firstName = "Alan", surname = "Smith"),
        ),
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))
      given(arnApiClient.getRiskSummary(anyString())).willReturn(Mono.fromCallable { riskSummaryResponse() })

      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      given(recommendationRepository.save(recommendationCaptor.capture())).willReturn(existingRecommendation)

      recommendationService.updateRecommendation(
        recommendationJsonNode,
        1L,
        "bill",
        "Bill",
        null,
        false,
        false,
        listOf("riskOfSeriousHarm"),
        null,
      )

      then(arnApiClient).should(times(1)).getRiskSummary(anyString())

      val recommendationEntity = recommendationCaptor.firstValue

      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToChildren).isEqualTo("LOW")
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToPublic).isEqualTo("LOW")
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToKnownAdult).isEqualTo("HIGH")
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToStaff).isEqualTo("VERY_HIGH")
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToPrisoners).isEqualTo("VERY_HIGH")
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToChildren).isEqualTo("HIGH")
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToPublic).isEqualTo("HIGH")
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToKnownAdult).isEqualTo(
        "HIGH",
      )
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToStaff).isEqualTo("MEDIUM")
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToPrisoners).isEqualTo("LOW")
      assertThat(recommendationEntity.data.roshSummary?.riskOfSeriousHarm?.overallRisk).isEqualTo("HIGH")
      assertThat(recommendationEntity.data.roshSummary?.lastUpdatedDate).isEqualTo("2022-10-09T08:26:31.000Z")
    }
  }

  @Test
  fun `update recommendation with index offence details from Delius when index offence refresh received`() {
    runTest {
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          personOnProbation = PersonOnProbation(firstName = "Alan", surname = "Smith"),
        ),
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))
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
      given(deliusClient.getRecommendationModel(anyString()))
        .willReturn(deliusRecommendationModelResponse(activeConvictions = listOf(activeConviction("Extended Determinate Sentence"))))

      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      given(recommendationRepository.save(recommendationCaptor.capture())).willReturn(existingRecommendation)

      recommendationService.updateRecommendation(
        recommendationJsonNode,
        1L,
        "bill",
        "Bill",
        null,
        false,
        false,
        listOf("indexOffenceDetails"),
        null,
      )

      then(arnApiClient).should().getAssessments(anyString())
      then(deliusClient).should().getRecommendationModel(ArgumentMatchers.anyString())

      val recommendationEntity = recommendationCaptor.firstValue

      assertThat(recommendationEntity.data.indexOffenceDetails).isEqualTo("Juicy offence details.")
    }
  }

  @Test
  fun `update recommendation with person details from Delius when person details page refresh received`() {
    runTest {
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
        ),
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))

      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      given(recommendationRepository.save(recommendationCaptor.capture())).willReturn(existingRecommendation)
      given(deliusClient.getRecommendationModel(anyString())).willReturn(deliusRecommendationModelResponse())

      recommendationService.updateRecommendation(
        recommendationJsonNode,
        1L,
        "bill",
        "Bill",
        null,
        false,
        false,
        listOf("personOnProbation"),
        null,
      )

      then(deliusClient).should().getRecommendationModel(anyString())

      val recommendationEntity = recommendationCaptor.firstValue
      val expected = personDetailsResponse().toPersonOnProbation()
      val actual = recommendationEntity.data.personOnProbation

      assertThat(expected.croNumber).isEqualTo(actual?.croNumber)
      assertThat(expected.mostRecentPrisonerNumber).isEqualTo(actual?.mostRecentPrisonerNumber)
      assertThat(expected.nomsNumber).isEqualTo(actual?.nomsNumber)
      assertThat(expected.pncNumber).isEqualTo(actual?.pncNumber)
      assertThat(expected.firstName).isEqualTo(actual?.firstName)
      assertThat(expected.middleNames).isEqualTo(actual?.middleNames)
      assertThat(expected.surname).isEqualTo(actual?.surname)
      assertThat(expected.ethnicity).isEqualTo(actual?.ethnicity)
      assertThat(expected.primaryLanguage).isEqualTo(actual?.primaryLanguage)
      assertThat(expected.dateOfBirth).isEqualTo(actual?.dateOfBirth)
      assertThat(expected.addresses).isEqualTo(actual?.addresses)
      assertThat(expected.firstName).isEqualTo(actual?.firstName)
    }
  }

  @Test
  fun `throws exception when no recommendation available for given id on an update`() {
    val recommendation = Optional.empty<RecommendationEntity>()

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    val updateRecommendationRequest = RecommendationModel(
      crn = null,
      status = null,
      recallType = null,
      custodyStatus = null,
      responseToProbation = null,
      triggerLeadingToRecall = null,
      whatLedToRecall = null,
      isThisAnEmergencyRecall = null,
      isIndeterminateSentence = null,
      isExtendedSentence = null,
      activeCustodialConvictionCount = null,
      hasVictimsInContactScheme = null,
      indeterminateSentenceType = null,
      dateVloInformed = null,
      alternativesToRecallTried = null,
      hasArrestIssues = null,
      hasContrabandRisk = null,
      underIntegratedOffenderManagement = null,
      convictionDetail = null,
    )

    val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
    val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

    Assertions.assertThatThrownBy {
      runTest {
        recommendationService.updateRecommendation(
          recommendationJsonNode,
          recommendationId = 456L,
          "bill",
          "Bill",
          null,
          false,
          false,
          emptyList(),
          null,
        )
      }
    }.isInstanceOf(NoRecommendationFoundException::class.java)
      .hasMessage("No recommendation found for id: 456")

    then(recommendationRepository).should().findById(456L)
  }

  @Test
  fun `get a recommendation from the database`() {
    val recommendation = Optional.of(MrdTestDataBuilder.recommendationDataEntityData(crn))

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    val recommendationResponse = recommendationService.getRecommendation(456L)

    assertThat(recommendationResponse.id).isEqualTo(recommendation.get().id)
    assertThat(recommendationResponse.createdBy).isEqualTo(recommendation.get().data.createdBy)
    assertThat(recommendationResponse.createdDate).isEqualTo(recommendation.get().data.createdDate)
    assertThat(recommendationResponse.recallConsideredList?.get(0)?.id).isNotNull
    assertThat(recommendationResponse.recallConsideredList?.get(0)?.createdDate).isEqualTo("2022-07-26T09:48:27.443Z")
    assertThat(recommendationResponse.recallConsideredList?.get(0)?.userName).isEqualTo("Bill")
    assertThat(recommendationResponse.recallConsideredList?.get(0)?.recallConsideredDetail).isEqualTo("I have concerns about their behaviour")
    assertThat(recommendationResponse.recallConsideredList?.get(0)?.userId).isEqualTo("bill")
    assertThat(recommendationResponse.id).isEqualTo(recommendation.get().id)
    assertThat(recommendationResponse.crn).isEqualTo(recommendation.get().data.crn)
    assertThat(recommendationResponse.personOnProbation).isEqualTo(recommendation.get().data.personOnProbation?.toPersonOnProbationDto())
    assertThat(recommendationResponse.status).isEqualTo(recommendation.get().data.status)
    assertThat(recommendationResponse.recallType?.selected?.value).isEqualTo(RecallTypeValue.FIXED_TERM)
    assertThat(recommendationResponse.recallType?.selected?.details).isEqualTo("My details")
    assertThat(recommendationResponse.recallType?.allOptions!![0].value).isEqualTo("NO_RECALL")
    assertThat(recommendationResponse.recallType?.allOptions!![0].text).isEqualTo("No recall")
    assertThat(recommendationResponse.recallType?.allOptions!![1].value).isEqualTo("FIXED_TERM")
    assertThat(recommendationResponse.recallType?.allOptions!![1].text).isEqualTo("Fixed term")
    assertThat(recommendationResponse.recallType?.allOptions!![2].value).isEqualTo("STANDARD")
    assertThat(recommendationResponse.recallType?.allOptions!![2].text).isEqualTo("Standard")
    assertThat(recommendationResponse.custodyStatus?.selected).isEqualTo(CustodyStatusValue.YES_PRISON)
    assertThat(recommendationResponse.custodyStatus?.details).isEqualTo("Bromsgrove Police Station\r\nLondon")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![0].value).isEqualTo("YES_PRISON")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![0].text).isEqualTo("Yes, prison custody")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![1].value).isEqualTo("YES_POLICE")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![1].text).isEqualTo("Yes, police custody")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![2].value).isEqualTo("NO")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![2].text).isEqualTo("No")
    assertThat(recommendationResponse.responseToProbation).isEqualTo("They have not responded well")
    assertThat(recommendationResponse.whatLedToRecall).isEqualTo("Increasingly violent behaviour")
    assertThat(recommendationResponse.isThisAnEmergencyRecall).isEqualTo(true)
    assertThat(recommendationResponse.isIndeterminateSentence).isEqualTo(true)
    assertThat(recommendationResponse.isExtendedSentence).isEqualTo(true)
    assertThat(recommendationResponse.activeCustodialConvictionCount).isEqualTo(1)
    assertThat(recommendationResponse.hasVictimsInContactScheme?.selected).isEqualTo(YesNoNotApplicableOptions.YES)
    assertThat(recommendationResponse.indeterminateSentenceType?.selected).isEqualTo(IndeterminateSentenceTypeOptions.LIFE)
    assertThat(recommendationResponse.dateVloInformed).isEqualTo(LocalDate.now())
    assertThat(recommendationResponse.hasArrestIssues?.selected).isEqualTo(true)
    assertThat(recommendationResponse.hasArrestIssues?.details).isEqualTo("Arrest issue details")
    assertThat(recommendationResponse.hasContrabandRisk?.selected).isEqualTo(true)
    assertThat(recommendationResponse.decisionDateTime).isNotNull()
    assertThat(recommendationResponse.hasContrabandRisk?.details).isEqualTo("Contraband risk details")
    assertThat(recommendationResponse.licenceConditionsBreached?.standardLicenceConditions?.selected!![0]).isEqualTo(
      SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name,
    )
    assertThat(recommendationResponse.licenceConditionsBreached?.standardLicenceConditions?.allOptions!![0].value).isEqualTo(
      SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name,
    )
    assertThat(recommendationResponse.licenceConditionsBreached?.standardLicenceConditions?.allOptions!![0].text).isEqualTo(
      "They had good behaviour",
    )
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.selected!![0]).isEqualTo("NST14")
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].title).isEqualTo(
      "Additional title",
    )
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].details).isEqualTo(
      "Additional details",
    )
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].note).isEqualTo(
      "Additional note",
    )
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].mainCatCode).isEqualTo(
      "NLC5",
    )
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].subCatCode).isEqualTo(
      "NST14",
    )
    assertThat(recommendationResponse.underIntegratedOffenderManagement?.selected).isEqualTo("YES")
    assertThat(recommendationResponse.underIntegratedOffenderManagement?.allOptions?.get(0)?.text).isEqualTo("Yes")
    assertThat(recommendationResponse.underIntegratedOffenderManagement?.allOptions?.get(0)?.value).isEqualTo("YES")
    assertThat(recommendationResponse.underIntegratedOffenderManagement?.allOptions?.get(1)?.text).isEqualTo("No")
    assertThat(recommendationResponse.underIntegratedOffenderManagement?.allOptions?.get(1)?.value).isEqualTo("NO")
    assertThat(recommendationResponse.underIntegratedOffenderManagement?.allOptions?.get(2)?.text).isEqualTo("N/A")
    assertThat(recommendationResponse.underIntegratedOffenderManagement?.allOptions?.get(2)?.value).isEqualTo("NOT_APPLICABLE")
    assertThat(recommendationResponse.localPoliceContact?.contactName).isEqualTo("Thomas Magnum")
    assertThat(recommendationResponse.localPoliceContact?.phoneNumber).isEqualTo("555-0100")
    assertThat(recommendationResponse.localPoliceContact?.faxNumber).isEqualTo("555-0199")
    assertThat(recommendationResponse.localPoliceContact?.emailAddress).isEqualTo("thomas.magnum@gmail.com")
    assertThat(recommendationResponse.convictionDetail?.indexOffenceDescription).isEqualTo("This is the index offence")
    assertThat(recommendationResponse.convictionDetail?.indexOffenceDescription).isEqualTo("This is the index offence")
    assertThat(recommendationResponse.convictionDetail?.dateOfOriginalOffence).isEqualTo("2022-09-01")
    assertThat(recommendationResponse.convictionDetail?.dateOfSentence).isEqualTo("2022-09-02")
    assertThat(recommendationResponse.convictionDetail?.lengthOfSentence).isEqualTo(6)
    assertThat(recommendationResponse.convictionDetail?.lengthOfSentenceUnits).isEqualTo("days")
    assertThat(recommendationResponse.convictionDetail?.sentenceDescription).isEqualTo("CJA - Extended Sentence")
    assertThat(recommendationResponse.convictionDetail?.licenceExpiryDate).isEqualTo("2022-09-03")
    assertThat(recommendationResponse.convictionDetail?.sentenceExpiryDate).isEqualTo("2022-09-04")
    assertThat(recommendationResponse.convictionDetail?.sentenceSecondLength).isEqualTo(12)
    assertThat(recommendationResponse.convictionDetail?.sentenceSecondLengthUnits).isEqualTo("months")
    assertThat(recommendationResponse.region).isEqualTo("London")
    assertThat(recommendationResponse.localDeliveryUnit).isEqualTo("LDU London")
    assertThat(recommendationResponse.fixedTermAdditionalLicenceConditions?.selected).isEqualTo(true)
    assertThat(recommendationResponse.fixedTermAdditionalLicenceConditions?.details).isEqualTo("This is an additional licence condition")
    assertThat(recommendationResponse.mainAddressWherePersonCanBeFound?.selected).isEqualTo(false)
    assertThat(recommendationResponse.mainAddressWherePersonCanBeFound?.details).isEqualTo("123 Acacia Avenue, Birmingham, B23 1AV")
    assertThat(recommendationResponse.whyConsideredRecall?.selected).isEqualTo(WhyConsideredRecallValue.RISK_INCREASED)
    assertThat(recommendationResponse.reasonsForNoRecall?.licenceBreach).isEqualTo("Reason for breaching licence")
    assertThat(recommendationResponse.reasonsForNoRecall?.noRecallRationale).isEqualTo("Rationale for no recall")
    assertThat(recommendationResponse.reasonsForNoRecall?.popProgressMade).isEqualTo("Progress made so far detail")
    assertThat(recommendationResponse.reasonsForNoRecall?.futureExpectations).isEqualTo("Future expectations detail")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.selected.toString()).isEqualTo("TELEPHONE")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(0)?.text).isEqualTo("Telephone")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(0)?.value.toString()).isEqualTo(
      "TELEPHONE",
    )
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(1)?.text).isEqualTo("Video call")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(1)?.value).isEqualTo("VIDEO_CALL")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(2)?.text).isEqualTo("Office visit")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(2)?.value).isEqualTo("OFFICE_VISIT")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(3)?.text).isEqualTo("Home visit")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(3)?.value).isEqualTo("HOME_VISIT")
    assertThat(recommendationResponse.nextAppointment?.dateTimeOfAppointment).isEqualTo("2022-04-24T20:39:00.000Z")
    assertThat(recommendationResponse.nextAppointment?.probationPhoneNumber).isEqualTo("01238282838")
    assertThat(recommendationResponse.previousReleases?.lastReleaseDate).isEqualTo("2022-09-02")
    assertThat(recommendationResponse.previousReleases?.lastReleasingPrisonOrCustodialEstablishment).isEqualTo("HMP Holloway")
    assertThat(recommendationResponse.previousReleases?.previousReleaseDates?.get(0)).isEqualTo("2020-02-01")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToChildren).isEqualTo("LOW")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToPublic).isEqualTo("MEDIUM")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToKnownAdult).isEqualTo("MEDIUM")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToStaff).isEqualTo("LOW")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCustody?.riskToPrisoners).isEqualTo("VERY_HIGH")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToChildren).isEqualTo("HIGH")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToPublic).isEqualTo("MEDIUM")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToKnownAdult).isEqualTo("MEDIUM")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToStaff).isEqualTo("LOW")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.riskInCommunity?.riskToPrisoners).isEqualTo("")
    assertThat(recommendationResponse.roshSummary?.riskOfSeriousHarm?.overallRisk).isEqualTo("HIGH")
    assertThat(recommendationResponse.roshSummary?.lastUpdatedDate).isEqualTo("2023-01-12T20:39:00.000Z")
    assertThat(recommendationResponse.whoCompletedPartA?.name).isEqualTo("Mr Jenkins")
    assertThat(recommendationResponse.whoCompletedPartA?.email).isEqualTo("jenkins@onsabatical.com")
    assertThat(recommendationResponse.whoCompletedPartA?.telephone).isEqualTo("1234567")
    assertThat(recommendationResponse.whoCompletedPartA?.region).isEqualTo("London")
    assertThat(recommendationResponse.whoCompletedPartA?.isPersonProbationPractitionerForOffender).isEqualTo(false)
    assertThat(recommendationResponse.whoCompletedPartA?.localDeliveryUnit).isEqualTo("A123")
    assertThat(recommendationResponse.practitionerForPartA?.name).isEqualTo("Mr Jenkins 1")
    assertThat(recommendationResponse.practitionerForPartA?.email).isEqualTo("jenkins1@onsabatical.com")
    assertThat(recommendationResponse.practitionerForPartA?.telephone).isEqualTo("12345678")
    assertThat(recommendationResponse.practitionerForPartA?.region).isEqualTo("London2")
    assertThat(recommendationResponse.practitionerForPartA?.localDeliveryUnit).isEqualTo("A1234")
  }

  @Test
  fun `get a recommendation in draft or recall considered state for CRN from the database`() {
    val recommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)

    given(recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name)))
      .willReturn(listOf(recommendation))

    val result = recommendationService.getRecommendationsInProgressForCrn(crn)

    assertThat(result?.recommendationId).isEqualTo(recommendation.id)
    assertThat(result?.lastModifiedBy).isEqualTo(recommendation.data.lastModifiedBy)
    assertThat(result?.lastModifiedDate).isEqualTo(recommendation.data.lastModifiedDate)
    assertThat(result?.recallType).isEqualTo(recommendation.data.recallType)
    assertThat(result?.recallConsideredList).isEqualTo(recommendation.data.recallConsideredList)
    assertThat(result?.status).isEqualTo(recommendation.data.status)
  }

  @Test
  fun `given case is excluded when fetching a recommendation for user then return user access response details`() {
    runTest {
      // given
      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(excludedAccess())
      given(recommendationRepository.findById(anyLong())).willReturn {
        Optional.of(
          RecommendationEntity(
            data = RecommendationModel(
              crn = crn,
            ),
          ),
        )
      }

      // when
      val response = recommendationService.getRecommendation(123L)

      // then
      assertThat(response.userAccessResponse).isEqualTo(
        UserAccess(
          userRestricted = false,
          userExcluded = true,
          userNotFound = false,
          exclusionMessage = "I am an exclusion message",
          restrictionMessage = null,
        ),
      )
    }
  }

  @Test
  fun `given case is excluded when creating a recommendation for user then return user access response details`() {
    runTest {
      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(excludedAccess())
      val response = recommendationService.createRecommendation(CreateRecommendationRequest(crn), "Bill", null, null)
      // then
      assertThat(response?.userAccessResponse).isEqualTo(
        UserAccess(
          userRestricted = false,
          userExcluded = true,
          userNotFound = false,
          exclusionMessage = "I am an exclusion message",
          restrictionMessage = null,
        ),
      )
      then(deliusClient).should().getUserAccess(username, crn)
    }
  }

  @Test
  fun `given case is excluded when updating a recommendation for user then no update is made to db`() {
    runTest {
      given(deliusClient.getUserAccess(anyString(), anyString())).willReturn(excludedAccess())

      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
        ),
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))

      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)
      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      try {
        recommendationService.updateRecommendation(
          recommendationJsonNode,
          1L,
          "bill",
          "Bill",
          null,
          false,
          false,
          emptyList(),
          null,
        )
      } catch (e: UserAccessException) {
        // nothing to do here!!
      }
      then(deliusClient).should().getUserAccess(username, crn)
      then(recommendationRepository).shouldHaveNoMoreInteractions()
    }
  }

  @Test
  fun `given invalid recall type should throw invalid request exception`() {
    runTest {
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          recallType = RecallType(
            selected = RecallTypeSelectedValue(RecallTypeValue.NO_RECALL),
            allOptions = listOf(TextValueOption("NO_RECALL")),
          ),
        ),
      )
      val updateRecommendationRequest = RecommendationModel(
        crn = crn,
        recallType = existingRecommendation.data.recallType?.copy(
          selected = RecallTypeSelectedValue(RecallTypeValue.FIXED_TERM),
          allOptions = listOf(TextValueOption("NO_RECALL")),
        ),
      )
      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      try {
        recommendationService.updateRecommendation(
          recommendationJsonNode,
          1L,
          "bill",
          "Bill",
          null,
          false,
          false,
          emptyList(),
          null,
        )
      } catch (e: InvalidRequestException) {
        // nothing to do here!!
      }
      then(recommendationRepository).shouldHaveNoMoreInteractions()
    }
  }

  @Test
  fun `given user is not found when updating a recommendation for user then return user access response details`() {
    runTest {
      given(deliusClient.getUserAccess(username, crn)).willThrow(PersonNotFoundException("Not found"))

      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
        ),
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))

      val response = recommendationService.getRecommendation(1L)
      assertThat(
        response,
        equalTo(
          RecommendationResponse(
            userAccessResponse = UserAccess(
              userRestricted = false,
              userExcluded = false,
              userNotFound = true,
              exclusionMessage = null,
              restrictionMessage = null,
            ),
          ),
        ),
      )
    }
  }

  @Test
  fun `given case is excluded when updating a recommendation for user then return user access response details`() {
    runTest {
      given(deliusClient.getUserAccess(username, crn)).willReturn(excludedAccess())

      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
        ),
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))

      val response = recommendationService.getRecommendation(1L)
      assertThat(
        response,
        equalTo(
          RecommendationResponse(
            userAccessResponse = UserAccess(
              userRestricted = false,
              userExcluded = true,
              userNotFound = false,
              exclusionMessage = "I am an exclusion message",
              restrictionMessage = null,
            ),
          ),
        ),
      )
    }
  }

  @Test
  fun `get the latest draft recommendation for CRN when multiple draft recommendations exist in database`() {
    val recommendation1 = RecommendationEntity(
      id = 1,
      data = RecommendationModel(
        crn = crn,
        lastModifiedBy = "John Smith",
        lastModifiedDate = "2022-07-19T23:00:00.000",
        createdBy = "Jack",
        createdDate = "2022-07-01T15:22:24.567Z",
      ),
    )
    val recommendation2 = RecommendationEntity(
      id = 2,
      data = RecommendationModel(
        crn = crn,
        lastModifiedBy = "Mary Berry",
        lastModifiedDate = "2022-08-01T10:00:00.000",
        createdBy = "Jack",
        createdDate = "2022-07-01T15:22:24.567Z",
      ),
    )
    val recommendation3 = RecommendationEntity(
      id = 3,
      data = RecommendationModel(
        crn = crn,
        lastModifiedBy = "Mary Berry",
        lastModifiedDate = "2022-08-01T11:00:00.000",
        createdBy = "Jack",
        createdDate = "2022-07-01T15:22:24.567Z",
      ),
    )
    val recommendation4 = RecommendationEntity(
      id = 4,
      data = RecommendationModel(
        crn = crn,
        lastModifiedBy = "Mary Berry",
        lastModifiedDate = "2022-08-01T09:00:00.000",
        createdBy = "Jack",
        createdDate = "2022-07-01T15:22:24.567Z",
      ),
    )
    val recommendation5 = RecommendationEntity(
      id = 5,
      data = RecommendationModel(
        crn = crn,
        lastModifiedBy = "Harry Winks",
        lastModifiedDate = "2022-07-26T12:00:00.000",
        createdBy = "Jack",
        createdDate = "2022-07-01T15:22:24.567Z",
      ),
    )

    given(recommendationRepository.findByCrnAndStatus(crn, listOf(Status.DRAFT.name, Status.RECALL_CONSIDERED.name)))
      .willReturn(listOf(recommendation1, recommendation2, recommendation3, recommendation4, recommendation5))

    val result = recommendationService.getRecommendationsInProgressForCrn(crn)

    assertThat(result?.recommendationId).isEqualTo(recommendation3.id)
    assertThat(result?.lastModifiedBy).isEqualTo(recommendation3.data.lastModifiedBy)
    assertThat(result?.lastModifiedDate).isEqualTo(recommendation3.data.lastModifiedDate)
    assertThat(result?.status).isEqualTo(recommendation3.data.status)
  }

  @Test
  fun `throws exception when no recommendation available for given id`() {
    val recommendation = Optional.empty<RecommendationEntity>()

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    Assertions.assertThatThrownBy {
      runTest {
        recommendationService.getRecommendation(456L)
      }
    }.isInstanceOf(NoRecommendationFoundException::class.java)
      .hasMessage("No recommendation found for id: 456")

    then(recommendationRepository).should().findById(456L)
  }

  // FIXME: Can probably remove this test once domain events feature is on as duplicated below
  @ParameterizedTest
  @CsvSource("true", "false")
  fun `generate DNTR letter from recommendation data`(firstDownload: Boolean) {
    runTest {
      given(recommendationStatusRepository.findByRecommendationId(1L)).willReturn(
        listOf(
          RecommendationStatusEntity(
            createdByUserFullName = "John Smith",
            active = true,
            created = null,
            createdBy = null,
            name = null,
            recommendationId = 1L,
          ),
        ),
      )
      initialiseWithMockedTemplateReplacementService()

      val existingRecommendation =
        if (!firstDownload) {
          MrdTestDataBuilder.recommendationDataEntityData(crn)
            .copy(
              data = RecommendationModel(
                crn = crn,
                personOnProbation = PersonOnProbation(firstName = "Jim", surname = "Long"),
                status = Status.DOCUMENT_DOWNLOADED,
                userNameDntrLetterCompletedBy = "Jack",
                lastDntrLetterADownloadDateTime = LocalDateTime.parse("2022-12-01T15:22:24"),
              ),
            )
        } else {
          val recommendationToSave = RecommendationEntity(
            data = RecommendationModel(
              crn = crn,
              personOnProbation = PersonOnProbation(firstName = "Jim", surname = "Long"),
            ),
          )

          given(recommendationRepository.save(any()))
            .willReturn(recommendationToSave)

          MrdTestDataBuilder.recommendationDataEntityData(crn)
        }

      given(
        templateReplacementServiceMocked.generateDocFromRecommendation(
          anyObject(),
          anyObject(),
          anyObject(),
          anyObject(),
        ),
      )
        .willReturn("Contents")

      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val result = recommendationService.generateDntr(
        1L,
        "john.smith",
        "John Smith",
        DocumentRequestType.DOWNLOAD_DOC_X,
        null,
      )

      assertThat(result.fileName).isEqualTo("No_Recall_26072022_Long_J_$crn.docx")
      assertThat(result.fileContents).isNotNull
      if (firstDownload) {
        val captorAfterRecommendationSaved = argumentCaptor<RecommendationEntity>()
        then(recommendationRepository).should(times(1)).save(captorAfterRecommendationSaved.capture())
        val savedRecommendationEntity = captorAfterRecommendationSaved.firstValue

        assertThat(savedRecommendationEntity.data.userNameDntrLetterCompletedBy).isEqualTo("John Smith")
        assertThat(savedRecommendationEntity.data.lastDntrLetterADownloadDateTime).isNotNull
        assertThat(savedRecommendationEntity.data.status).isEqualTo(Status.DRAFT)
      } else {
        then(recommendationRepository).should(times(0)).save(any())

        val captor = argumentCaptor<RecommendationResponse>()
        then(templateReplacementServiceMocked).should(times(1))
          .generateDocFromRecommendation(captor.capture(), anyObject(), anyObject(), anyObject())
        val recommendationResponseResult = captor.firstValue

        assertThat(recommendationResponseResult.userNameDntrLetterCompletedBy).isEqualTo("Jack")
        assertThat(recommendationResponseResult.lastDntrLetterDownloadDateTime).isEqualTo("2022-12-01T15:22:24")
        assertThat(recommendationResponseResult.status).isEqualTo(Status.DOCUMENT_DOWNLOADED)
        assertThat(recommendationResponseResult.status).isEqualTo(Status.DOCUMENT_DOWNLOADED)
      }
      then(mrdEmitterMocked).shouldHaveNoInteractions()
    }
  }

  @ParameterizedTest
  @CsvSource("true", "false")
  fun `generate DNTR letter from recommendation data and send domain event`(firstDownload: Boolean) {
    runTest {
      given(recommendationStatusRepository.findByRecommendationId(1L)).willReturn(
        listOf(
          RecommendationStatusEntity(
            createdByUserFullName = "John Smith",
            active = true,
            created = null,
            createdBy = null,
            name = null,
            recommendationId = 1L,
          ),
        ),
      )
      initialiseWithMockedTemplateReplacementService()
      val existingRecommendation =
        if (!firstDownload) {
          MrdTestDataBuilder.recommendationDataEntityData(crn)
            .copy(
              data = RecommendationModel(
                crn = crn,
                personOnProbation = PersonOnProbation(firstName = "Jim", surname = "Long"),
                status = Status.DOCUMENT_DOWNLOADED,
                userNameDntrLetterCompletedBy = "Jack",
                lastDntrLetterADownloadDateTime = LocalDateTime.parse("2022-12-01T15:22:24"),
              ),
            )
        } else {
          val recommendationToSave = RecommendationEntity(
            data = RecommendationModel(
              crn = crn,
              personOnProbation = PersonOnProbation(firstName = "Jim", surname = "Long"),
            ),
          )

          given(recommendationRepository.save(any()))
            .willReturn(recommendationToSave)

          MrdTestDataBuilder.recommendationDataEntityData(crn)
        }

      given(
        templateReplacementServiceMocked.generateDocFromRecommendation(
          anyObject(),
          anyObject(),
          anyObject(),
          anyObject(),
        ),
      )
        .willReturn("Contents")

      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val result = recommendationService.generateDntr(
        1L,
        "john.smith",
        "John Smith",
        DocumentRequestType.DOWNLOAD_DOC_X,
        null,
        FeatureFlags(flagSendDomainEvent = true),
      )

      assertThat(result.fileName).isEqualTo("No_Recall_26072022_Long_J_$crn.docx")
      assertThat(result.fileContents).isNotNull
      if (firstDownload) {
        val captorAfterRecommendationSaved = argumentCaptor<RecommendationEntity>()
        then(recommendationRepository).should(times(1)).save(captorAfterRecommendationSaved.capture())
        val savedRecommendationEntity = captorAfterRecommendationSaved.firstValue

        assertThat(savedRecommendationEntity.data.userNameDntrLetterCompletedBy).isEqualTo("John Smith")
        assertThat(savedRecommendationEntity.data.lastDntrLetterADownloadDateTime).isNotNull
        assertThat(savedRecommendationEntity.data.status).isEqualTo(Status.DRAFT)
        then(mrdEmitterMocked).should().sendEvent(org.mockito.kotlin.any())
      } else {
        then(recommendationRepository).should(times(0)).save(any())

        val captor = argumentCaptor<RecommendationResponse>()
        then(templateReplacementServiceMocked).should(times(1))
          .generateDocFromRecommendation(captor.capture(), anyObject(), anyObject(), anyObject())
        val recommendationResponseResult = captor.firstValue

        assertThat(recommendationResponseResult.userNameDntrLetterCompletedBy).isEqualTo("Jack")
        assertThat(recommendationResponseResult.lastDntrLetterDownloadDateTime).isEqualTo("2022-12-01T15:22:24")
        assertThat(recommendationResponseResult.status).isEqualTo(Status.DOCUMENT_DOWNLOADED)
        then(mrdEmitterMocked).shouldHaveNoInteractions()
      }
    }
  }

  @Test
  fun `generate DNTR letter preview from recommendation data`() {
    runTest {
      recommendationService = RecommendationService(
        recommendationRepository,
        recommendationStatusRepository,
        mockPersonDetailService,
        templateReplacementService,
        userAccessValidator,
        riskServiceMocked,
        deliusClient,
        mrdEmitterMocked,
      )
      val existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)

      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val result =
        recommendationService.generateDntr(
          1L,
          "john.smith",
          "John Smith",
          DocumentRequestType.PREVIEW,
          null,
          FeatureFlags(),
        )

      assertThat(result.letterContent?.salutation).isEqualTo("Dear Jim Long,")
      assertThat(result.letterContent?.letterAddress).isEqualTo(
        "Jim Long\n" +
          "Line 1 address\n" +
          "Line 2 address\n" +
          "Town address\n" +
          "TS1 1ST",
      )
    }
  }

  @Test
  fun `generate Part A document from recommendation data`() {
    runTest {
      val status = RecommendationStatusEntity(
        createdByUserFullName = "John Smith",
        active = true,
        created = "2023-08-08T08:20:05.047Z",
        createdBy = null,
        name = "DRAFT",
        recommendationId = 1L,
      )
      val acoSignedStatus = status.copy(name = "ACO_SIGNED")
      val spoSignedStatus = status.copy(name = "SPO_SIGNED")
      val poRecallConsultSpoStatus = status.copy(name = "PO_RECALL_CONSULT_SPO")

      given(recommendationStatusRepository.findByRecommendationId(1L)).willReturn(
        listOf(
          status,
          acoSignedStatus,
          spoSignedStatus,
          poRecallConsultSpoStatus,
        ),
      )
      initialiseWithMockedTemplateReplacementService()
      then(recommendationRepository).should(times(0)).save(any())

      val existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)
        .copy(
          data = RecommendationModel(
            crn = crn,
            personOnProbation = PersonOnProbation(
              name = "John Smith",
              firstName = "John",
              surname = "Smith",
            ),
          ),
        )
      given(
        templateReplacementServiceMocked.generateDocFromRecommendation(
          anyObject(),
          anyObject(),
          anyObject(),
          anyObject(),
        ),
      )
        .willReturn("Contents")

      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      // when
      val result = recommendationService.generatePartA(1L, "john.smith", "John Smith")

      assertThat(result.fileName).isEqualTo("NAT_Recall_Part_A_26072022_Smith_J_$crn.docx")
      assertThat(result.fileContents).isNotNull

      val recommendationResponseCaptor = argumentCaptor<RecommendationResponse>()
      val metaDataCaptor = argumentCaptor<RecommendationMetaData>()
      then(templateReplacementServiceMocked).should(times(1))
        .generateDocFromRecommendation(
          recommendationResponseCaptor.capture(),
          eq(DocumentType.PART_A_DOCUMENT),
          metaDataCaptor.capture(),
          anyObject(),
        )
      assertThat(metaDataCaptor.firstValue.countersignAcoDateTime).isNotNull
      assertThat(metaDataCaptor.firstValue.countersignSpoDateTime).isNotNull
      assertThat(metaDataCaptor.firstValue.userPartACompletedByDateTime).isNotNull
    }
  }

  @Test
  fun `generate Preview Part A document from recommendation data`() {
    runTest {
      val status = RecommendationStatusEntity(
        createdByUserFullName = "John Smith",
        active = true,
        created = "2023-08-08T08:20:05.047Z",
        createdBy = null,
        name = "DRAFT",
        recommendationId = 1L,
      )
      val acoSignedStatus = status.copy(name = "ACO_SIGNED")
      val spoSignedStatus = status.copy(name = "SPO_SIGNED")
      val poRecallConsultSpoStatus = status.copy(name = "PO_RECALL_CONSULT_SPO")

      given(recommendationStatusRepository.findByRecommendationId(1L)).willReturn(
        listOf(
          status,
          acoSignedStatus,
          spoSignedStatus,
          poRecallConsultSpoStatus,
        ),
      )
      initialiseWithMockedTemplateReplacementService()
      then(recommendationRepository).should(times(0)).save(any())

      val existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)
        .copy(
          data = RecommendationModel(
            crn = crn,
            personOnProbation = PersonOnProbation(
              name = "John Smith",
              firstName = "John",
              surname = "Smith",
            ),
          ),
        )
      given(
        templateReplacementServiceMocked.generateDocFromRecommendation(
          anyObject(),
          anyObject(),
          anyObject(),
          anyObject(),
        ),
      )
        .willReturn("Contents")

      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      // when
      val result = recommendationService.generatePartA(1L, "john.smith", "John Smith", true)

      assertThat(result.fileName).isEqualTo("Preview_NAT_Recall_Part_A_26072022_Smith_J_$crn.docx")
      assertThat(result.fileContents).isNotNull

      then(templateReplacementServiceMocked).should(times(1))
        .generateDocFromRecommendation(anyObject(), eq(DocumentType.PREVIEW_PART_A_DOCUMENT), anyObject(), anyObject())
    }
  }

  @Test
  fun `generate Part A document with missing recommendation data required to build filename`() {
    runTest {
      given(recommendationStatusRepository.findByRecommendationId(1L)).willReturn(
        listOf(
          RecommendationStatusEntity(
            createdByUserFullName = "John Smith",
            active = true,
            created = null,
            createdBy = null,
            name = null,
            recommendationId = 1L,
          ),
        ),
      )

      val existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(crn, "", "")

      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      given(recommendationStatusRepository.findByRecommendationId(1L)).willReturn(
        listOf(
          RecommendationStatusEntity(
            createdByUserFullName = "John Smith",
            active = true,
            created = null,
            createdBy = null,
            name = null,
            recommendationId = 1L,
          ),
        ),
      )

      val result = recommendationService.generatePartA(1L, "john.smith", "John Smith")

      assertThat(result.fileName).isEqualTo("NAT_Recall_Part_A_26072022___12345.docx")
      assertThat(result.fileContents).isNotNull
    }
  }

  @Test
  fun `return empty conviction detail when conviction is non custodial`() {
    runTest {
      // given
      val recommendationToSave = RecommendationEntity(
        data = RecommendationModel(
          crn = crn,
        ),
      )

      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // when
      recommendationService.createRecommendation(CreateRecommendationRequest(crn), "Bill", null, null)

      // then
      val captor = argumentCaptor<RecommendationEntity>()
      then(recommendationRepository).should().save(captor.capture())
      val recommendationEntity = captor.firstValue

      assertThat(recommendationEntity.data.convictionDetail).isNull()
    }
  }

  @Test
  fun `return empty conviction detail when there are multiple convictions`() {
    runTest {
      // given
      val recommendationToSave = RecommendationEntity(
        data = RecommendationModel(
          crn = crn,
        ),
      )

      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // when
      recommendationService.createRecommendation(CreateRecommendationRequest(crn), "Bill", null, null)

      // then
      val captor = argumentCaptor<RecommendationEntity>()
      then(recommendationRepository).should().save(captor.capture())
      val recommendationEntity = captor.firstValue

      assertThat(recommendationEntity.data.convictionDetail).isNull()
    }
  }

  @Test
  fun `return case overview response for last completed`() {
    runTest {
      val lastModifiedDate1 = "2022-07-02T15:22:24.567Z"
      val lastModifiedDate2 = "2022-07-01T15:22:24.567Z"
      val lastModifiedDate3 = "2022-07-03T15:22:24.567Z"

      val recommendation1 = MrdTestDataBuilder.recommendationDataEntityData(
        crn,
        status = Status.DOCUMENT_DOWNLOADED,
        recallTypeValue = RecallTypeValue.NO_RECALL,
        lastModifiedDate = lastModifiedDate1,
      )
      val recommendation2 = MrdTestDataBuilder.recommendationDataEntityData(
        crn,
        status = Status.DOCUMENT_DOWNLOADED,
        recallTypeValue = RecallTypeValue.NO_RECALL,
        lastModifiedDate = lastModifiedDate2,
      )
      val recommendation3 = MrdTestDataBuilder.recommendationDataEntityData(
        crn,
        status = Status.DOCUMENT_DOWNLOADED,
        recallTypeValue = RecallTypeValue.STANDARD,
        lastModifiedDate = lastModifiedDate3,
      )

      given(recommendationStatusRepository.findByRecommendationId(recommendation3.id)).willReturn(
        listOf(
          RecommendationStatusEntity(
            createdByUserFullName = "John Smith",
            active = true,
            created = null,
            createdBy = null,
            name = "COMPLETED",
            recommendationId = recommendation3.id,
          ),
        ),
      )

      given(recommendationRepository.findByCrn(crn)).willReturn(
        listOf(
          recommendation1,
          recommendation2,
          recommendation3,
        ),
      )

      val response = recommendationService.getLatestCompleteRecommendationOverview(crn)

      response.recommendations!!.get(0).let {
        assertThat(it.recommendationId, equalTo(recommendation3.id))
        assertThat(it.createdDate, equalTo(recommendation3.data.createdDate))
        assertThat(it.lastModifiedDate, equalTo(recommendation3.data.lastModifiedDate))
        assertThat(it.recallType, equalTo(recommendation3.data.recallType))
      }
    }
  }

  @Test
  fun `return case overview response for last completed when there are none`() {
    runTest {
      val recommendation3 = MrdTestDataBuilder.recommendationDataEntityData(
        crn,
        status = Status.DOCUMENT_DOWNLOADED,
        recallTypeValue = RecallTypeValue.STANDARD,
        lastModifiedDate = "2022-07-03T15:22:24.567Z",
      )

      given(recommendationRepository.findByCrn(crn)).willReturn(listOf(recommendation3))

      val response = recommendationService.getLatestCompleteRecommendationOverview(crn)

      assertThat(response.recommendations, equalTo(null))
    }
  }

  private fun initialiseWithMockedTemplateReplacementService() {
    recommendationService = RecommendationService(
      recommendationRepository,
      recommendationStatusRepository,
      mockPersonDetailService,
      templateReplacementServiceMocked,
      userAccessValidator,
      RiskService(deliusClient, arnApiClient, userAccessValidator, null),
      deliusClient,
      mrdEmitterMocked,
    )
  }

  object MockitoHelper {
    fun <T> anyObject(): T {
      Mockito.any<T>()
      return uninitialized()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> uninitialized(): T = null as T
  }
}
