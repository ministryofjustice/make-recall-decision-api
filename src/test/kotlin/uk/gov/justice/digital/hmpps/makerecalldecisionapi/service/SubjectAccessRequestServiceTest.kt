package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.MrdTestDataBuilder
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.MrdTestDataBuilder.Helper.buildCvlLicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.sar.SubjectAccessRequestResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class SubjectAccessRequestServiceTest : ServiceTestBase() {

  private lateinit var recommendation: RecommendationEntity
  private lateinit var recList: List<RecommendationEntity>

  @BeforeEach
  fun setup() {
    recommendation = MrdTestDataBuilder.recommendationDataEntityData(crn)
    recList = listOf(recommendation)
  }

  @Test
  fun `get a recommendation by CRN and created date from the database`() {
    val fromDate = LocalDate.of(2000, 1, 1)
    val toDate = LocalDate.of(2000, 6, 30)

    given(recommendationRepository.findRecommendationsForSar(crn, fromDate, toDate)).willReturn(recList)

    val probationContent = subjectAccessRequestService.getProbationContentFor(crn, fromDate, toDate)

    assertThat(probationContent).isNotNull()
    assertThat(probationContent?.content).isInstanceOf(SubjectAccessRequestResponse::class.java)
    val subjectAccessRequestResponse = probationContent?.content as SubjectAccessRequestResponse
    assertThat(subjectAccessRequestResponse.crn).isEqualTo(crn)
    assertThat(subjectAccessRequestResponse.recommendations).isEqualTo(recList.map { rec -> expectedTransformedData(rec) })
  }

  @Test
  fun `get a recommendation by CRN and created date from the database with transformed license conditions`() {
    val fromDate = LocalDate.of(2000, 1, 1)
    val toDate = LocalDate.of(2000, 6, 30)

    val recList = listOf(
      recommendation.copy(
        data = recommendation.data.copy(
          cvlLicenceConditionsBreached = buildCvlLicenceConditionsBreached(false),
        ),
      ),
    )

    val responseWithTransformedLicenceConditions = recList.map { rec ->
      expectedTransformedData(rec).copy(cvlLicenceConditionsBreached = buildCvlLicenceConditionsBreached(true))
    }

    given(recommendationRepository.findRecommendationsForSar(crn, fromDate, toDate)).willReturn(recList)

    val probationContent = subjectAccessRequestService.getProbationContentFor(crn, fromDate, toDate)

    assertThat(probationContent).isNotNull()
    assertThat(probationContent?.content).isInstanceOf(SubjectAccessRequestResponse::class.java)
    val subjectAccessRequestResponse = probationContent?.content as SubjectAccessRequestResponse
    assertThat(subjectAccessRequestResponse.crn).isEqualTo(crn)
    assertThat(subjectAccessRequestResponse.recommendations).isEqualTo(responseWithTransformedLicenceConditions)
  }

  @Test
  fun `get a recommendation by CRN with null date parameters from the database`() {
    given(recommendationRepository.findRecommendationsForSar(crn, null, null)).willReturn(recList)

    val probationContent = subjectAccessRequestService.getProbationContentFor(crn, null, null)

    assertThat(probationContent).isNotNull()
    assertThat(probationContent?.content).isInstanceOf(SubjectAccessRequestResponse::class.java)
    val subjectAccessRequestResponse = probationContent?.content as SubjectAccessRequestResponse
    assertThat(subjectAccessRequestResponse.crn).isEqualTo(crn)
    assertThat(subjectAccessRequestResponse.recommendations).isEqualTo(recList.map { rec -> expectedTransformedData(rec) })
  }

  @Test
  fun `returns null (204) when no subject access content available`() {
    given(recommendationRepository.findRecommendationsForSar(any(), any(), any())).willReturn(emptyList())

    val result = subjectAccessRequestService.getProbationContentFor("crn", LocalDate.now(), LocalDate.now())

    assertThat(result?.content).isNull()
  }

  @Test
  fun `get a recommendation by CRN transforms all user name fields to surnames`() {
    val fromDate = LocalDate.of(2000, 1, 1)
    val toDate = LocalDate.of(2000, 6, 30)

    val recWithNames = recommendation.copy(
      data = recommendation.data.copy(
        createdByUserFullName = "Jane Doe",
        odmName = "Off Manager123",
        lastModifiedByUserName = "Last Editor",
        considerationRationale = recommendation.data.considerationRationale?.copy(createdBy = "Rationale Author"),
        managerRecallDecision = recommendation.data.managerRecallDecision?.copy(createdBy = "Manager Person"),
        localPoliceContact = recommendation.data.localPoliceContact?.copy(contactName = "Police Contact"),
        whoCompletedPartA = recommendation.data.whoCompletedPartA?.copy(name = "Part A Completer"),
        practitionerForPartA = recommendation.data.practitionerForPartA?.copy(name = "Practitioner Person"),
      ),
    )

    val recList = listOf(recWithNames)

    given(recommendationRepository.findRecommendationsForSar(crn, fromDate, toDate)).willReturn(recList)

    val probationContent = subjectAccessRequestService.getProbationContentFor(crn, fromDate, toDate)

    val subjectAccessRequestResponse = probationContent?.content as SubjectAccessRequestResponse
    val transformedRec = subjectAccessRequestResponse.recommendations.first()

    assertThat(transformedRec.createdBy).isEqualTo("Doe")
    assertThat(transformedRec.odmName).isEqualTo("NO DATA")
    assertThat(transformedRec.lastModifiedByUserName).isEqualTo("Editor")
    assertThat(transformedRec.considerationRationale?.createdBy).isEqualTo("Author")
    assertThat(transformedRec.managerRecallDecision?.createdBy).isEqualTo("Person")
    assertThat(transformedRec.localPoliceContact?.contactName).isEqualTo("Contact")
    assertThat(transformedRec.whoCompletedPartA?.name).isEqualTo("Completer")
    assertThat(transformedRec.practitionerForPartA?.name).isEqualTo("Person")
  }

  // Builds the expected transformed recommendation data by applying the same
  // name-transformation logic used in the service, so tests stay in sync
  // regardless of what name-like values the test data builder produces.
  private fun expectedTransformedData(rec: RecommendationEntity) = rec.data.copy(
    createdBy = subjectAccessRequestService.transformNameToSurname(rec.data.createdByUserFullName),
    considerationRationale = rec.data.considerationRationale?.let {
      it.copy(createdBy = subjectAccessRequestService.transformNameToSurname(it.createdBy))
    },
    managerRecallDecision = rec.data.managerRecallDecision?.let {
      it.copy(createdBy = subjectAccessRequestService.transformNameToSurname(it.createdBy))
    },
    localPoliceContact = rec.data.localPoliceContact?.let {
      it.copy(contactName = subjectAccessRequestService.transformNameToSurname(it.contactName))
    },
    odmName = subjectAccessRequestService.transformNameToSurname(rec.data.odmName),
    lastModifiedByUserName = subjectAccessRequestService.transformNameToSurname(rec.data.lastModifiedByUserName),
    whoCompletedPartA = rec.data.whoCompletedPartA?.let {
      it.copy(name = subjectAccessRequestService.transformNameToSurname(it.name))
    },
    practitionerForPartA = rec.data.practitionerForPartA?.let {
      it.copy(name = subjectAccessRequestService.transformNameToSurname(it.name))
    },
  )
}
