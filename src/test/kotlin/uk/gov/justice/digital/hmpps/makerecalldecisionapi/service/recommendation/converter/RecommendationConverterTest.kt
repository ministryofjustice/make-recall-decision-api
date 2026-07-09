package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbationDto
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.recommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

@ExtendWith(MockitoExtension::class)
class RecommendationConverterTest {

  @InjectMocks
  private lateinit var converter: RecommendationConverter

  @Mock
  private lateinit var personOnProbationConverter: PersonOnProbationConverter

  @Test
  fun convertsCorrectly() {
    val recommendationEntity = recommendationEntity()

    val personOnProbationDto = PersonOnProbationDto(randomString())
    given(personOnProbationConverter.convert(recommendationEntity.data.personOnProbation!!))
      .willReturn(personOnProbationDto)

    val recommendationResponse = converter.convert(recommendationEntity)

    // id is directly in recommendationEntity rather than within its data field, hence the separate comparison
    assertThat(recommendationResponse.id).isEqualTo(recommendationEntity.id)
    // These fields have slightly different names in each type, hence the separate comparison
    assertThat(recommendationResponse.lastDntrLetterDownloadDateTime).isEqualTo(recommendationEntity.data.lastDntrLetterADownloadDateTime)
    // personOnProbation is converted using the PersonOnProbationConverter, hence the separate comparison
    assertThat(recommendationResponse.personOnProbation).isEqualTo(personOnProbationDto)

    // We compare both ways to ensure we're not missing fields in either direction
    assertThat(recommendationResponse).usingRecursiveComparison()
      .ignoringFields(
        // the following set of fields is compared separately above
        "id",
        "lastDntrLetterDownloadDateTime",
        "personOnProbation",

        // the following field is constructed ad-hoc elsewhere, so we exclude it from the comparison
        "userAccessResponse",
      )
      .isEqualTo(recommendationEntity.data)
    assertThat(recommendationEntity.data).usingRecursiveComparison()
      .ignoringFields(
        // the following set of fields is compared separately above
        "id",
        "lastDntrLetterADownloadDateTime",
        "personOnProbation",

        // the following set of fields are used by the UI to trigger the API to send
        // events, so we exclude them from the response, as the UI doesn't need them
        "sendSpoRationaleToDelius",
        "sendSpoDeleteRationaleToDelius",

        // used by UI to tell API to mark items as reviewed, but not in the other direction, so excluded from responses
        "hasBeenReviewed",

        // not needed by the UI, so excluded from responses
        "recommendationStartedDomainEventSent",

        // was replaced by prisonOffender.locationDescription field
        "prisonApiLocationDescription",

        // the following fields are deprecated and so don't need to be included in
        // responses (as the UI doesn't need them)
        "thoughtsLeadingToRecall",
        "spoCancelRecommendationRationale",
        "isOver18",
        "isSentenceUnder12Months",
        "userNamePartACompletedBy",
        "userEmailPartACompletedBy",
        "lastPartADownloadDateTime",
        "countersignSpoDateTime",
        "countersignSpoName",
        "acoCounterSignEmail",
        "spoCounterSignEmail",
        "countersignAcoName",
        "countersignAcoDateTime",
        "deleted",
      )
      .isEqualTo(recommendationResponse)
  }
}
