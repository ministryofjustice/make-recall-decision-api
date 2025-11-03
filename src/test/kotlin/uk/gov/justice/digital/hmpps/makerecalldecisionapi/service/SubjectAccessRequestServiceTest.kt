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

    given(recommendationRepository.findByCrnAndCreatedDate(crn, fromDate, toDate)).willReturn(recList)

    val probationContent = subjectAccessRequestService.getProbationContentFor(crn, fromDate, toDate)

    assertThat(probationContent).isNotNull()
    assertThat(probationContent?.content).isInstanceOf(SubjectAccessRequestResponse::class.java)
    val subjectAccessRequestResponse = probationContent?.content as SubjectAccessRequestResponse
    assertThat(subjectAccessRequestResponse.crn).isEqualTo(crn)
    assertThat(subjectAccessRequestResponse.recommendations).isEqualTo(recList.map { rec -> rec.data })
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

    val responseWithTransformedLicenceConditions = recList.map { rec -> rec.data.copy(cvlLicenceConditionsBreached = buildCvlLicenceConditionsBreached(true)) }

    given(recommendationRepository.findByCrnAndCreatedDate(crn, fromDate, toDate)).willReturn(recList)

    val probationContent = subjectAccessRequestService.getProbationContentFor(crn, fromDate, toDate)

    assertThat(probationContent).isNotNull()
    assertThat(probationContent?.content).isInstanceOf(SubjectAccessRequestResponse::class.java)
    val subjectAccessRequestResponse = probationContent?.content as SubjectAccessRequestResponse
    assertThat(subjectAccessRequestResponse.crn).isEqualTo(crn)
    assertThat(subjectAccessRequestResponse.recommendations).isEqualTo(responseWithTransformedLicenceConditions)
  }

  @Test
  fun `get a recommendation by CRN with null date parameters from the database`() {
    given(recommendationRepository.findByCrnAndCreatedDate(crn, null, null)).willReturn(recList)

    val probationContent = subjectAccessRequestService.getProbationContentFor(crn, null, null)

    assertThat(probationContent).isNotNull()
    assertThat(probationContent?.content).isInstanceOf(SubjectAccessRequestResponse::class.java)
    val subjectAccessRequestResponse = probationContent?.content as SubjectAccessRequestResponse
    assertThat(subjectAccessRequestResponse.crn).isEqualTo(crn)
    assertThat(subjectAccessRequestResponse.recommendations).isEqualTo(recList.map { rec -> rec.data })
  }

  @Test
  fun `returns null (204) when no subject access content available`() {
    given(recommendationRepository.findByCrnAndCreatedDate(any(), any(), any())).willReturn(emptyList())

    val result = subjectAccessRequestService.getProbationContentFor("crn", LocalDate.now(), LocalDate.now())

    assertThat(result?.content).isNull()
  }
}
