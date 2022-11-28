package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.microsoft.applicationinsights.core.dependencies.google.gson.Gson
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTimeUtils
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.BDDMockito.anyString
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.BDDMockito.times
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.willReturn
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.MrdTestDataBuilder
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.AssessmentInfo
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentRequestType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RiskResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceTypeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecallValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.YesNoNotApplicableOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.UserAccessException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.mapper.ResourceLoader.CustomMapper
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RecommendationServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    DateTimeUtils.setCurrentMillisFixed(1658828907443)
  }

  @Mock
  protected lateinit var riskServiceMocked: RiskService

  @Mock
  protected lateinit var mrdEmitterMocked: MrdEventsEmitter

  @ParameterizedTest()
  @CsvSource("Extended Determinate Sentence", "CJA - Extended Sentence", "Random sentence description")
  fun `creates a new recommendation in the database`(sentenceDescription: String) {
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
            addresses = listOf(Address(line1 = "Line 1 address", line2 = "Line 2 address", town = "Town address", postcode = "TS1 1ST", noFixedAbode = false))
          )
        )
      )

      // and
      given(recommendationRepository.save(any())).willReturn(recommendationToSave)
      given(communityApiClient.getActiveConvictions(ArgumentMatchers.anyString(), anyBoolean())).willReturn(Mono.fromCallable { listOf(custodialConvictionResponse(sentenceDescription)) })
      recommendationService = RecommendationService(recommendationRepository, mockPersonDetailService, templateReplacementService, userAccessValidator, convictionService, riskServiceMocked, mrdEmitterMocked)

      // when
      val response = recommendationService.createRecommendation(CreateRecommendationRequest(crn), "Bill")

      // then
      assertThat(response.id).isNotNull
      assertThat(response.status).isEqualTo(Status.DRAFT)
      assertThat(response.personOnProbation).isEqualTo(recommendationToSave.data.personOnProbation)
      assertThat(response.indexOffenceDetails).isEqualTo(recommendationToSave.data.indexOffenceDetails)

      val captor = argumentCaptor<RecommendationEntity>()
      then(recommendationRepository).should().save(captor.capture())
      val recommendationEntity = captor.firstValue

      assertThat(recommendationEntity.id).isNotNull()
      assertThat(recommendationEntity.data.crn).isEqualTo(crn)
      assertThat(recommendationEntity.data.status).isEqualTo(Status.DRAFT)
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
              noFixedAbode = false
            )
          )
        )
      )

      val expectedCustodialTerm = if (sentenceDescription != "Random sentence description") "6 Days" else null
      val expectedExtendedTerm = if (sentenceDescription != "Random sentence description") "10 Months" else null

      assertThat(recommendationEntity.data.convictionDetail).isEqualTo(
        ConvictionDetail(
          indexOffenceDescription = "Robbery (other than armed robbery)",
          dateOfOriginalOffence = LocalDate.parse("2022-08-26"),
          dateOfSentence = LocalDate.parse("2022-04-26"),
          lengthOfSentence = 6,
          lengthOfSentenceUnits = "Days",
          sentenceDescription = sentenceDescription,
          licenceExpiryDate = LocalDate.parse("2022-05-10"),
          sentenceExpiryDate = LocalDate.parse("2022-06-10"),
          sentenceSecondLength = 10,
          sentenceSecondLengthUnits = "Months",
          custodialTerm = expectedCustodialTerm,
          extendedTerm = expectedExtendedTerm,
          hasBeenReviewed = false,
        )
      )
      assertThat(recommendationEntity.data.lastModifiedBy).isEqualTo("Bill")
      assertThat(recommendationEntity.data.lastModifiedDate).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(recommendationEntity.data.createdBy).isEqualTo("Bill")
      assertThat(recommendationEntity.data.createdDate).isEqualTo("2022-07-26T09:48:27.443Z")
      assertThat(recommendationEntity.data.region).isEqualTo("Probation area description")
      assertThat(recommendationEntity.data.localDeliveryUnit).isEqualTo("LDU description")
      assertThat(recommendationEntity.data.userNamePartACompletedBy).isNull()
      assertThat(recommendationEntity.data.lastPartADownloadDateTime).isNull()
    }
  }

  @Test
  fun `updates a recommendation to the database`() {
    runTest {
      // given
      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          status = Status.DRAFT,
          personOnProbation = PersonOnProbation(name = "John Smith"),
          lastModifiedBy = "Jack",
          lastModifiedDate = "2022-07-01T15:22:24.567Z",
          createdBy = "Jack",
          createdDate = "2022-07-01T15:22:24.567Z",
        )
      )

      // and
      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation)

      // and
      val recommendationToSave =
        existingRecommendation.copy(
          id = existingRecommendation.id,
          data = RecommendationModel(
            crn = existingRecommendation.data.crn,
            personOnProbation = PersonOnProbation(name = "John Smith", hasBeenReviewed = true),
            recallType = updateRecommendationRequest.recallType,
            custodyStatus = updateRecommendationRequest.custodyStatus,
            responseToProbation = updateRecommendationRequest.responseToProbation,
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
            status = existingRecommendation.data.status,
            lastModifiedDate = "2022-07-26T09:48:27.443Z",
            lastModifiedBy = "Bill",
            createdBy = existingRecommendation.data.createdBy,
            createdDate = existingRecommendation.data.createdDate,
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
            hasBeenReviewed = null
          )
        )

      // and
      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // and
      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      // when
      recommendationService.updateRecommendation(recommendationJsonNode, 1L, "Bill", null, false)

      // then
      then(recommendationRepository).should().save(recommendationToSave)
      then(recommendationRepository).should(times(2)).findById(1)
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
      convictionDetail = null
    )

    val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
    val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

    Assertions.assertThatThrownBy {
      runTest {
        recommendationService.updateRecommendation(
          recommendationJsonNode,
          recommendationId = 456L,
          "Bill",
          null,
          false
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
    assertThat(recommendationResponse.crn).isEqualTo(recommendation.get().data.crn)
    assertThat(recommendationResponse.personOnProbation).isEqualTo(recommendation.get().data.personOnProbation)
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
    assertThat(recommendationResponse.hasContrabandRisk?.details).isEqualTo("Contraband risk details")
    assertThat(recommendationResponse.licenceConditionsBreached?.standardLicenceConditions?.selected!![0]).isEqualTo(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name)
    assertThat(recommendationResponse.licenceConditionsBreached?.standardLicenceConditions?.allOptions!![0].value).isEqualTo(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name)
    assertThat(recommendationResponse.licenceConditionsBreached?.standardLicenceConditions?.allOptions!![0].text).isEqualTo("They had good behaviour")
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.selected!![0]).isEqualTo("NST14")
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].title).isEqualTo("Additional title")
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].details).isEqualTo("Additional details")
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].note).isEqualTo("Additional note")
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].mainCatCode).isEqualTo("NLC5")
    assertThat(recommendationResponse.licenceConditionsBreached?.additionalLicenceConditions?.allOptions!![0].subCatCode).isEqualTo("NST14")
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
    assertThat(recommendationResponse.userNamePartACompletedBy).isEqualTo("Ben Baker")
    assertThat(recommendationResponse.userEmailPartACompletedBy).isEqualTo("Ben.Baker@test.com")
    assertThat(recommendationResponse.lastPartADownloadDateTime).isNull()
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
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(0)?.value.toString()).isEqualTo("TELEPHONE")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(1)?.text).isEqualTo("Video call")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(1)?.value).isEqualTo("VIDEO_CALL")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(2)?.text).isEqualTo("Office visit")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(2)?.value).isEqualTo("OFFICE_VISIT")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(3)?.text).isEqualTo("Home visit")
    assertThat(recommendationResponse.nextAppointment?.howWillAppointmentHappen?.allOptions?.get(3)?.value).isEqualTo("HOME_VISIT")
    assertThat(recommendationResponse.nextAppointment?.dateTimeOfAppointment).isEqualTo("2022-04-24T20:39:00.000Z")
    assertThat(recommendationResponse.nextAppointment?.probationPhoneNumber).isEqualTo("01238282838")
  }

  @Test
  fun `get a draft recommendation for CRN from the database`() {
    val recommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)

    given(recommendationRepository.findByCrnAndStatus(crn, Status.DRAFT.name))
      .willReturn(listOf(recommendation))

    val result = recommendationService.getDraftRecommendationForCrn(crn)

    assertThat(result?.recommendationId).isEqualTo(recommendation.id)
    assertThat(result?.lastModifiedBy).isEqualTo(recommendation.data.lastModifiedBy)
    assertThat(result?.lastModifiedDate).isEqualTo(recommendation.data.lastModifiedDate)
    assertThat(result?.recallType).isEqualTo(recommendation.data.recallType)
  }

  @Test
  fun `given case is excluded when fetching a recommendation for user then return user access response details`() {
    runTest {
      // given
      given(communityApiClient.getUserAccess(anyString())).willThrow(WebClientResponseException(403, "Forbidden", null, excludedResponse().toByteArray(), null))
      given(recommendationRepository.findById(anyLong())).willReturn { Optional.of(RecommendationEntity(data = RecommendationModel(crn = crn))) }

      // when
      val response = recommendationService.getRecommendation(123L)

      // then
      assertThat(response.userAccessResponse).isEqualTo(
        UserAccessResponse(
          userRestricted = false,
          userExcluded = true,
          userNotFound = false,
          exclusionMessage = "I am an exclusion message",
          restrictionMessage = null
        )
      )
    }
  }

  @Test
  fun `given case is excluded when creating a recommendation for user then return user access response details`() {
    runTest {
      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(403, "Forbidden", null, excludedResponse().toByteArray(), null)
      )
      try {
        recommendationService.createRecommendation(CreateRecommendationRequest(crn), "Bill")
        fail()
      } catch (actual: UserAccessException) {
        val expected = UserAccessException(
          Gson().toJson(
            UserAccessResponse(
              userRestricted = false,
              userExcluded = true,
              userNotFound = false,
              exclusionMessage = "I am an exclusion message",
              restrictionMessage = null
            )
          )
        )
        assertThat(actual.message, equalTo((expected.message)))
      }
      then(communityApiClient).should().getUserAccess(crn)
    }
  }

  @Test
  fun `given case is excluded when updating a recommendation for user then no update is made to db`() {
    runTest {
      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(403, "Forbidden", null, excludedResponse().toByteArray(), null)
      )

      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn
        )
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))

      val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData(existingRecommendation).copy(status = Status.DOCUMENT_CREATED)
      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      try {
        recommendationService.updateRecommendation(recommendationJsonNode, 1L, "Bill", null, false)
      } catch (e: UserAccessException) {
        // nothing to do here!!
      }
      then(communityApiClient).should().getUserAccess(crn)
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
            allOptions = listOf(TextValueOption("NO_RECALL"))
          )
        )
      )
      val updateRecommendationRequest = RecommendationModel(
        crn = crn,
        recallType = existingRecommendation.data.recallType?.copy(
          selected = RecallTypeSelectedValue(RecallTypeValue.FIXED_TERM),
          allOptions = listOf(TextValueOption("NO_RECALL"))
        )
      )
      val json = CustomMapper.writeValueAsString(updateRecommendationRequest)
      val recommendationJsonNode: JsonNode = CustomMapper.readTree(json)

      try {
        recommendationService.updateRecommendation(recommendationJsonNode, 1L, "Bill", null, false)
      } catch (e: InvalidRequestException) {
        // nothing to do here!!
      }
      then(recommendationRepository).shouldHaveNoMoreInteractions()
    }
  }

  @Test
  fun `given user is not found when updating a recommendation for user then return user access response details`() {
    runTest {
      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          404,
          "Not found",
          null,
          null,
          null
        )
      )

      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn
        )
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))

      val response = recommendationService.getRecommendation(1L)
      assertThat(
        response,
        equalTo(
          RecommendationResponse(
            userAccessResponse = UserAccessResponse(
              userRestricted = false,
              userExcluded = false,
              userNotFound = true,
              exclusionMessage = null,
              restrictionMessage = null
            )
          )
        )
      )
    }
  }

  @Test
  fun `given case is excluded when updating a recommendation for user then return user access response details`() {
    runTest {
      given(communityApiClient.getUserAccess(crn)).willThrow(
        WebClientResponseException(
          403,
          "Forbidden",
          null,
          excludedResponse().toByteArray(),
          null
        )
      )

      val existingRecommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn
        )
      )

      given(recommendationRepository.findById(1L)).willReturn(Optional.of(existingRecommendation))

      val response = recommendationService.getRecommendation(1L)
      assertThat(
        response,
        equalTo(
          RecommendationResponse(
            userAccessResponse = UserAccessResponse(
              userRestricted = false,
              userExcluded = true,
              userNotFound = false,
              exclusionMessage = "I am an exclusion message",
              restrictionMessage = null
            )
          )
        )
      )
    }
  }

  @Test
  fun `get the latest draft recommendation for CRN when multiple draft recommendations exist in database`() {
    val recommendation1 = RecommendationEntity(
      id = 1,
      data = RecommendationModel(crn = crn, lastModifiedBy = "John Smith", lastModifiedDate = "2022-07-19T23:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )
    val recommendation2 = RecommendationEntity(
      id = 2,
      data = RecommendationModel(crn = crn, lastModifiedBy = "Mary Berry", lastModifiedDate = "2022-08-01T10:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )
    val recommendation3 = RecommendationEntity(
      id = 3,
      data = RecommendationModel(crn = crn, lastModifiedBy = "Mary Berry", lastModifiedDate = "2022-08-01T11:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )
    val recommendation4 = RecommendationEntity(
      id = 4,
      data = RecommendationModel(crn = crn, lastModifiedBy = "Mary Berry", lastModifiedDate = "2022-08-01T09:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )
    val recommendation5 = RecommendationEntity(
      id = 5,
      data = RecommendationModel(crn = crn, lastModifiedBy = "Harry Winks", lastModifiedDate = "2022-07-26T12:00:00.000", createdBy = "Jack", createdDate = "2022-07-01T15:22:24.567Z")
    )

    given(recommendationRepository.findByCrnAndStatus(crn, Status.DRAFT.name))
      .willReturn(listOf(recommendation1, recommendation2, recommendation3, recommendation4, recommendation5))

    val result = recommendationService.getDraftRecommendationForCrn(crn)

    assertThat(result?.recommendationId).isEqualTo(recommendation3.id)
    assertThat(result?.lastModifiedBy).isEqualTo(recommendation3.data.lastModifiedBy)
    assertThat(result?.lastModifiedDate).isEqualTo(recommendation3.data.lastModifiedDate)
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

  @Test
  fun `generate DNTR letter from recommendation data`() {
    runTest {
      val existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)

      val recommendationToSave = RecommendationEntity(
        data = RecommendationModel(
          crn = crn,
          personOnProbation = PersonOnProbation(firstName = "Jim", surname = "Long")
        )
      )

      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      val result = recommendationService.generateDntr(1L, "John Smith", DocumentRequestType.DOWNLOAD_DOC_X)

      val captor = argumentCaptor<RecommendationEntity>()
      then(recommendationRepository).should().save(captor.capture())
      val recommendationEntity = captor.firstValue

      assertThat(result.fileName).isEqualTo("No_Recall_26072022_Long_J_$crn.docx")
      assertThat(result.fileContents).isNotNull
      assertThat(recommendationEntity.data.userNameDntrLetterCompletedBy).isEqualTo("John Smith")
      assertThat(recommendationEntity.data.lastDntrLetterADownloadDateTime).isNotNull
    }
  }

  @Test
  fun `generate DNTR letter preview from recommendation data`() {
    runTest {
      given(riskServiceMocked.getRisk(anyString())).willReturn(
        RiskResponse(
          mappa = Mappa(
            category = 1,
            level = 1,
            lastUpdatedDate = null
          )
        )
      )
      recommendationService = RecommendationService(
        recommendationRepository,
        mockPersonDetailService,
        templateReplacementService,
        userAccessValidator,
        convictionService,
        riskServiceMocked,
        mrdEmitterMocked
      )
      val existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)

      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val result = recommendationService.generateDntr(1L, "John Smith", DocumentRequestType.PREVIEW)

      assertThat(result.letterContent?.salutation).isEqualTo("Dear Jim Long,")
      assertThat(result.letterContent?.letterAddress).isEqualTo(
        "Jim Long\n" +
          "Line 1 address\n" +
          "Line 2 address\n" +
          "Town address\n" +
          "TS1 1ST"
      )
    }
  }

  @Test
  fun `generate Part A document from recommendation data when optional fields missing`() {
    runTest {
      // given
      given(riskServiceMocked.getRisk(anyString())).willReturn(
        RiskResponse(
          mappa = null
        )
      )
      given(riskServiceMocked.fetchAssessmentInfo(anyString(), anyBoolean())).willReturn(null)
      given(mockPersonDetailService.getPersonDetails(anyString())).willReturn {
        personDetailsResponse().copy(
          personalDetailsOverview = PersonDetails(
            name = "John Smith",
            firstName = "John",
            surname = "Smith",
            crn = crn,
            age = 21,
            croNumber = "",
            dateOfBirth = null,
            ethnicity = "",
            gender = "",
            middleNames = "",
            nomsNumber = "",
            pncNumber = "",
            mostRecentPrisonerNumber = null,
            primaryLanguage = ""
          ),
          addresses = null,
          offenderManager = personDetailsResponse().offenderManager?.copy(
            probationAreaDescription = null,
            probationTeam = personDetailsResponse().offenderManager?.probationTeam?.copy(
              localDeliveryUnitDescription = null
            )
          )
        )
      }

      // and
      recommendationService = RecommendationService(
        recommendationRepository,
        mockPersonDetailService,
        templateReplacementService,
        userAccessValidator,
        convictionService,
        riskServiceMocked,
        mrdEmitterMocked
      )
      val existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)
        .copy(data = RecommendationModel(crn = crn, personOnProbation = null, indexOffenceDetails = null))
      val data = existingRecommendation.data
      val pop = data.personOnProbation
      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation.copy(data = data.copy(indexOffenceDetails = null, personOnProbation = pop?.copy(mappa = null, mostRecentPrisonerNumber = null)))))

      // and
      val recommendationToSave = RecommendationEntity(
        data = RecommendationModel(
          crn = crn,
          personOnProbation = PersonOnProbation(
            name = "John Smith",
            firstName = "John",
            surname = "Smith",
            mappa = null,
            addresses = null,
            dateOfBirth = null
          ),
          indexOffenceDetails = null
        )
      )
      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // when
      val result = recommendationService.generatePartA(1L, "John Smith", "John.Smith@test.com", null)

      // and
      val captor = argumentCaptor<RecommendationEntity>()
      then(recommendationRepository).should(times(1)).save(captor.capture())
      val recommendationEntity = captor.firstValue

      assertThat(result.fileName).isEqualTo("NAT_Recall_Part_A_26072022_Smith_J_$crn.docx")
      assertThat(result.fileContents).isNotNull
      assertThat(recommendationEntity.data.userNamePartACompletedBy).isEqualTo("John Smith")
      assertThat(recommendationEntity.data.userEmailPartACompletedBy).isEqualTo("John.Smith@test.com")
      assertThat(recommendationEntity.data.lastPartADownloadDateTime).isNotNull
    }
  }

  @ParameterizedTest()
  @CsvSource("true", "false")
  fun `generate Part A document from recommendation data with feature flags`(flagRecommendationOffenceDetails: Boolean) {
    runTest {
      // given
      given(riskServiceMocked.getRisk(anyString())).willReturn(
        RiskResponse(
          mappa = Mappa(category = 2, level = 2, lastUpdatedDate = null)
        )
      )
      given(riskServiceMocked.fetchAssessmentInfo(anyString(), anyBoolean())).willReturn(AssessmentInfo(offenceDescription = "Juicy details", offenceDataFromLatestCompleteAssessment = true, lastUpdatedDate = null, offencesMatch = true))
      given(mockPersonDetailService.getPersonDetails(anyString())).willReturn {
        personDetailsResponse().copy(
          personalDetailsOverview = PersonDetails(name = "John Smith", firstName = "John", surname = "Smith", crn = crn, age = 21, croNumber = "", dateOfBirth = LocalDate.now(), ethnicity = "", gender = "", middleNames = "", nomsNumber = "", pncNumber = "", mostRecentPrisonerNumber = "G12345", primaryLanguage = ""),
          addresses = listOf(
            Address(
              line1 = "Line 1 addressXYZ",
              line2 = "Line 2 addressXYZ",
              town = "Town address",
              postcode = "ABC CBA",
              noFixedAbode = false
            )
          )
        )
      }

      // and
      recommendationService = RecommendationService(
        recommendationRepository,
        mockPersonDetailService,
        templateReplacementService,
        userAccessValidator,
        convictionService,
        riskServiceMocked,
        mrdEmitterMocked
      )
      val existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)
        .copy(data = RecommendationModel(crn = crn, personOnProbation = null, indexOffenceDetails = null))
      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      // and
      val recommendationToSave = RecommendationEntity(
        data = RecommendationModel(
          crn = crn,
          personOnProbation = PersonOnProbation(
            name = "John Smith",
            firstName = "John",
            surname = "Smith",
            primaryLanguage = "English",
            mappa = Mappa(level = 2, category = 2, lastUpdatedDate = null),
            addresses = listOf(
              Address(
                line1 = "Line 1 addressXYZ",
                line2 = "Line 2 addressXYZ",
                town = "Town address",
                postcode = "ABC CBA",
                noFixedAbode = false
              )
            )
          ),
          indexOffenceDetails = "Juicy details"
        )
      )
      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // when
      val result = recommendationService.generatePartA(1L, "John Smith", "John.Smith@test.com", FeatureFlags(flagRecommendationOffenceDetails = true))

      // then
      val captor = argumentCaptor<RecommendationEntity>()
      then(recommendationRepository).should(times(1)).save(captor.capture())
      val recommendationEntity = captor.firstValue

      assertThat(result.fileName).isEqualTo("NAT_Recall_Part_A_26072022_Smith_J_$crn.docx")
      assertThat(result.fileContents).isNotNull
      assertThat(recommendationEntity.data.userNamePartACompletedBy).isEqualTo("John Smith")
      assertThat(recommendationEntity.data.userEmailPartACompletedBy).isEqualTo("John.Smith@test.com")
      assertThat(recommendationEntity.data.lastPartADownloadDateTime).isNotNull
    }
  }

  @Test
  fun `generate Part A document with missing recommendation data required to build filename`() {
    runTest {
      var existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(null, "", "")

      given(recommendationRepository.findById(any()))
        .willReturn(Optional.of(existingRecommendation))

      val recommendationToSave = RecommendationEntity(
        data = RecommendationModel(
          crn = crn
        )
      )

      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      val result = recommendationService.generatePartA(1L, "John smith", "John.Smith@test.com", null)

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
          crn = crn
        )
      )

      given(communityApiClient.getActiveConvictions(ArgumentMatchers.anyString(), anyBoolean()))
        .willReturn(Mono.fromCallable { listOf(nonCustodialConvictionResponse()) })

      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // when
      recommendationService.createRecommendation(CreateRecommendationRequest(crn), "Bill")

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
          crn = crn
        )
      )

      given(communityApiClient.getActiveConvictions(ArgumentMatchers.anyString(), anyBoolean()))
        .willReturn(Mono.fromCallable { listOf(custodialConvictionResponse(), custodialConvictionResponse()) })

      given(recommendationRepository.save(any()))
        .willReturn(recommendationToSave)

      // when
      recommendationService.createRecommendation(CreateRecommendationRequest(crn), "Bill")

      // then
      val captor = argumentCaptor<RecommendationEntity>()
      then(recommendationRepository).should().save(captor.capture())
      val recommendationEntity = captor.firstValue

      assertThat(recommendationEntity.data.convictionDetail).isNull()
    }
  }
}
