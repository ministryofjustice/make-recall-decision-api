package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.converter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.MrdTestDataBuilder
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbationDto
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomInt
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

@ExtendWith(MockitoExtension::class)
class RecommendationConverterTest {

  @InjectMocks
  private lateinit var converter: RecommendationConverter

  @Mock
  private lateinit var personOnProbationConverter: PersonOnProbationConverter

  @Test
  fun convertsCorrectly() {
    val recommendationEntity = MrdTestDataBuilder.recommendationDataEntityData(randomInt().toString())

    val personOnProbationDto = PersonOnProbationDto(randomString())
    given(personOnProbationConverter.convert(recommendationEntity.data.personOnProbation!!))
      .willReturn(personOnProbationDto)

    val recommendationResponse = converter.convert(recommendationEntity)

    assertThat(recommendationResponse.id).isEqualTo(recommendationEntity.id)
    assertThat(recommendationResponse.lastDntrLetterDownloadDateTime).isEqualTo(recommendationEntity.data.lastDntrLetterADownloadDateTime)
    assertThat(recommendationResponse.personOnProbation).isEqualTo(personOnProbationDto)
    assertThat(recommendationResponse).usingRecursiveComparison()
      .ignoringFields(
        "id",
        "userAccessResponse",
        "lastDntrLetterDownloadDateTime",
        "personOnProbation",
      )
      .isEqualTo(recommendationEntity.data)
  }
}