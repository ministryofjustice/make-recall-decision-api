package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTimeUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.MrdTestDataBuilder
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CreateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternative
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternativeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UpdateRecommendationRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactSchemeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoRecommendationFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RecommendationServiceTest : ServiceTestBase() {

  @BeforeEach
  fun setup() {
    DateTimeUtils.setCurrentMillisFixed(1658828907443)
  }

  @Test
  fun `creates a new recommendation in the database`() {
    // given
    val recommendationToSave = RecommendationEntity(
      id = 1,
      data = RecommendationModel(
        crn = crn,
        status = Status.DRAFT,
        lastModifiedBy = "Bill",
        personOnProbation = PersonOnProbation(name = "John Smith")
      )
    )

    // and
    given(recommendationRepository.save(any()))
      .willReturn(recommendationToSave)

    // when
    val result = recommendationService.createRecommendation(CreateRecommendationRequest(crn), "Bill")

    // then
    assertThat(result.id).isEqualTo(1)
    assertThat(result.status).isEqualTo(Status.DRAFT)
    assertThat(result.personOnProbation?.name).isEqualTo("John Smith")

    then(recommendationRepository).should().save(
      recommendationToSave.copy(
        id = null,
        data = (
          RecommendationModel(
            crn = crn,
            status = Status.DRAFT,
            personOnProbation = PersonOnProbation(name = "John Smith", firstName = "John", surname = "Smith"),
            lastModifiedBy = "Bill",
            lastModifiedDate = "2022-07-26T09:48:27.443Z",
            createdBy = "Bill",
            createdDate = "2022-07-26T09:48:27.443Z"
          )
          )
      )
    )
  }

  @Test
  fun `updates a recommendation to the database`() {
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
        alternativesToRecallTried = AlternativesToRecallTried(
          selected = listOf(SelectedAlternative(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, details = "We sent a warning letter on 27th July 2022")),
          allOptions = listOf(TextValueOption(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, text = "Warnings/licence breach letters"))
        )
      )
    )

    // and
    val updateRecommendationRequest = MrdTestDataBuilder.updateRecommendationRequestData()

    // and
    val recommendationToSave =
      existingRecommendation.copy(
        id = existingRecommendation.id,
        data = RecommendationModel(
          crn = existingRecommendation.data.crn,
          personOnProbation = PersonOnProbation(name = "John Smith"),
          recallType = updateRecommendationRequest.recallType,
          custodyStatus = updateRecommendationRequest.custodyStatus,
          responseToProbation = updateRecommendationRequest.responseToProbation,
          isThisAnEmergencyRecall = updateRecommendationRequest.isThisAnEmergencyRecall,
          hasVictimsInContactScheme = updateRecommendationRequest.hasVictimsInContactScheme,
          dateVloInformed = updateRecommendationRequest.dateVloInformed,
          hasArrestIssues = updateRecommendationRequest.hasArrestIssues,
          status = existingRecommendation.data.status,
          lastModifiedDate = "2022-07-26T09:48:27.443Z",
          lastModifiedBy = "Bill",
          createdBy = existingRecommendation.data.createdBy,
          createdDate = existingRecommendation.data.createdDate,
          alternativesToRecallTried = existingRecommendation.data.alternativesToRecallTried
        )
      )

    // and
    given(recommendationRepository.save(any()))
      .willReturn(recommendationToSave)

    // and
    given(recommendationRepository.findById(any()))
      .willReturn(Optional.of(existingRecommendation))

    // when
    recommendationService.updateRecommendation(updateRecommendationRequest, 1L, "Bill")

    // then
    then(recommendationRepository).should().save(recommendationToSave)
    then(recommendationRepository).should().findById(1)
  }

  @Test
  fun `throws exception when no recommendation available for given id on an update`() {
    val recommendation = Optional.empty<RecommendationEntity>()

    given(recommendationRepository.findById(456L))
      .willReturn(recommendation)

    Assertions.assertThatThrownBy {
      runTest {
        recommendationService.updateRecommendation(
          UpdateRecommendationRequest(
            status = null,
            recallType = null,
            custodyStatus = null,
            responseToProbation = null,
            isThisAnEmergencyRecall = null,
            hasVictimsInContactScheme = null,
            dateVloInformed = null,
            alternativesToRecallTried = null,
            hasArrestIssues = null
          ),
          recommendationId = 456L,
          "Bill"
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
    assertThat(recommendationResponse.personOnProbation?.name).isEqualTo(recommendation.get().data.personOnProbation?.name)
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
    assertThat(recommendationResponse.custodyStatus?.allOptions!![0].value).isEqualTo("YES_PRISON")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![0].text).isEqualTo("Yes, prison custody")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![1].value).isEqualTo("YES_POLICE")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![1].text).isEqualTo("Yes, police custody")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![2].value).isEqualTo("NO")
    assertThat(recommendationResponse.custodyStatus?.allOptions!![2].text).isEqualTo("No")
    assertThat(recommendationResponse.responseToProbation).isEqualTo("They have not responded well")
    assertThat(recommendationResponse.isThisAnEmergencyRecall).isEqualTo(true)
    assertThat(recommendationResponse.hasVictimsInContactScheme?.selected).isEqualTo(VictimsInContactSchemeValue.YES)
    assertThat(recommendationResponse.dateVloInformed).isEqualTo(LocalDate.now())
    assertThat(recommendationResponse.hasArrestIssues?.selected).isEqualTo(true)
    assertThat(recommendationResponse.hasArrestIssues?.details).isEqualTo("Arrest issue details")
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
  fun `generate Part A document from recommendation data`() {
    val existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)

    given(recommendationRepository.findById(any()))
      .willReturn(Optional.of(existingRecommendation))

    val result = recommendationService.generatePartA(1L)

    assertThat(result.fileName).isEqualTo("NAT_Recall_Part_A_26072022_Long_J_$crn.docx")
    assertThat(result.fileContents).isNotNull()
  }

  @Test
  fun `generate Part A document with missing recommendation data required to build filename`() {
    var existingRecommendation = MrdTestDataBuilder.recommendationDataEntityData(null, "", "")

    given(recommendationRepository.findById(any()))
      .willReturn(Optional.of(existingRecommendation))

    val result = recommendationService.generatePartA(1L)

    assertThat(result.fileName).isEqualTo("NAT_Recall_Part_A_26072022___.docx")
    assertThat(result.fileContents).isNotNull()
  }
}
